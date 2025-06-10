// Libraries & stores
import { plausible } from "@/ts/plausible"
import * as API from "@/api/documents"
import { documentsPath } from "@/api/documents"
import * as Utils from "@/api/utils"
import stores from "@/stores"
import type { UUID } from "@/types/corpora"
// Types & API
import { type DocumentMetadata, Format } from "@/types/documents"
import { useAxios } from "@/api/useAxios"

// Custom types
type FileStatus = {
    status: "busy" | "success" | "error"
    message?: string
}

/**
 * Contains the documents for the corpusId.value
 * as well as functionality related to the user's documents, like uploading.
 */
const documents = defineStore("documents", () => {
    // Stores
    const errors = stores.useErrors()
    const { corpusId, corpus } = storeToRefs(stores.useCorpora())
    const { reload: reloadCorpora } = stores.useCorpora()

    // Fields
    const {
        data: documents,
        loading,
        reload
    } = useAxios<DocumentMetadata[]>(
        (): string | undefined =>
            corpusId.value ? documentsPath(corpusId.value) : undefined,
        []
    )

    const numSourceAnnotations = computed(
        () => documents.value.filter(i => i.layerSummary?.tokens > 0).length
    )
    const uploading: Record<string, FileStatus> = reactive({})
    const uploadBusyCount = computed(
        () => Object.values(uploading).filter(i => i.status === "busy").length
    )
    const uploadErrorCount = computed(
        () => Object.values(uploading).filter(i => i.status === "error").length
    )
    const filesToUpload = ref<File[]>([])
    const illegalFiles = computed((): File[] => {
        return filesToUpload.value.filter((x: any) => {
            const ext = x.name.split(".").at(-1)
            return ![
                "xml",
                "tsv",
                "txt",
                "zip",
                "conllu",
                "naf",
                "pdf",
                "docx"
            ].includes(ext)
        })
    })

    /**
     * Delete a document.
     * @param name Document name.
     */
    function deleteDocument(name: string): void {
        plausible.documentDeleted(corpus.value, getDocument(name))
        API.deleteDocument(corpusId.value, name)
            .catch(error => errors.handle(error))
            .finally(reload)
    }

    /**
     * Download original source document.
     * @param name Document name.
     */
    function downloadRaw(name: string): void {
        plausible.documentDownloaded(corpus.value, getDocument(name))
        API.getRawDocument(corpusId.value, name)
            .then(Utils.browserDownloadResponseFile)
            .catch(res =>
                Utils.handleBlobError(res, "download raw document", errors)
            )
    }

    function getDocument(name: string): DocumentMetadata {
        return documents.value.find(
            (d: DocumentMetadata) => d.name === name
        ) as DocumentMetadata
    }

    /**
     * Upload all files in filesToUpload.
     * Creates timeouts to spread load.
     */
    function uploadAll(): void {
        for (let i = 0; i < filesToUpload.value.length; i++) {
            const formData = new FormData()
            const file = filesToUpload.value[i]
            // if( file.size > MAX_FILE_SIZE ) continue // skip too large files
            formData.append("file", file)
            uploading[file.name] = { status: "busy" }
            // Spread the uploads a little
            setTimeout(() => upload(formData), (i / 10) * 100)
        }
        filesToUpload.value = []
    }

    /**
     * Clear errors from not yet uploaded files.
     */
    function clearUploadErrors(): void {
        Object.keys(uploading).forEach(key => {
            if (uploading[key].status === "error") delete uploading[key]
        })
    }

    /**
     * Add content type header.
     * @param fd FormData with file to upload.
     * @param contentType Content type header.
     * @param exts File extensions to apply the content type header to.
     */
    function addContentTypeHeader(fd: FormData): Record<string, string> | null {
        const exts_and_headers = {
            tsv: "text/tab-separated-values",
            conllu: "text/tab-separated-values",
            naf: "text/xml"
        }

        let file = fd.get("file") as File
        const extension = fileExtension(file)
        let header = null

        if (Object.keys(exts_and_headers).includes(extension)) {
            const contentType = exts_and_headers[extension]
            file = new File([file], file.name, { type: contentType })
            header = { "Content-Type": contentType }
            fd.set("file", file)
        }
        return header
    }

    function fileExtension(file: File): string {
        return file.name.split(".").at(-1)
    }

    /**
     * Upload a single file. Takes http content type header into account.
     * @param formData FormData with file to upload.
     */
    function upload(formData: FormData): void {
        const file = formData.get("file") as File

        // Some files need an explicit content type header.
        const header = addContentTypeHeader(formData)

        plausible.documentUploaded(corpus.value, fileExtension(file))

        // Update status on upload, on success and on error.
        uploading[file?.name] = { status: "busy" }
        API.postDocument(corpusId.value, formData, header)
            .then(() => {
                uploading[file?.name] = { status: "success" }
            })
            .catch(
                error =>
                    (uploading[file.name] = {
                        status: "error",
                        message: error.response.data.message
                    })
            )
            .finally(() => {
                if (uploadBusyCount.value === 0) {
                    reload()
                    reloadCorpora()
                }
            })
    }

    /**
     * Checks if the documentsStore.documents contains at least one file of the given format.
     */
    function containsFormat(format: Format): boolean {
        return documents.value.some(i => {
            // Overwrite the format for legacy formats.
            let otherFormat = i.format
            if (otherFormat === Format.TEI_P5_LEGACY) {
                otherFormat = Format.TEI_P5
            }
            return otherFormat === format
        })
    }

    // Exports
    return {
        // Fields
        documents,
        filesToUpload,
        illegalFiles,
        loading,
        uploading,
        uploadBusyCount,
        uploadErrorCount,
        numSourceAnnotations,
        // Methods
        deleteDocument,
        downloadRaw,
        uploadAll,
        clearUploadErrors,
        containsFormat
    }
})

export default documents

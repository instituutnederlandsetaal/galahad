// Libraries & stores

import * as API from "@/api/documents"
import * as Utils from "@/api/utils"
import stores from "@/stores"
import type { UUID } from "@/types/corpora"
// Types & API
import { type DocumentMetadata, Format } from "@/types/documents"

const MAX_FILE_SIZE = 10485760 // 10 MB

// Custom types
type FileStatus = {
    status: "busy" | "success" | "error"
    message?: string
}

/**
 * Contains the documents for the corporaStore.activeUUID
 * as well as functionality related to the user's documents, like uploading.
 */
const documents = defineStore("documents", () => {
    // Stores
    const errorsStore = stores.useErrors()
    const corporaStore = stores.useCorpora()

    // Fields
    const loading = ref(false)
    const available = ref([] as DocumentMetadata[])
    const numSourceAnnotations = computed(
        () => available.value.filter(i => i.layerSummary?.tokens > 0).length,
    )
    const uploading: Record<string, FileStatus> = reactive({})
    const uploadBusyCount = computed(
        () => Object.values(uploading).filter(i => i.status === "busy").length,
    )
    const uploadErrorCount = computed(
        () => Object.values(uploading).filter(i => i.status === "error").length,
    )
    const filesToUpload = ref([] as File[])
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
                "docx",
            ].includes(ext)
        })
    })
    const tooLargeFiles = computed((): File[] => {
        return filesToUpload.value.filter((x: any) => x.size > MAX_FILE_SIZE)
    })

    // Methods
    /**
     * Reloads documentsStore.available for a given corpus.
     * @param corpus
     */
    function reloadDocumentsForCorpus(corpus: UUID) {
        if (!corpus) return
        // Reset
        loading.value = true
        // Fetch
        API.getDocuments(corpus)
            .then(response => {
                // Only update if the response is for the active corpus.
                if (response.request.responseURL.includes(corpus)) {
                    available.value = response.data
                }
            })
            .catch(error => {
                errorsStore.handle("fetch documents", error)
            })
            .finally(() => (loading.value = false))
    }

    /**
     * Reload documents for the active user corpus and updates the corpora list.
     * The latter is to update the document count which unlocks, e.g., the job tab.
     */
    function reloadForActiveUserCorpus() {
        reloadDocumentsForCorpus(corporaStore.activeUUID)
        corporaStore.reload()
    }

    /**
     * Delete a document.
     * @param documentName Document name.
     */
    function deleteDocument(documentName: string) {
        API.deleteDocument(corporaStore.activeUUID, documentName)
            .catch(error => errorsStore.handle("delete document", error))
            .finally(reloadForActiveUserCorpus)
    }

    /**
     * Download original source document.
     * @param documentName Document name.
     */
    function downloadRaw(documentName: string) {
        API.getRawDocument(corporaStore.activeUUID, documentName)
            .then(Utils.browserDownloadResponseFile)
            .catch(res =>
                Utils.handleBlobError(
                    res,
                    "download raw document",
                    errorsStore,
                ),
            )
    }

    /**
     * Upload all files in filesToUpload.
     * Creates timeouts to spread load.
     */
    function uploadAll() {
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
    function clearUploadErrors() {
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
            naf: "text/xml",
        }

        let file = fd.get("file") as File
        const extension = file?.name.split(".").at(-1)
        let header = null

        if (Object.keys(exts_and_headers).includes(extension)) {
            const contentType = exts_and_headers[extension]
            file = new File([file], file.name, { type: contentType })
            header = { "Content-Type": contentType }
            fd.set("file", file)
        }
        return header
    }

    /**
     * Upload a single file. Takes http content type header into account.
     * @param formData FormData with file to upload.
     */
    function upload(formData: FormData) {
        const file = formData.get("file") as File

        // Some files need an explicit content type header.
        const header = addContentTypeHeader(formData)

        // Update status on upload, on success and on error.
        uploading[file?.name] = { status: "busy" }
        API.postDocument(corporaStore.activeUUID, formData, header)
            .then(() => {
                uploading[file?.name] = { status: "success" }
            })
            .catch(
                error =>
                    (uploading[file.name] = {
                        status: "error",
                        message: error.response.data.message,
                    }),
            )
            .finally(() => {
                if (uploadBusyCount.value === 0) reloadForActiveUserCorpus()
            })
    }

    /**
     * Checks if the documentsStore.available contains at least one file of the given format.
     */
    function containsFormat(format: Format): boolean {
        return available.value.some(i => {
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
        available,
        filesToUpload,
        illegalFiles,
        loading,
        tooLargeFiles,
        uploading,
        uploadBusyCount,
        uploadErrorCount,
        numSourceAnnotations,
        // Methods
        reloadDocumentsForCorpus,
        deleteDocument,
        downloadRaw,
        uploadAll,
        clearUploadErrors,
        containsFormat,
    }
})

export default documents

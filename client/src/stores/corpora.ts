// Libraries & stores

import * as API from "@/api/corpora"
import { plausible } from "@/ts/plausible"
import stores from "@/stores"
// Types & API
import type { CorpusMetadata, MutableCorpusMetadata, UUID } from "@/types/corpora"
import { useRouteQuery } from "@vueuse/router"
import { useAxios } from "@/api/useAxios"

/**
 * Contains all corpora for which the user has read access.
 */
const useCorpora = defineStore("corpora", () => {
    // Stores
    const user = stores.useUser()
    const errors = stores.useErrors()

    // Fields
    const corpusId = useRouteQuery<UUID>("corpus")
    const { data: corpora, loading, reload } = useAxios<CorpusMetadata[]>(API.corporaPath, [])
    const datasets = computed<CorpusMetadata[]>((): CorpusMetadata[] => corpora.value?.filter((i) => i.dataset) ?? [])
    const sharedCorpora = computed<CorpusMetadata[]>(
        (): CorpusMetadata[] => corpora.value?.filter((i) => !i.dataset && i.owner !== user.user?.id) ?? [],
    )
    const corpus = computed<CorpusMetadata>(
        (): CorpusMetadata => corpora.value?.find((i) => i.uuid === corpusId.value) as CorpusMetadata,
    )
    const hasDocs = computed((): boolean => Boolean(corpus.value?.numDocs))
    const isCollaborator = computed((): boolean => corpus.value?.collaborators.includes(user.user?.id) ?? false)
    const isOwner = computed<boolean>((): boolean => corpus.value?.owner === user.user?.id)

    /**
     * Create a new corpus with the given metadata and set it as active.
     * @param metadata Metadata of the new corpus.
     */
    function create(metadata: MutableCorpusMetadata): void {
        plausible.corpusCreated(metadata)
        API.postCorpus(metadata)
            // Automatically set the new corpus as active.
            .then((response) => {
                console.log("Created corpus", response.data)
                corpusId.value = response.data
            })
            .catch((error) => errors.handle(error))
            .finally(reload)
    }

    /**
     * Delete and unselect corpus.
     * @param metadata Corpus to delete.
     */
    function remove(metadata: CorpusMetadata): void {
        plausible.corpusDeleted(metadata)
        API.deleteCorpus(metadata.uuid)
            .then(() => {
                // Deselect now deleted corpus
                if (metadata.uuid === corpusId.value) {
                    corpusId.value = undefined
                }
            })
            .catch((error) => errors.handle(error))
            .finally(reload)
    }

    /**
     * Update metadata of existing corpus. Keeps it selected.
     * @param metadata Updated metadata.
     */
    function update(metadata: CorpusMetadata): void {
        plausible.corpusUpdated(metadata)
        API.patchCorpus(metadata.uuid, metadata)
            .catch((error) => errors.handle(error))
            .finally(reload)
    }

    // Exports
    return {
        // Fields
        corpora,
        loading,
        datasets,
        sharedCorpora,
        corpus,
        hasDocs,
        corpusId,
        isCollaborator,
        isOwner,
        // Methods
        reload,
        create,
        remove,
        update,
    }
})

export default useCorpora

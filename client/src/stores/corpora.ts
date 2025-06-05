// Libraries & stores

import * as API from "@/api/corpora"
import { plausible } from "@/ts/plausible"
import stores from "@/stores"
// Types & API
import type {
    CorpusMetadata,
    MutableCorpusMetadata,
    UUID
} from "@/types/corpora"
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
    const activeUUID = useRouteQuery("corpus")
    const {
        data: allCorpora,
        loading,
        reload
    } = useAxios<CorpusMetadata[]>(API.corporaPath, [])
    const datasets = computed<CorpusMetadata[]>(
        (): CorpusMetadata[] => allCorpora.value?.filter(i => i.dataset) ?? []
    )
    const sharedCorpora = computed<CorpusMetadata[]>(
        (): CorpusMetadata[] =>
            allCorpora.value?.filter(
                i => !i.dataset && i.owner !== user.user?.id
            ) ?? []
    )
    const activeCorpus = computed((): CorpusMetadata | undefined =>
        allCorpora.value?.find(i => i.uuid === activeUUID.value)
    )
    const hasDocs = computed((): boolean =>
        Boolean(activeCorpus.value?.numDocs)
    )
    const isCollaborator = computed(
        (): boolean =>
            activeCorpus.value?.collaborators.includes(user.user?.id) ?? false
    )
    const isOwner = computed<boolean>(
        (): boolean => activeCorpus.value?.owner === user.user?.id
    )

    /**
     * Create a new corpus with the given metadata and set it as active.
     * @param metadata Metadata of the new corpus.
     */
    function create(metadata: MutableCorpusMetadata): void {
        plausible.newCorpus(metadata)
        API.postCorpus(metadata)
            // Automatically set the new corpus as active.
            .then(response => {
                activeUUID.value = response.data
            })
            .catch(error => errors.handle(error))
            .finally(reload)
    }

    /**
     * Delete and unselect corpus.
     * @param metadata Corpus to delete.
     */
    function remove(metadata: CorpusMetadata): void {
        plausible.corpusDeleted()
        API.deleteCorpus(metadata.uuid)
            .then(() => {
                // Deselect now deleted corpus
                if (metadata.uuid === activeUUID.value) {
                    activeUUID.value = undefined
                }
            })
            .catch(error => errors.handle(error))
            .finally(reload)
    }

    /**
     * Update metadata of existing corpus. Keeps it selected.
     * @param uuid UUID of corpus to update.
     * @param metadata Updated metadata.
     */
    function update(uuid: UUID, metadata: MutableCorpusMetadata): void {
        plausible.corpusUpdated(metadata)
        API.patchCorpus(uuid, metadata)
            .catch(error => errors.handle(error))
            .finally(reload)
    }

    // Exports
    return {
        // Fields
        allCorpora,
        loading,
        datasets,
        sharedCorpora,
        activeCorpus,
        hasDocs,
        activeUUID,
        isCollaborator,
        isOwner,
        // Methods
        reload,
        create,
        remove,
        update
    }
})

export default useCorpora

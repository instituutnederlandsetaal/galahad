import * as API from "@/api/corpora"
import { plausible } from "@/ts/plausible"
import stores from "@/stores"
import type { CorpusMetadata, MutableCorpusMetadata, UUID } from "@/types/corpora"
import { useRouteQuery } from "@vueuse/router"

/** Contains all corpora for which the user has read access. */
const useCorpora = defineStore("corpora", () => {
    // Stores
    const user = stores.useUser()

    // Fields
    const corpusId = useRouteQuery<UUID>("corpus")
    const loading = ref<boolean>(false)
    const corpora = ref<CorpusMetadata[]>([])
    const datasets = computed<CorpusMetadata[]>((): CorpusMetadata[] => corpora.value?.filter((i) => i.dataset) ?? [])
    const sharedCorpora = computed<CorpusMetadata[]>(
        (): CorpusMetadata[] => corpora.value?.filter((i) => !i.dataset && i.owner !== user.user?.id) ?? [],
    )
    const corpus = computed<CorpusMetadata>(
        (): CorpusMetadata => corpora.value?.find((i) => i.uuid === corpusId.value) as CorpusMetadata,
    )
    const isCollaborator = computed((): boolean => corpus.value?.collaborators.includes(user.user?.id) ?? false)
    const isOwner = computed<boolean>((): boolean => corpus.value?.owner === user.user?.id)

    /** Reload all corpora. */
    function reload(): void {
        loading.value = true
        API.getCorpora()
            .then((res) => (corpora.value = res.data))
            .finally(() => (loading.value = false))
    }

    /** Create a new corpus with the given metadata and set it as active. */
    function create(metadata: MutableCorpusMetadata): void {
        plausible.corpusCreated(metadata)
        API.postCorpus(metadata)
            .then((res) => (corpusId.value = res.data))
            .finally(reload)
    }

    /** Delete and unselect corpus. */
    function remove(metadata: CorpusMetadata): void {
        plausible.corpusDeleted(metadata)
        API.deleteCorpus(metadata.uuid)
            .then(() => (corpusId.value = undefined))
            .finally(reload)
    }

    /** Update metadata of existing corpus. */
    function update(metadata: CorpusMetadata): void {
        plausible.corpusUpdated(metadata)
        API.patchCorpus(metadata.uuid, metadata).finally(reload)
    }

    return {
        corpora,
        loading,
        datasets,
        sharedCorpora,
        corpus,
        corpusId,
        isCollaborator,
        isOwner,
        reload,
        create,
        remove,
        update,
    }
})

export default useCorpora

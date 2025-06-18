import { jobsEntitiesPath } from "@/api/evaluation"
import { useAxios } from "@/api/useAxios"
import stores from "@/stores"
import type { JobsEntities } from "@/types/evaluation/entities"

const useEntities = defineStore("entities", () => {
    const { corpusId } = storeToRefs(stores.useCorpora())
    const { loading, data: entities } = useAxios<JobsEntities>(() =>
        jobsEntitiesPath(corpusId.value)
    )

    return {
        entities,
        loading
    }
})

export default useEntities

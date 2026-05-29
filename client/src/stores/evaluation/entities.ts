import { jobsEntitiesPath } from "@/api/evaluation"
import { useAxios } from "@/api/useAxios"
import type { JobsEntities } from "@/types/evaluation/entities"
import useCorpora from "@/stores/corpora"

const useEntities = defineStore("entities", () => {
    const { corpusId } = storeToRefs(useCorpora())
    const { loading, data: entities } = useAxios<JobsEntities>(() => jobsEntitiesPath(corpusId.value))

    return { entities, loading }
})

export default useEntities

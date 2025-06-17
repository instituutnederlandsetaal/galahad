import { jobsEntitiesPath } from "@/api/evaluation"
import { useAxios } from "@/api/useAxios"
import stores from "@/stores"

const useEntities = defineStore("entities", () => {
    const { corpusId } = storeToRefs(stores.useCorpora())
    const { loading, data: entities } = useAxios(() =>
        jobsEntitiesPath(corpusId.value)
    )

    return {
        entities,
        loading
    }
})

export default useEntities

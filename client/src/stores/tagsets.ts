import { tagsetsPath } from "@/api/tagset"
import { useAxios } from "@/api/useAxios"

/** Stores available tagsets. */
const useTagsets = defineStore("tagsets", () => {
    const { data: tagsets, loading } = useAxios(tagsetsPath)
    return { loading, tagsets }
})

export default useTagsets

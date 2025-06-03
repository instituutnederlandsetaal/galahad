import { getTagsets } from "@/api/tagset"
import { useFetch } from "@/ts/useFetch"

/**
 * Stores all available tagsets. Mainly informational.
 */
const useTagsets = defineStore("tagsets", () => {
    const { data: tagsets, loading } = useFetch(() => getTagsets())
    return { loading, tagsets }
})

export default useTagsets

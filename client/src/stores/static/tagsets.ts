import { endpoints } from "@/api"
import { useAxios } from "@/api/useAxios"
import type { Tagset } from "@/types/tagset"

/** Stores available tagsets. */
const useTagsets = defineStore("tagsets", () => {
    const { data: tagsets, loading } = useAxios<Tagset[]>(endpoints.tagsets(), [])
    return { loading, tagsets }
})

export default useTagsets

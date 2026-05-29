import { endpoints } from "@/api"
import { useAxios } from "@/api/useAxios"
import type { Tagger } from "@/types/taggers"

/** Stores available taggers. */
const useTaggers = defineStore("taggers", () => {
    const { data: taggers, loading } = useAxios<Tagger[]>(endpoints.taggers.base(), [])
    return { loading, taggers }
})

export default useTaggers

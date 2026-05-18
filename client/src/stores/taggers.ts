import { taggersPath } from "@/api/taggers"
import { useAxios } from "@/api/useAxios"
import type { Tagger } from "@/types/taggers"

/** Stores available taggers. */
const useTaggers = defineStore("taggers", () => {
    const { data: taggers, loading } = useAxios<Tagger[]>(taggersPath, [])
    return { loading, taggers }
})

export default useTaggers

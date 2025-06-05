import { taggersPath } from "@/api/taggers"
import { useAxios } from "@/api/useAxios"

/** Stores available taggers. */
const useTaggers = defineStore("taggers", () => {
    const { data: taggers, loading } = useAxios(taggersPath)
    return { loading, taggers }
})

export default useTaggers

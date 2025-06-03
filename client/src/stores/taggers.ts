import { getTaggers } from "@/api/taggers"
import { useFetch } from "@/ts/useFetch"

/**
 * Stores all available taggers. Mainly informational.
 */
const useTaggers = defineStore("taggers", () => {
    const { data: taggers, loading } = useFetch(() => getTaggers())
    return { loading, taggers }
})

export default useTaggers

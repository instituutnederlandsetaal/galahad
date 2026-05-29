import type { User } from "@/types/user"
import { useAxios } from "@/api/useAxios"
import { endpoints } from "@/api"

/** User store & permissions checks. */
const useUser = defineStore("user", () => {
    const { data: user } = useAxios<User>(endpoints.user(), { name: "NO USER", admin: false }, {}, true)
    return { user }
})

export default useUser

import { userPath } from "@/api/user"
import stores from "@/stores"
import type { User } from "@/types/user"
import { useAxios } from "@/api/useAxios"

/** User store & permissions checks. */
const useUser = defineStore("user", () => {
    const corpora = stores.useCorpora()

    const { data: user } = useAxios<User>(userPath, { name: "NO USER", admin: false }, {}, true)

    const canWrite = computed<boolean>((): boolean => user.value?.admin || corpora.isOwner || corpora.isCollaborator)
    const canDelete = computed<boolean>((): boolean => user.value?.admin || corpora.isOwner)

    return { user, canWrite, canDelete }
})

export default useUser

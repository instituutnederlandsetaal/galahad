import { userPath } from "@/api/user"
import stores from "@/stores"
import type { User } from "@/types/user"
import { useAxios } from "@/api/useAxios"

/** User store & permissions checks. */
const useUser = defineStore("user", () => {
    const corpora = stores.useCorpora()

    const { data: user } = useAxios<User>(userPath, { id: "NO USER", isAdmin: false }, {}, true)

    const canWrite = computed<boolean>((): boolean => user.value?.isAdmin || corpora.isOwner || corpora.isCollaborator)
    const canDelete = computed<boolean>((): boolean => user.value?.isAdmin || corpora.isOwner)

    return { user, canWrite, canDelete }
})

export default useUser

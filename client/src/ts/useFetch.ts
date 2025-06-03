import type { AxiosResponse } from "axios"
import stores from "@/stores"

export function useFetch<T>(fetch: () => Promise<AxiosResponse<T>>): {
    data: Ref<T | undefined>
    loading: Ref<boolean>
} {
    const errors = stores.useErrors()
    const data = ref<T>()
    const loading = ref<boolean>(false)

    watchEffect(() => {
        fetch()
            .then(res => {
                data.value = res.data
            })
            .catch(err => errors.handle("fetch tagsets", err))
            .finally(() => {
                loading.value = false
            })
    })

    return { data, loading }
}

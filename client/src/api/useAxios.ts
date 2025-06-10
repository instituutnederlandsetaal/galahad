import axios from "axios"
import stores from "@/stores"

export function useAxios<T>(
    urlRef: MaybeRefOrGetter<string | undefined>,
    initial?: T,
    params?: MaybeRefOrGetter<Record<string, string | number | boolean>>,
    pageReloadOnError?: boolean
): {
    data: Ref<T>
    loading: Ref<boolean>
    reload: () => void
} {
    const errors = stores.useErrors()
    const data = ref<T | undefined>(initial)
    const loading = ref<boolean>(false)
    watchEffect(() => {
        execute()
    })

    function execute(): void {
        const url: string | undefined = toValue(urlRef)
        if (url === undefined) {
            data.value = initial
            return
        }

        loading.value = true
        axios
            .get(url, { params: toValue(params) })
            .then(res => {
                data.value = res.data
            })
            .catch(err => {
                errors.handle(err)
                if (pageReloadOnError) {
                    setTimeout(() => location.reload(), 2000)
                }
            })
            .finally(() => {
                loading.value = false
            })
    }

    return { data, loading, reload: execute }
}

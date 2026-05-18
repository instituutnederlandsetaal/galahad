import axios from "axios"
import stores from "@/stores"

export function useAxios<T>(
    urlRef: MaybeRefOrGetter<string | undefined>,
    initial?: T,
    params?: MaybeRefOrGetter<Record<string, string | number | boolean>>,
    pageReloadOnError?: boolean,
): { data: Ref<T>; loading: Ref<boolean>; reload: () => void } {
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

        const controller = new AbortController()
        onWatcherCleanup(() => {
            console.log("Aborting request to", url)
            controller.abort()
        })

        loading.value = true
        axios
            .get(url, { params: toValue(params), signal: controller.signal })
            .then((res) => {
                data.value = res.data
            })
            .catch((err) => {
                if (pageReloadOnError) {
                    setTimeout(() => location.reload(), 5000)
                }
            })
            .finally(() => {
                loading.value = false
            })
    }

    return { data, loading, reload: execute }
}

/**
 * Utils for handling the blobs from some API responses.
 */

import type {ErrorMessage} from "@/api/api"
import type {AppStore} from "@/stores"
// --- libraries ---
import axios from "axios"
// --- types ---
import type {AxiosError, AxiosRequestConfig, AxiosResponse} from "axios"
import {parse} from "content-disposition"

export type BlobResponse = AxiosResponse<Blob>

// --- methods ---
/**
 * Fetch a blob from a path.
 * @param path Request path.
 */
export function getBlob(
    path: string,
    config?: AxiosRequestConfig,
): Promise<BlobResponse> {
    return axios.get(path, {responseType: "blob", ...config})
}

/**
 * Downloads a file from a response object.
 * @param response Response with blob data.
 */
export function browserDownloadResponseFile(response: BlobResponse) {
    // Parse potential UTF8 filename.
    const filename = parse(response.headers["content-disposition"]).parameters
        .filename
    // DOM link.
    const linkEl = document.createElement("a")
    linkEl.href = window.URL.createObjectURL(new Blob([response.data]))
    linkEl.setAttribute("download", filename)
    document.body.appendChild(linkEl)
    linkEl.click()
}

/**
 * Axios does not support multiple responseTypes. When trying to download a blob,
 * we also receive a blob on error instead of json. So first parse to json.
 * https://medium.com/@fakiolinho/handle-blobs-requests-with-axios-the-right-way-bb905bdb1c04
 * @param error Axios error.
 * @param intent Human readable explanation.
 * @param app appStore.
 */
export function handleBlobError(
    error: AxiosError<Blob>,
    intent: string,
    app: AppStore,
) {
    const reader = new FileReader()
    // Setup the onload that fires after reading.
    reader.onload = () => {
        const json = JSON.parse(reader.result as string) as ErrorMessage
        const errObj = {
            response: {
                data: json,
            },
        } as AxiosError<ErrorMessage>
        app.handleServerError(intent, errObj)
    }
    // Now, read.
    reader.readAsText(error.response?.data as Blob)
}

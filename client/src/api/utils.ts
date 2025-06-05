/**
 * Utils for handling the blobs from some API responses.
 */

import axios from "axios"
import type { AxiosError, AxiosRequestConfig, AxiosResponse } from "axios"
import { parse } from "content-disposition"
import type { ErrorMessage } from "@/api"
import type stores from "@/stores"

export type BlobResponse = AxiosResponse<Blob>

/**
 * Fetch a blob from a path.
 * @param path Request path.
 */
export function getBlob(
    path: string,
    config?: AxiosRequestConfig
): Promise<BlobResponse> {
    return axios.get(path, { responseType: "blob", ...config })
}

/**
 * Downloads a file from a response object.
 * @param response Response with blob data.
 */
export function browserDownloadResponseFile(response: BlobResponse): void {
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
 * @param errors errorStore.
 */
export function handleBlobError(
    error: AxiosError<Blob>,
    intent: string,
    errors: any
): void {
    // If no response, handle the NETWORK_ERROR.
    if (!error.response) errors.handle(error)

    const reader = new FileReader()
    // Setup the onload that fires after reading.
    reader.onload = (): void => {
        const json = JSON.parse(reader.result as string) as ErrorMessage
        const errObj = {
            response: {
                data: json
            }
        } as AxiosError<ErrorMessage>
        errors.handle(errObj)
    }
    // Now, read.
    reader.readAsText(error.response?.data as Blob)
}

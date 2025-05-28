/**
 * Axios config.
 */

import axios from "axios"

export type ErrorMessage = {
    statusCode: string
    message: string
}

/**
 * Set the axios request base to localhost:8010 if running locally,
 * or https://<hostname>/galahad/api/ if running in production.
 */
export function setAxiosBaseUrl(): void {
    axios.defaults.baseURL =
        location.hostname === "localhost"
            ? `${location.protocol}//localhost:8010`
            : `${location.protocol}//${location.hostname}/galahad/api/`
}

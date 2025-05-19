/**
 * Axios config.
 */

// --- libraries ---
import axios from "axios"

// --- methods ---
/**
 * Set the axios request base to localhost:8010 if running locally,
 * or https://<hostname>/galahad/api/ if running in production.
 */
export const setAxiosBaseUrl = () => {
    axios.defaults.baseURL =
        location.hostname == "localhost"
            ? `${location.protocol}//localhost:8010`
            : `${location.protocol}//${location.hostname}/galahad/api/`
}

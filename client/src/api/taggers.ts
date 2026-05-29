/** API calls for fetching taggers and their health status. */

import axios, { type AxiosResponse } from "axios"
import { endpoints } from "@/api"

type TaggerHealthResponse = AxiosResponse<boolean>
type TaggerQueueResponse = AxiosResponse<number>

/** Get tagger health status. */
export function getTaggerHealth(tagger: string): Promise<TaggerHealthResponse> {
    return axios.get(endpoints.taggers.health({ tagger }))
}

/**
 * Get how many docs are currently actively processing.
 * Summed over all taggers & corpora on the server.
 */
export function getQueue(): Promise<TaggerQueueResponse> {
    return axios.get(endpoints.taggers.queue())
}

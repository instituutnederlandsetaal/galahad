/**
 * API calls for fetching taggers and their health status.
 */

import axios, { type AxiosResponse } from "axios"
import type { Tagger, TaggerHealth } from "@/types/taggers"

type TaggersResponse = AxiosResponse<Tagger[]>
type TaggerHealthResponse = AxiosResponse<TaggerHealth>
type TaggersBusyResponse = AxiosResponse<number>

export const taggersPath = "/taggers"

const taggerPath = (tagger: string): string => `${taggersPath}/${tagger}`
const taggerHealthPath = (tagger: string): string =>
    `${taggerPath(tagger)}/health`

/**
 * Get all taggers.
 */
export function getTaggers(): Promise<TaggersResponse> {
    return axios.get(taggersPath)
}

/**
 * Get tagger health status.
 * @param tagger Tagger name.
 */
export function getTaggerHealth(tagger: string): Promise<TaggerHealthResponse> {
    return axios.get(taggerHealthPath(tagger))
}

/**
 * Get how many docs are currently actively processing.
 * Summed over all taggers & corpora on the server.
 */
export function getDocsAtTaggers(): Promise<TaggersBusyResponse> {
    return axios.get(`${taggersPath}/active`)
}

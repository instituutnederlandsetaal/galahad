/**
 * API calls for fetching tagsets.
 */

import axios, { type AxiosResponse } from "axios"
import type { Tagset } from "@/types/tagset"

type TagsetsResponse = AxiosResponse<Tagset[]>

const tagsetsPath = "/tagsets"

/**
 * Get all tagsets.
 */
export function getTagsets(): Promise<TagsetsResponse> {
    return axios.get(tagsetsPath)
}

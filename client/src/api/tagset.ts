/**
 * API calls for fetching tagsets.
 */

import type { Tagset } from "@/types/tagset"
// --- libraries ---
import axios from "axios"
// --- types ---
import type { AxiosResponse } from "axios"

type TagsetsResponse = AxiosResponse<Tagset[]>

// --- data ---
const tagsetsPath = "/tagsets"

// --- methods ---
/**
 * Get all tagsets.
 */
export function getTagsets(): Promise<TagsetsResponse> {
    return axios.get(tagsetsPath)
}

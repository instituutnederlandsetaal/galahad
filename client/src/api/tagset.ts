/**
 * API calls for fetching tagsets.
 */

// --- libraries ---
import axios from "axios"
// --- types ---
import type { AxiosResponse } from "axios"
import type { Tagset } from "@/types/tagset"

type TagsetsResponse = AxiosResponse<Tagset[]>

// --- data ---
const tagsetsPath = `/tagsets`

// --- methods ---
/**
 * Get all tagsets.
 */
export function getTagsets(): Promise<TagsetsResponse> {
	return axios.get(tagsetsPath)
}

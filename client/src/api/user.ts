/**
 * User API
 */

// --- libraries ---
import axios, { type AxiosResponse } from "axios"
// --- types ---
import type { User } from "@/types/user"

type UserResponse = AxiosResponse<User>

// --- data ---
const userPath = `/user`

// --- methods ---
/**
 * Poll user account.
 */
export function getUser(): Promise<UserResponse> {
	return axios.get(userPath)
}

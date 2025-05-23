/**
 * User API
 */

// --- types ---
import type {User} from "@/types/user"
// --- libraries ---
import axios, {type AxiosResponse} from "axios"

type UserResponse = AxiosResponse<User>

// --- data ---
const userPath = "/user"

// --- methods ---
/**
 * Poll user account.
 */
export function getUser(): Promise<UserResponse> {
    return axios.get(userPath)
}

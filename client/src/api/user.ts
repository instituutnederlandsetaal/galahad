/**
 * User API
 */

import axios, { type AxiosResponse } from "axios"
import type { User } from "@/types/user"

type UserResponse = AxiosResponse<User>

const userPath = "/user"

/**
 * Poll user account.
 */
export function getUser(): Promise<UserResponse> {
    return axios.get(userPath)
}

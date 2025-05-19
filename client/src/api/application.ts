// Libraries & stores
import axios, { AxiosResponse } from "axios"
// API & types
import { User } from '@/types/user'

// Paths
const userPath = `/user`

// Custom types
type UserResponse = AxiosResponse<User>

// Public methods
/**
 * Poll user account.
 */
export function getUser(): Promise<UserResponse> {
    return axios.get(userPath)
}

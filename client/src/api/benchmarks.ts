/**
 * API for fetching benchmarks.
 */

// --- libraries ---
import axios from "axios"
// --- types ---
import type { AxiosResponse } from "axios"
import type { Benchmarks } from "@/types/assays"

type BenchmarksResponse = AxiosResponse<Benchmarks>

// --- data ---
const benchmarksPath = "/benchmarks"

// --- methods ---
/**
 * Fetch all benchmarks.
 */
export function getBenchmarks(): Promise<BenchmarksResponse> {
    return axios.get(benchmarksPath)
}

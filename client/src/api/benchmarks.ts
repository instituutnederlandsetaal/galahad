/**
 * API for fetching benchmarks.
 */

import type { Benchmarks } from "@/types/assays"
// --- libraries ---
import axios from "axios"
// --- types ---
import type { AxiosResponse } from "axios"

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

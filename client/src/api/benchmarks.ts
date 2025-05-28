/**
 * API for fetching benchmarks.
 */

import axios, { type AxiosResponse } from "axios"
import type { Benchmarks } from "@/types/assays"

type BenchmarksResponse = AxiosResponse<Benchmarks>

const benchmarksPath = "/benchmarks"

/**
 * Fetch all benchmarks.
 */
export function getBenchmarks(): Promise<BenchmarksResponse> {
    return axios.get(benchmarksPath)
}

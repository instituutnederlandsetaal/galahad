import type { LayerMetadata } from "@/types/layers"

export const SOURCE_LAYER: string = "source"

export type Job = { layer: LayerMetadata; progress: Progress }

export type Progress = {
    untagged: number
    processing: number
    failed: number
    finished: number
    total: number
    errors: { [document: string]: string }
}

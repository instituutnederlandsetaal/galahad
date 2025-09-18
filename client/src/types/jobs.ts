import type { Term } from "@/types/evaluation"
import type { Tagger } from "@/types/taggers"

export const SOURCE_LAYER: string = "sourceLayer"

export type Job = { tagger: Tagger; progress: Progress; preview: LayerPreview; modified: number; annotations: Record<string, number> }

export type Progress = {
    pending: number
    processing: number
    failed: number
    finished: number
    total: number
    untagged: number
    busy: boolean
    hasError: boolean
    errors: { [document: string]: string }
}

export type LayerPreview = { terms: Term[] }

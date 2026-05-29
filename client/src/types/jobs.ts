import type { Tagger } from "@/types/taggers"

export const SOURCE_LAYER: string = "source"

export type Job = { tagger: Tagger; progress: Progress; modified: number }

export type Progress = {
    untagged: number
    processing: number
    failed: number
    finished: number
    total: number
    errors: { [document: string]: string }
}

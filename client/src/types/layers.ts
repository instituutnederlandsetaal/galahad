import type { Tagger } from "@/types/taggers"
import type { Term } from "@/types/evaluation"

export type LayerPreview = { terms: Term[] }

export type LayerMetadata = {
    tagger: Tagger
    preview: LayerPreview
    annotations: Record<string, number>
    modified: number
}

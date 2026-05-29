import type { Tagger } from "@/types/taggers"
import type { Term } from "@/types/evaluation"
import type { AnnotationsSummary } from "@/types/documents"

export type LayerPreview = { terms: Term[] }

export type LayerMetadata = {
    tagger: Tagger
    documents: number
    preview: LayerPreview
    annotations: AnnotationsSummary
    modified: number
}

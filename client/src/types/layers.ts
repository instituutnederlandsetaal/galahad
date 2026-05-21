import type { Tagger } from "@/types/taggers"
import type { LayerPreview } from "@/types/jobs"

export type LayerMetadata = {
    tagger: Tagger;
    preview: LayerPreview;
    annotations: Record<string, number>;
    modified: number
}

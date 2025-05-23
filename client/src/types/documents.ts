import type { LayerPreview, LayerSummary } from "./jobs"

export enum Format {
    Tei_p4_legacy = "tei-p4-legacy",
    Tei_p5_legacy = "tei-p5-legacy",
    Tei_p5 = "tei-p5",
    Naf = "naf",
    Folia = "folia",
    Tsv = "tsv",
    Txt = "txt",
    Conllu = "conllu",
}

export type DocumentMetadata = {
    name: string
    format: Format
    preview: string
    layerPreview: LayerPreview
    layerSummary: LayerSummary
    lastModified: number
}

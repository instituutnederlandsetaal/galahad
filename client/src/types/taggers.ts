export type Tagger = {
    name: string
    description: string
    language: string
    period: Period
    annotations: AnnotationItem[]
    attributions: LinkItem[]
}

export type LinkItem = { name: string; description: string; url: string }
export type AnnotationItem = { annotation: string; principles: LinkItem[] }
export type Period = { from: number; to: number }

export type Tagger = {
    id: string
    version: string
    uri: string
    description: string
    language: string
    period: Period
    annotations: AnnotationItem[]
    attributions: LinkItem[]
}

export type LinkItem = { name: string; details: string; href: string }
export type AnnotationItem = { annotation: string; principles: LinkItem[] }
export type Period = { from: number; to: number }

export enum TaggerStatus {
    HEALTHY = "HEALTHY",
    ERROR = "ERROR",
    NOT_HEALTHY = "NOT_HEALTHY",
    UNKOWN = "UNKOWN",
}

export type TaggerHealth = { status: TaggerStatus; message: string }

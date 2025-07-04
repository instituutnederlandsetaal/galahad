export type Tagger = {
    id: string
    description: string
    tagset: string
    eraFrom: number
    eraTo: number
    annotations: string[]
}

export enum TaggerStatus {
    HEALTHY = "HEALTHY",
    ERROR = "ERROR",
    NOT_HEALTHY = "NOT_HEALTHY",
    UNKOWN = "UNKOWN",
}

export type TaggerHealth = { status: TaggerStatus; message: string }

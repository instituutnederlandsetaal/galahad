import type { Period } from "@/types/taggers"

export type UUID = string

export type CorpusMetadata = MutableCorpusMetadata & {
    activeJobs: number
    numResults: number
    dataset: boolean
    modified: number
    numDocs: number
    public: boolean
    size: number
    uuid: UUID
}

export type Source = { name: string; url: string }

export type MutableCorpusMetadata = {
    name: string
    owner: string
    dataset: boolean
    period: Period
    language: string
    tagset: string
    source: Source
    collaborators: string[]
    viewers: string[]
}

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

export type MutableCorpusMetadata = {
    name: string
    owner: string
    eraFrom: number
    eraTo: number
    tagset: string
    language: string
    dataset: boolean
    public: boolean
    collaborators: string[]
    viewers: string[]
    sourceName: string
    sourceUrl: string
}

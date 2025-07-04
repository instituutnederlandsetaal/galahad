export type Entity = { label: string; form: string; count: number; job: string }

export type DocumentEntities = { entities: Entity[]; summary: Record<string, number>; total: number }

export type JobEntities = {
    documents: Record<string, DocumentEntities>
    summary: Record<string, number>
    total: number
}

export type JobsEntitiesStddev = {
    documents: Record<string, DocumentEntitiesStddev>
    stddev: Record<string, number>
    average: number
}
export type DocumentEntitiesStddev = { stddev: Record<string, number>; average: number }

export type JobsEntities = { jobs: Record<string, JobEntities>; stddev: JobsEntitiesStddev }

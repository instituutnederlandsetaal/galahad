export type Entity = {
    label: string
    form: string
    count: number
}

export type DocumentEntities = {
    entities: Entity[]
    summary: Record<string, number>
    total: number
}

export type JobEntities = {
    documents: Record<string, DocumentEntities>
    summary: Record<string, number>
    total: number  
}

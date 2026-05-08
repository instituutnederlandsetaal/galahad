export const CorpusTableType = { dataset: "benchmark", user: "user", shared: "shared" }
export type CorpusTableType = (typeof CorpusTableType)[keyof typeof CorpusTableType]

export const DocsTableType = { dataset: "dataset", user: "user" }
export type DocsTableType = (typeof DocsTableType)[keyof typeof DocsTableType]

export type Column<T> = {
    key: string
    label?: string
    align?: string
    hidden?: boolean
    noSort?: boolean
    sortOn?: (value: T) => number | string
    format?: (value: T) => number | string
}

export type TableData<T> = { column: Column<T>; item: T; value: unknown }

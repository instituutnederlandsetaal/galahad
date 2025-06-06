export enum TableCorporaType {
    Dataset = "benchmark",
    User = "user",
    Shared = "shared"
}

export enum TableDocumentsType {
    Dataset = "dataset",
    User = "user"
}

export interface Item {
    [key: string]: unknown
}

export type Column<T> = {
    key: string
    label?: string
    noSort?: boolean
    sortOn?: (value: T) => number | string
    align?: string
    hidden?: boolean
    format?: (data: TableData<T>) => string
}

export type TableData<T> = {
    column: Column<T>
    item: T
    value: unknown
}

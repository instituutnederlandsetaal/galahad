export const TableCorporaType = {
    dataset: "benchmark",
    user: "user",
    shared: "shared"
}
export type TableCorporaType =
    (typeof TableCorporaType)[keyof typeof TableCorporaType]

export const TableDocumentsType = {
    dataset: "dataset",
    user: "user"
}
export type TableDocumentsType =
    (typeof TableDocumentsType)[keyof typeof TableDocumentsType]

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
    format?: (value: T) => number | string
}

export type TableData<T> = {
    column: Column<T>
    item: T
    value: unknown
}

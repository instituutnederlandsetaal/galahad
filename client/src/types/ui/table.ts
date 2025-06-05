export enum TableCorporaType {
    Dataset = "benchmark",
    User = "user",
    Shared = "shared"
}

export enum TableDocumentsType {
    Dataset = "dataset",
    User = "user"
}

/* sortOn defines what to sort field values on */
export type Column = {
    key: string
    label?: string
    sortOn?: (value: any) => any
    align?: string
    hidden?: boolean
    format?: (data: TableData<any>) => string
}

export type TableData<T> = {
    field: Column
    item: T
    value: any
}

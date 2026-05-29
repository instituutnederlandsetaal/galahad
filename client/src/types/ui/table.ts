export const CorporaTableType = { dataset: "benchmark", user: "user", shared: "shared" }
export type CorporaTableType = (typeof CorporaTableType)[keyof typeof CorporaTableType]

export type Column<T> = {
    key: string
    label?: string
    align?: string
    hidden?: boolean
    noSort?: boolean
    sortOn?: (value: T) => number | string | undefined
    format?: (value: T) => number | string | undefined
}

export type TableData<T> = { column: Column<T>; item: T; value: unknown }

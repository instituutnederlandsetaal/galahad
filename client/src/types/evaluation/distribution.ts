export type Distribution = Record<string, TypeToken[]>
export type TypeToken = { annotation: string; group: string; count: number; tokens: Record<string, number> }

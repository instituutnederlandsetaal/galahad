export type Distribution = Record<string, TypeToken[]>
export type TypeToken = { lemma: string; group: string; count: number; tokens: Record<string, number> }

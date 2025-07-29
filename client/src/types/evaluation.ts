// Confusion
export type ConfusionWrapper = {
    matrix: { [key: string]: { count: number; samples: TermComparison[] } }
    table: { [key: string]: {} }
    generated: number
    hypothesisLastModified: number
    referenceLastModified: number
}

// Metrics
export type Metrics = {
    global: MetricsRow
    perPOS: MetricsRow[]
    generated: number
    hypothesisLastModified: number
    referenceLastModified: number
}

export type MetricsRow = {
    name: string
    count: number
    bothAgree: MetricEntry
    lemmaAgree: MetricEntry
    lemmaDisagree: MetricEntry
    posAgree: MetricEntry
    posDisagree: MetricEntry
    noMatch: MetricEntry
}

export type MetricEntry = { count: number; samples: TermComparison[] }

// Shared
export type TermComparison = {
    hypoTerm: Term
    refTerm: Term
    equalLemma: boolean
    equalPOS: boolean
    equalLabel: boolean
    fullOverlap: boolean
    partialOverlap: boolean
}

export type Term = { id: string; offset: number; annotations: Record<string, string>; spaceAfter?: boolean }

export type WordForm = { literal: string; offset: number; length: number; id: null | string }

export type EvaluationEntry = { count: number; samples: TermComparison[] }

type Samples = { title: string; samples: TermComparison[] }

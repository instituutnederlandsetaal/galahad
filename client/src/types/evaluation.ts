// Confusion
export type Confusion = Record<string, Record<string, EvaluationEntry>>

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
export type TermComparison = { hyp: Term; ref: Term }

export type Term = { id: string; offset: number; annotations: Record<string, string>; spaceAfter?: boolean }

export type WordForm = { literal: string; offset: number; length: number; id: null | string }

export type EvaluationEntry = { count: number; samples: TermComparison[] }

export type Samples = { title: string; samples: TermComparison[] }

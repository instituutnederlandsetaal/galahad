import type { CorpusMetadata, MutableCorpusMetadata } from "@/types/corpora"
import type { DocumentMetadata, Format } from "@/types/documents"
import type { Job } from "@/types/jobs"

declare global {
    interface Window {
        plausible: (eventName: string, props?: Record<string, any>) => void
    }
}

enum JobType {
    Hypothesis = "hypothesis",
    Reference = "reference",
    Tagger = "tagger",
}

// debug printing for plausible
if (location.hostname.includes("localhost")) {
    window.plausible = (eventName: string, props?: Record<string, any>): void =>
        console.log(`localhost plausible event: ${eventName}\nparams: ${JSON.stringify(props)}`)
}

function corpusParams(corpus: CorpusMetadata): Record<string, number | string | boolean> {
    return {
        shared: corpus.dataset ? "dataset" : (corpus.collaborators.length + corpus.viewers.length),
        period: `${corpus.eraFrom} - ${corpus.eraTo}`,
        source: Boolean(corpus.sourceName) || Boolean(corpus.sourceUrl),
        numDocs: corpus.numDocs,
    }
}

function docParams(doc: DocumentMetadata): Record<string, string> {
    return { format: doc.format, annotations: doc.annotations.join() }
}

function jobParams(job: Job, type: JobType): Record<string, any> {
    return {
        [`${type}-id`]: job.tagger.id,
        [`${type}-annotations`]: job.tagger.annotations.map((a) => a.annotation),
    }
}

function corpusDocParams(corpus: CorpusMetadata, doc: DocumentMetadata): Record<string, string | number | boolean> {
    return { ...docParams(doc), ...corpusParams(corpus) }
}

export const plausible = {
    corpusCreated(corpus: CorpusMetadata): void {
        window.plausible("corpus-created", { props: corpusParams(corpus) })
    },
    corpusDeleted(corpus: CorpusMetadata): void {
        window.plausible("corpus-deleted", { props: corpusParams(corpus) })
    },
    corpusUpdated(corpus: CorpusMetadata): void {
        window.plausible("corpus-updated", { props: corpusParams(corpus) })
    },
    documentDownloaded(corpus: CorpusMetadata, doc: DocumentMetadata): void {
        window.plausible("document-downloaded", { props: corpusDocParams(corpus, doc) })
    },
    documentDeleted(corpus: CorpusMetadata, doc: DocumentMetadata): void {
        window.plausible("document-deleted", { props: corpusDocParams(corpus, doc) })
    },
    documentUploaded(corpus: CorpusMetadata, fileExtension: string): void {
        const props = { format: fileExtension, ...corpusParams(corpus) }
        window.plausible("document-uploaded", { props })
    },
    corpusExported(corpus: CorpusMetadata, layer: string, format: Format, merged: boolean, headOnly: boolean): void {
        const props = { layer, format, merged, headOnly, ...corpusParams(corpus) }
        window.plausible("corpus-exported", { props })
    },
    helpClicked(): void {
        const props = { url: location.pathname }
        window.plausible("help-clicked", { props })
    },
    distributionEvaluated(corpus: CorpusMetadata, hypothesisJob: Job): void {
        const props = { ...jobParams(hypothesisJob, JobType.Hypothesis), ...corpusParams(corpus) }
        window.plausible("distribution-evaluated", { props })
    },
    confusionEvaluated(corpus: CorpusMetadata, hypothesisJob: Job, referenceJob: Job): void {
        const props = {
            ...jobParams(hypothesisJob, JobType.Hypothesis),
            ...jobParams(referenceJob, JobType.Reference),
            ...corpusParams(corpus),
        }
        window.plausible("confusion-evaluated", { props })
    },
    metricsEvaluated(corpus: CorpusMetadata, hypothesisJob: Job, referenceJob: Job): void {
        const props = {
            ...jobParams(hypothesisJob, JobType.Hypothesis),
            ...jobParams(referenceJob, JobType.Reference),
            ...corpusParams(corpus),
        }
        window.plausible("metrics-evaluated", { props })
    },
    evaluationDownloaded(corpus: CorpusMetadata, hypothesisJob: Job, referenceJob: Job): void {
        const props = {
            ...jobParams(hypothesisJob, JobType.Hypothesis),
            ...jobParams(referenceJob, JobType.Reference),
            ...corpusParams(corpus),
        }
        window.plausible("evaluation-downloaded", { props })
    },
    jobStarted(corpus: CorpusMetadata, taggerJob: Job): void {
        const props = { ...jobParams(taggerJob, JobType.Tagger), ...corpusParams(corpus) }
        window.plausible("job-started", { props })
    },
    jobDeleted(corpus: CorpusMetadata, taggerJob: Job): void {
        const props = { ...jobParams(taggerJob, JobType.Tagger), ...corpusParams(corpus) }
        window.plausible("job-deleted", { props })
    },
    jobStopped(corpus: CorpusMetadata, taggerJob: Job): void {
        const props = { ...jobParams(taggerJob, JobType.Tagger), ...corpusParams(corpus) }
        window.plausible("job-stopped", { props })
    }
}

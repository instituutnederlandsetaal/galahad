import type { CorpusMetadata, MutableCorpusMetadata } from "@/types/corpora"
import type { DocumentMetadata, Format } from "@/types/documents"

declare global {
    interface Window {
        plausible: (eventName: string, props?: Record<string, any>) => void
    }
}

// debug printing for plausible
if (location.hostname.includes("localhost")) {
    window.plausible = (eventName: string, props?: Record<string, any>): void =>
        console.log(
            `localhost plausible event: ${eventName}\nparams: ${JSON.stringify(props)}`
        )
}

function corpusParams(
    corpus: CorpusMetadata
): Record<string, number | string | boolean> {
    return {
        shared: corpus.collaborators.length + corpus.viewers.length,
        tagset: corpus.tagset,
        period: `${corpus.eraFrom} - ${corpus.eraTo}`,
        source: Boolean(corpus.sourceName) || Boolean(corpus.sourceUrl),
        dataset: corpus.dataset,
        numDocs: corpus.numDocs
    }
}

function docParams(doc: DocumentMetadata): Record<string, string> {
    return {
        format: doc.format,
        annotations: doc.annotations.join()
    }
}

function corpusDocParams(
    corpus: CorpusMetadata,
    doc: DocumentMetadata
): Record<string, string | number | boolean> {
    return {
        ...docParams(doc),
        ...corpusParams(corpus)
    }
}

export const plausible = {
    corpusCreated(corpus: CorpusMetadata): void {
        window.plausible("corpus-created", corpusParams(corpus))
    },
    corpusDeleted(corpus: CorpusMetadata): void {
        window.plausible("corpus-deleted", corpusParams(corpus))
    },
    corpusUpdated(corpus: CorpusMetadata): void {
        window.plausible("corpus-updated", corpusParams(corpus))
    },
    documentDownloaded(corpus: CorpusMetadata, doc: DocumentMetadata): void {
        window.plausible("document-downloaded", corpusDocParams(corpus, doc))
    },
    documentDeleted(corpus: CorpusMetadata, doc: DocumentMetadata): void {
        window.plausible("document-deleted", corpusDocParams(corpus, doc))
    },
    documentUploaded(corpus: CorpusMetadata, fileExtension: string): void {
        const params = {
            format: fileExtension,
            ...corpusParams(corpus)
        }
        window.plausible("document-uploaded", params)
    },
    corpusExported(
        corpus: CorpusMetadata,
        layer: string,
        format: Format,
        merged: boolean,
        headOnly: boolean
    ): void {
        const params = {
            layer,
            format,
            merged,
            headOnly,
            ...corpusParams(corpus)
        }
        window.plausible("corpus-exported", params)
    },
    helpClicked(): void {
        window.plausible("help-clicked", { url: location.pathname })
    }
}

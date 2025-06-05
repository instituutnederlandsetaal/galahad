import type { MutableCorpusMetadata } from "@/types/corpora"

declare global {
    interface Window {
        plausible: (eventName: string, props?: Record<string, any>) => void
    }
}

function corpusShared(metadata: MutableCorpusMetadata): boolean {
    return metadata.collaborators.length > 0 || metadata.viewers.length > 0
}

export const plausible = {
    newCorpus(metadata: MutableCorpusMetadata): void {
        const params = { shared: corpusShared(metadata) }
        window.plausible("corpus-created", params)
    },
    corpusDeleted(): void {
        window.plausible("corpus-deleted")
    },
    corpusUpdated(metadata: MutableCorpusMetadata): void {
        const params = { shared: corpusShared(metadata) }
        window.plausible("corpus-updated", params)
    }
}

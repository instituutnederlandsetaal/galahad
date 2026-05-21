package org.ivdnt.galahad.web.controller

import org.ivdnt.galahad.web.controller.Endpoints.Jobs.JOB

object Endpoints {
    const val BASE: String = "/"
    const val VERSION: String = "/version"
    const val BENCHMARKS: String = "/benchmarks"
    const val USER: String = "/user"
    const val SWAGGER: String = "/swagger-ui/index.html"

    object Corpora {
        const val BASE: String = "/corpora"
        const val CORPUS: String = "$BASE/{corpus}"
    }

    object Internal {
        const val BASE: String = "/internal"
        const val JOBS: String = "$BASE/jobs"
        const val RESULT: String = "$JOBS/result"
        const val ERROR: String = "$JOBS/error"
    }

    object Jobs {
        const val BASE: String = "${Corpora.CORPUS}/jobs"
        const val JOB: String = "$BASE/{job}"

        object Documents {
            const val BASE: String = "$JOB/documents"
            const val DOCUMENT: String = "$BASE/{document}"
        }
    }

    object Layers {
        const val BASE: String = "${Corpora.CORPUS}/layers"
        const val LAYER: String = "$BASE/{layer}"

        object Documents {
            const val BASE: String = "$LAYER/documents"
            const val DOCUMENT: String = "$BASE/{document}"
        }
    }

    object Export {
        const val BASE: String = "${Layers.LAYER}/export"
        const val CONVERT: String = "$BASE/convert"
        const val MERGE: String = "$BASE/merge"

        object Documents {
            const val BASE: String = "${Layers.Documents.DOCUMENT}/export"
            const val CONVERT: String = "$BASE/convert"
            const val MERGE: String = "$BASE/merge"
        }
    }

    object Documents {
        const val BASE: String = "${Corpora.CORPUS}/documents"
        const val DOCUMENT: String = "$BASE/{document}"
        const val DOWNLOAD: String = "$DOCUMENT/download"
    }

    object Taggers {
        const val BASE: String = "/taggers"
        const val TAGGER: String = "$BASE/{tagger}"
        const val QUEUE: String = "$BASE/queue"
        const val HEALTH: String = "$TAGGER/health"
    }

    object Tagsets {
        const val BASE: String = "/tagsets"
        const val TAGSET: String = "$BASE/{tagset}"
    }

    object Evaluation {
        const val BASE: String = "${Corpora.CORPUS}/evaluation"
        const val DOWNLOAD: String = "$BASE/download"
        const val ENTITIES: String = "$BASE/entities"
        const val METRICS: String = "$BASE/metrics"
        const val METRICS_DOWNLOAD: String = "$METRICS/download"
        const val CONFUSION: String = "$BASE/confusion"
        const val CONFUSION_DOWNLOAD: String = "$CONFUSION/download"
        const val DISTRIBUTION: String = "$BASE/distribution"
        const val DISTRIBUTION_DOWNLOAD: String = "$DISTRIBUTION/download"

        object Document {
            const val BASE: String = "${Documents.DOCUMENT}/evaluation"
            const val ENTITIES: String = "$BASE/entities"
            const val DISTRIBUTION: String = "$BASE/distribution"
            const val COMPARISON: String = "$BASE/comparison"
            const val METRICS: String = "$BASE/metrics"
            const val SPANS: String = "$BASE/spans"
        }
    }
}

package org.ivdnt.galahad.web.controller

object Endpoints {
    const val BASE = "/"
    const val TAGSETS = "/tagsets"
    const val VERSION = "/version"
    const val BENCHMARKS = "/benchmarks"
    const val USER = "/user"
    const val SWAGGER = "/swagger-ui/index.html"

    object Corpora {
        const val BASE = "/corpora"
        const val CORPUS = "$BASE/{corpus}"
    }

    object Internal {
        const val BASE = "/internal"
        const val JOBS = "$BASE/jobs"
        const val RESULT = "$JOBS/result"
        const val ERROR = "$JOBS/error"
    }

    object Jobs {
        const val BASE = "${Corpora.CORPUS}/jobs"
        const val JOB = "$BASE/{job}"
        const val CANCEL = "$JOB/cancel"
        const val PROGRESS = "$JOB/progress"

        object Documents {
            const val BASE = "$JOB/documents"
            const val DOCUMENT = "$BASE/{document}"
        }
    }

    object Export {
        const val BASE = "${Jobs.JOB}/export"
        const val CONVERT = "$BASE/convert"
        const val MERGE = "$BASE/merge"

        object Documents {
            const val BASE = "${Jobs.Documents.DOCUMENT}/export"
            const val CONVERT = "$BASE/convert"
            const val MERGE = "$BASE/merge"
        }
    }

    object Documents {
        const val BASE = "${Corpora.CORPUS}/documents"
        const val DOCUMENT = "$BASE/{document}"
        const val DOWNLOAD = "$DOCUMENT/download"
    }

    object Taggers {
        const val BASE = "/taggers"
        const val TAGGER = "$BASE/{tagger}"
        const val HEALTH = "$TAGGER/health"
        const val QUEUE = "$BASE/queue"
    }

    object Evaluation {
        const val BASE = "${Corpora.CORPUS}/evaluation"
        const val ENTITIES = "$BASE/entities"
        const val METRICS = "$BASE/metrics"
        const val METRICS_SAMPLES = "$METRICS/download"
        const val CONFUSION = "$BASE/confusion"
        const val CONFUSION_SAMPLES = "$CONFUSION/download"
        const val DISTRIBUTION = "$BASE/distribution"
        const val DOWNLOAD = "$BASE/download"

        object Document {
            const val BASE = "${Documents.DOCUMENT}/evaluation"
            const val COMPARISON = "$BASE/comparison"
        }
    }
}
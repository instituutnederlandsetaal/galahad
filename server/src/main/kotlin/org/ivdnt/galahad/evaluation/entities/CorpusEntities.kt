package org.ivdnt.galahad.evaluation.entities

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.JobPair
import org.ivdnt.galahad.evaluation.CorpusEvaluation
import kotlin.math.pow
import kotlin.math.sqrt

class CorpusEntities(
    val jobs: Map<String, JobEntities>,
    val stddev: JobsEntitiesStddev,
) {
    class JobsEntitiesStddev(
        val documents: Map<String, DocumentEntitiesStddev>,
        val stddev: Map<String, Double>,
        val average: Double,
    )
    class DocumentEntitiesStddev(
        val stddev: Map<String, Double>,
        val average: Double,
    )

    companion object {
        fun create(corpus: Corpus, jobsEvals: CorpusEvaluation): CorpusEntities {
            val jobs: Sequence<JobPair> = corpus.jobs.readAllSequence().map { JobPair(it.name) }
            val jobEntities: Map<String, JobEntities> = jobs.associateWith { job ->
                jobsEvals.createOrThrow(job).entities
            }.mapKeys { it.key.reference }
            // Next, we're going to create standard deviations.
            // For each document, we will need a sequence of the .summary throughout the jobs.

            // Map <document name (string), Map < NER-label (string), standard deviation (float) > >
            val docstddevs: Map<String, DocumentEntitiesStddev> = corpus.documents.readAllSequence().map{ it.name }.associateWith { doc ->
                // example summary: { "PER": 42, "ORG": 6 }
                val counts: Map<String, List<Int>> = buildMap {
                    val summaries = jobEntities.values.map { it.documents[doc]?.summary ?: emptyMap() }
                    summaries.forEach {
                        it.keys.forEach { putIfAbsent(it, emptyList()) }
                    }
                    this.keys.forEach { key ->
                        summaries.forEach { summary ->
                            val count = summary[key] ?: 0
                            computeIfPresent(key) { _, values -> values + count }
                        }
                    }
                }
               val stddev = counts.mapValues { (_, values) ->
                    if (values.size < 2) {
                        0.0 // Not enough data to calculate standard deviation
                    } else {
                        val mean = values.average()
                        sqrt(values.sumOf { (it - mean).pow(2) } / (values.size - 1))
                    }
                }
                DocumentEntitiesStddev(stddev, stddev.values.average().takeIf { !it.isNaN() } ?: 0.0)
            }

            val labelAvg: Map<String, Double> = docstddevs.values.flatMap { it.stddev.keys }.distinct().associateWith { label ->
                docstddevs.values.map { it.stddev[label] ?: 0.0 }.average()
            }

            val avg = labelAvg.values.average()

            val jobstddev = JobsEntitiesStddev(docstddevs, labelAvg, avg)
            return CorpusEntities(jobEntities, jobstddev)
        }
    }
}
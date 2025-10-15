package org.ivdnt.galahad.evaluation.distribution

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.DocumentEvaluations
import org.ivdnt.galahad.export.csv.CsvFile
import org.ivdnt.galahad.export.csv.CsvString
import org.ivdnt.galahad.util.merge

/**
 * The frequency distribution of terms in a corpus for a specific tagger layer.
 * A CorpusDistribution is the sum of the [DocumentDistribution]s of all documents in the corpus.
 */
class JobDistribution(
    @JsonValue val typeTokens: Map<Annotation, List<TypeToken>>
) {
    companion object {
        fun create(corpus: Corpus, docEvals: DocumentEvaluations): JobDistribution = JobDistribution(
            corpus.documents.readAllSequence().map { docEvals.createOrThrow(it.name).distribution.typeTokens }
                .reduce { map1, map2 ->
                    map1.merge(map2, { v1, v2 ->
                        (v1 + v2).groupingBy { it.lemma to it.group }.reduce { _, a, b ->
                            TypeToken(
                                lemma = a.lemma, group = a.group, tokens = a.tokens.merge(b.tokens, Integer::sum)
                            )
                        }.values.sortedByDescending { it.count }
                    })
                })

        fun toCsv(typeTokens: List<TypeToken>): CsvString = buildString {
            append(CsvFile.toCsvString(listOf("lemma", "group", "count", "unique", "tokens")))
            for (tt in typeTokens) {
                append(
                    CsvFile.toCsvString(
                        listOf(
                    tt.lemma,
                    tt.group,
                    tt.count,
                    tt.tokens.size,
                    tt.tokens.entries.joinToString { "${it.key} (${it.value})" })))
            }
        }
    }
}


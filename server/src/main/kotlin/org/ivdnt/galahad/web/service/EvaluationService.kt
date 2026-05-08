package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.CorpusStatistics
import org.ivdnt.galahad.evaluation.JobPair
import org.ivdnt.galahad.evaluation.comparison.*
import org.ivdnt.galahad.evaluation.confusion.JobConfusion
import org.ivdnt.galahad.evaluation.distribution.DocumentDistribution
import org.ivdnt.galahad.evaluation.distribution.JobDistribution
import org.ivdnt.galahad.evaluation.entities.CorpusEntities
import org.ivdnt.galahad.evaluation.metrics.*
import org.ivdnt.galahad.exceptions.InvalidMetricsTypeException
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.util.setContentDisposition
import org.ivdnt.galahad.util.toValidFileName
import org.ivdnt.galahad.util.zipDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.createTempDirectory

@Service
class EvaluationService(private val corpora: CorporaService) {
    fun getConfusionSamples(
        hypoFilter: String,
        refFilter: String,
        annotation: Annotation,
        corpus: UUID,
        job: String,
        reference: String,
    ): ByteArray {
        // Ensure the job has the required annotation types.
        //        if (annotation !in annotationTypesForTagger(job, corpus)) {
        //            throw AnnotationNotSupported(job, annotation)
        //        }
        val filter =
            ConfusionLayerFilter(
                HeadGroupTermFilter(annotation, hypoFilter),
                HeadGroupTermFilter(annotation, refFilter),
            )
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(job, filter = filter))
        val fileName = "confusion-${refFilter}-${hypoFilter}.csv"
        val csv = jobEval.confusion.confusion
        csv.values.first()
        return samplesToZip(corpus, job, reference, "a,b,c", fileName)
    }

//    fun getMetrics(corpus: UUID, job: String, reference: String?): CorpusMetrics {
//        val corpusObj = corpora.readOrThrow(corpus, user)
//        val allAnnots = annotationTypesForTagger(job, corpus)
//        val settings =
//            METRIC_TYPES.filter { it.requiredAnnotations.all { it in allAnnots } }.toMutableList()
//        //        val freq = TokenFrequency(corpusObj, job)
//        //        val freqSettings = settings.map { FrequencyMetricsSettings(freq, it) }
//        //        settings.addAll(freqSettings)
//        val cm =
//            CorpusMetrics(
//                corpusObj,
//                settings = settings,
//                hypothesis = job,
//                reference = if (reference.isNullOrBlank()) SOURCE_LAYER else reference,
//            )
//        return cm
//    }

    fun getMetricsSamples(
        metricsTypeStr: String,
        group: String?,
        corpus: UUID,
        job: String,
        reference: String,
        classType: String,
    ): ByteArray {
        return samplesToZip(corpus, job, reference, "a,b,c", "dummy.csv")
//        val metricsType = METRIC_TYPES.firstOrNull { it.id == metricsTypeStr }
//        if (metricsType == null) {
//            val validMetricsTypes = METRIC_TYPES.map { it.id }
//            throw InvalidMetricsTypeException(
//                "Metrics type $metricsTypeStr does not exist. Valid types are $validMetricsTypes"
//            )
//        }
//        val classType = ClassificationType.fromString(classType)
//
//        val layerFilter =
//            if (group == null) null
//            else {
//                val hypoFilter = HeadGroupTermFilter(metricsType.groupAnnotation, group)
//                val refFilter = HeadGroupTermFilter(metricsType.groupAnnotation, group)
//                MetricsLayerFilter(hypoFilter, refFilter)
//            }
//
//        val cm =
//            CorpusMetrics(
//                corpus = corpora.readOrThrow(corpus),
//                hypothesis = job,
//                reference = reference,
//                layerFilter = layerFilter,
//                truncate = false,
//                settings = listOf(metricsType),
//            )
//        val mt = cm.metricTypes.values.first()
//
//        if (group != null) {
//            val fileName = "metrics-$reference-$job-${classType}-${group}.csv"
//            val csv = mt.samplesToCsv(group, classType)
//            return samplesToZip(corpus, job, reference, csv, fileName)
//        } else {
//            val fileName = "metrics-$reference-$job-${mt.setting.id}-${classType}.csv"
//            val csv = mt.samplesToCsv(classType)
//            return samplesToZip(corpus, job, reference, csv, fileName)
//        }
    }

//    fun getLayerComparison(
//        corpus: UUID,
//        document: String,
//        job: String,
//        reference: String?,
//    ): List<TermComparison> {
//        val reference: String = reference ?: SOURCE_LAYER
//        return LayerComparison(
//                hypothesis =
//                    corpora.readOrThrow(corpus, user).jobs.readOrThrow(job).getLayer(document),
//                reference =
//                    corpora.readOrThrow(corpus, user).jobs.readOrThrow(reference).getLayer(document),
//            )
//            .matches
//    }

    fun getEvaluation(corpus: UUID, job: String, reference: String?): ByteArray {
        val dir: File = createTempDirectory().toFile()
        createDistributionCsv(dir.resolve("distribution"), corpus, job)
        createEntitiesCsv(dir.resolve("entities"), corpus)
        if (reference != null) {
            createMetricsCsv(dir.resolve("metrics"), corpus, job, reference)
            createConfusionCsv(dir.resolve("confusion"), corpus, job, reference)
        }
        val metadata = writeMetadataToDir(corpus, job, reference, dir)
        // zip the directory
        val zipFile = zipDir(dir)
        return zipFile.readBytes()
    }

    private fun createEntitiesCsv(dir: File, corpus: UUID) {
        dir.mkdirs()
        val entities = getCorpusEntities(corpus)
        val file = CsvFile(dir.resolve("entities.csv"))
        file.append(CorpusEntities.toCsv(entities))
    }

    private fun createDistributionCsv(dir: File, corpus: UUID, job: String) {
        dir.mkdirs()
        val distributions = getLayerDistribution(corpus, job)
        distributions.typeTokens.forEach { (annotation, distribution) ->
            val file = CsvFile(dir.resolve("distribution-${annotation.value}.csv"))
            file.append(JobDistribution.toCsv(distribution))
        }
    }

    private fun createConfusionCsv(dir: File, corpus: UUID, hypothesis: String, reference: String) {
        dir.mkdirs()
        val confusions = getJobConfusion(corpus, hypothesis, reference)
        confusions.confusion.forEach { (annotation, confusion) ->
            val file = CsvFile(dir.resolve("confusion-${annotation.value}.csv"))
            file.append(JobConfusion.toCsv(confusion))
        }
    }

    private fun createMetricsCsv(dir: File, corpus: UUID, hypothesis: String, reference: String) {
        dir.mkdirs()
        val metrics = getJobMetric(corpus, hypothesis, reference)
        val globFile = CsvFile(dir.resolve("metrics-global.csv"))
        globFile.append(metrics.toGlobalCsv())

        metrics.classesByGroup.values.forEach { mt ->
            val file = CsvFile(dir.resolve("metrics-${mt.settings.id}.csv"))
            file.append(JobMetric.toCsv(mt))
        }
    }

    private fun writeMetadataToDir(
        corpus: UUID,
        job: String,
        reference: String?,
        dir: File,
    ): CorpusStatistics {
        val metadata = corpora.readOrThrow(corpus).statistics

        val metadataFile = File(dir.resolve("metadata.txt").toURI())
        metadataFile.appendText("Evaluation generated by Galahad\n")
        metadataFile.appendText(
            "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n"
        )
        metadataFile.appendText("Corpus: ${metadata.name}\n")
        metadataFile.appendText("Documents: ${metadata.numDocs}\n")
        metadataFile.appendText("Era: ${metadata.period?.from}-${metadata.period?.to}\n")
        metadataFile.appendText("Hypothesis: $job\n")
        if (reference != null) metadataFile.appendText("Reference: $reference\n")
        return metadata
    }

//    private fun annotationTypesForTagger(job: String, corpus: UUID): Set<Annotation> {
//        val corpusObj = corpora.readOrThrow(corpus, user)
//        return Tagger.readOrThrow(job, corpusObj).annotationSet
//    }

    fun samplesToZip(
        corpus: UUID,
        job: String,
        reference: String?,
        csvBody: String,
        fileName: String,
    ): ByteArray {
        // Create csv file.
        val dir: File = createTempDirectory().toFile()
        val validFileName = fileName.toValidFileName()
        val file = CsvFile(dir.resolve(validFileName))
        file.append(csvBody)
        // Write metadata & create zip
        val metadata = writeMetadataToDir(corpus, job, reference, dir)
        // zip the directory
        return zipDir(dir).readBytes()
    }
//
//    fun getTokenFrequency(corpus: UUID, job: String, reference: String?): CorpusMetrics {
//        val corpusObj = corpora.readOrThrow(corpus, user)
//        val setting =
//            FrequencyMetricsSettings(TokenFrequency(corpusObj, job), LemmaByLemmaMetricsSettings())
//        val settings = listOf(setting)
//        val cm =
//            CorpusMetrics(
//                corpusObj,
//                settings = settings,
//                hypothesis = job,
//                reference = if (reference.isNullOrBlank()) SOURCE_LAYER else reference,
//            )
//        return cm
//    }
//
//    fun getDocumentEntities(corpus: UUID, document: String, job: String): DocumentEntities {
//        val corpus = corpora.readOrThrow(corpus, user)
//        val jobEval = corpus.evaluation.createOrThrow(JobPair(job))
//        val docEval = jobEval.documents.createOrThrow(document)
//        return docEval.entities
//    }
//
//    fun getJobEntities(corpus: UUID, job: String): JobEntities {
//        val corpusObj = corpora.readOrThrow(corpus, user)
//        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(job))
//        return jobEval.entities
//    }

    fun getCorpusEntities(corpus: UUID): CorpusEntities {
        val corpusObj = corpora.readOrThrow(corpus)
        return corpusObj.evaluation.entities
    }

    fun getDocumentDistribution(corpus: UUID, job: String, document: String): DocumentDistribution {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(job))
        val docEval = jobEval.documents.createOrThrow(document)
        return docEval.distribution
    }

    fun getLayerDistribution(corpus: UUID, job: String): JobDistribution {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(job))
        return jobEval.distribution
    }

    fun getJobConfusion(corpus: UUID, hypothesis: String, reference: String): JobConfusion {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
        return jobEval.confusion
    }

//    fun getDocumentMetric(
//        corpus: UUID,
//        document: String,
//        hypothesis: String,
//        reference: String,
//    ): DocumentMetric {
//        val corpusObj = corpora.readOrThrow(corpus, user)
//        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
//        val docEval = jobEval.documents.createOrThrow(document)
//        return docEval.metrics
//    }

    fun getJobMetric(corpus: UUID, hypothesis: String, reference: String): JobMetric {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
        return jobEval.metrics
    }
//
//    fun getDocumentSpanEvaluation(
//        corpus: UUID,
//        document: String,
//        hypothesis: String,
//        reference: String,
//    ): DocumentSpanEvaluation {
//        val corpusObj = corpora.readOrThrow(corpus, user)
//        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
//        val docEval = jobEval.documents.createOrThrow(document)
//        return docEval.spans
//    }

    // TODO duplicate code with export service
    fun getCorpusName(corpus: UUID): String = corpora.readOrThrow(corpus).metadata.name
}

package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.data.corpus.CorpusMetadata
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.evaluation.comparison.*
import org.ivdnt.galahad.evaluation.confusion.CONFUSION_TYPES
import org.ivdnt.galahad.evaluation.confusion.CorpusConfusion
import org.ivdnt.galahad.evaluation.distribution.CorpusDistribution
import org.ivdnt.galahad.evaluation.frequency.TokenFrequency
import org.ivdnt.galahad.evaluation.metrics.*
import org.ivdnt.galahad.exceptions.AnnotationNotSupported
import org.ivdnt.galahad.exceptions.InvalidMetricsTypeException
import org.ivdnt.galahad.formats.csv.CSVFile
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.createZipFile
import org.ivdnt.galahad.util.setContentDisposition
import org.ivdnt.galahad.util.toValidFileName
import org.ivdnt.galahad.web.controller.DISTRIBUTION_MAX_SIZE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.createTempDirectory

@Service
class EvaluationService(val corpora: CorporaService) {
    @Autowired
    private val request: HttpServletRequest? = null

    @Autowired
    private val response: HttpServletResponse? = null

    private val user: User get() = User.fromRequest(request)

    fun getDistribution(
        corpus: UUID,
        job: String,
    ): Map<AnnotationType, CorpusDistribution> {
        val allAnnots = annotationTypesForTagger(job, corpus)
        if (!allAnnots.contains(AnnotationType.LEMMA)) {
            return emptyMap()
        }
        val annotationTypes = CONFUSION_TYPES.filter { allAnnots.contains(it) }
        val distributions = annotationTypes.associateWith {
            CorpusDistribution(
                corpora.readAsReaderOrThrow(corpus, user),
                job,
                it
            ).trim(DISTRIBUTION_MAX_SIZE) as CorpusDistribution
        }
        return distributions
    }

    fun getConfusion(
        corpus: UUID,
        job: String,
        reference: String?,
    ): Map<AnnotationType, CorpusConfusion> {
        val allAnnots = annotationTypesForTagger(job, corpus)
        val annotationTypes = CONFUSION_TYPES.filter { allAnnots.contains(it) }
        val confusions = annotationTypes.associateWith {
            CorpusConfusion(
                corpora.readAsReaderOrThrow(corpus, user),
                hypothesis = job,
                annotation = it,
                reference = if (reference.isNullOrBlank()) SOURCE_LAYER_NAME else reference,
            )
        }
        return confusions
    }

    fun getConfusionSamples(
        hypoFilter: String,
        refFilter: String,
        annotation: String,
        corpus: UUID,
        job: String,
        reference: String,
    ): ByteArray {
        // Ensure the job has the required annotation types.
        val annotationType = AnnotationType.fromString(annotation)
        if (!annotationTypesForTagger(job, corpus).contains(annotationType)) {
            throw AnnotationNotSupported(job, annotationType)
        }
        var layerFilter: ConfusionLayerFilter? = ConfusionLayerFilter(
            HeadGroupTermFilter(annotationType, hypoFilter),
            HeadGroupTermFilter(annotationType, refFilter),
        )
        val cc = CorpusConfusion(
            corpus = corpora.readAsReaderOrThrow(corpus, user),
            hypothesis = job,
            reference = reference,
            layerFilter = layerFilter,
            annotation = AnnotationType.fromString(annotation)
        )
        val fileName = "confusion-${refFilter}-${hypoFilter}.csv"
        val csv = cc.samplesToCSV()
        return samplesToZip(corpus, job, reference, csv, fileName)
    }

    fun getMetrics(
        corpus: UUID,
        job: String,
        reference: String?,
    ): CorpusMetrics {
        val corpusObj = corpora.readAsReaderOrThrow(corpus, user)
        val allAnnots = annotationTypesForTagger(job, corpus)
        val settings = METRIC_TYPES.filter { it.requiredAnnotations.all { allAnnots.contains(it) } }.toMutableList()
        val freq = TokenFrequency(corpusObj, job)
        val freqSettings = settings.map { FrequencyMetricsSettings(freq, it) }
        settings.addAll(freqSettings)
        val cm = CorpusMetrics(
            corpusObj,
            settings = settings,
            hypothesis = job,
            reference = if (reference.isNullOrBlank()) SOURCE_LAYER_NAME else reference
        )
        return cm
    }

    fun getMetricsSamples(
        metricsTypeStr: String,
        group: String?,
        corpus: UUID,
        job: String,
        reference: String,
        classType: String,
    ): ByteArray {
        val metricsType = METRIC_TYPES.firstOrNull { it.id == metricsTypeStr }
        if (metricsType == null) {
            val validMetricsTypes = METRIC_TYPES.map { it.id }
            throw InvalidMetricsTypeException("Metrics type $metricsTypeStr does not exist. Valid types are $validMetricsTypes")
        }
        val classType = ClassificationType.fromString(classType)

        val layerFilter = if (group == null) null else {
            val hypoFilter = HeadGroupTermFilter(metricsType.groupAnnotation, group)
            val refFilter = HeadGroupTermFilter(metricsType.groupAnnotation, group)
            MetricsLayerFilter(hypoFilter, refFilter)
        }

        val cm = CorpusMetrics(
            corpus = corpora.readAsReaderOrThrow(corpus, user),
            hypothesis = job,
            reference = reference,
            layerFilter = layerFilter,
            truncate = false,
            settings = listOf(metricsType),
        )
        val mt = cm.metricTypes.values.first()

        if (group != null) {
            val fileName = "metrics-$reference-$job-${classType}-${group}.csv"
            val csv = mt.samplesToCsv(group, classType)
            return samplesToZip(corpus, job, reference, csv, fileName)
        } else {
            val fileName = "metrics-$reference-$job-${mt.setting.id}-${classType}.csv"
            val csv = mt.samplesToCsv(classType)
            return samplesToZip(corpus, job, reference, csv, fileName)
        }
    }

    fun getDocumentLevelLayerVisualisation(
        corpus: UUID, document: String, job: String, reference: String?,
    ): List<TermComparison> {
        val reference: String = reference ?: SOURCE_LAYER_NAME
        return DocumentLayerComparison(
            hypothesisLayer = corpora.readAsReaderOrThrow(corpus, user).jobs.readOrThrow(job).layer(document),
            referenceLayer = corpora.readAsReaderOrThrow(corpus, user).jobs.readOrThrow(reference).layer(document),
            layerFilter = null
        ).matches
    }


    fun getEvaluation(
        corpus: UUID,
        job: String,
        reference: String?,
    ): ByteArray {
        val dir: File = createTempDirectory("evaluation").toFile()
        createDistributionCsv(dir, corpus, job)
        if (reference != null) {
            createMetricsCsv(dir, corpus, job, reference)
            createConfusionCsv(dir, corpus, job, reference)
        }
        val metadata = writeMetadataToDir(corpus, job, reference, dir)
        response!!.contentType = "application/zip"
        response.setContentDisposition(metadata.name + "-evaluation.zip")

        // zip the directory
        val zipFile = createZipFile(dir.listFiles()!!.asSequence())
        return zipFile.readBytes()
    }

    private fun createDistributionCsv(dir: File, corpus: UUID, job: String) {
        val distributions = getDistribution(corpus, job)
        distributions.forEach { (annotation, distribution) ->
            val file = CSVFile(dir.resolve("distribution-${annotation.value}.csv"))
            file.appendText(distribution.toCSV())
        }
    }

    private fun createConfusionCsv(dir: File, corpus: UUID, job: String, reference: String?) {
        val confusions = getConfusion(corpus, job, reference)
        confusions.forEach { (annotation, confusion) ->
            val file = CSVFile(dir.resolve("confusion-${annotation.value}.csv"))
            file.appendText(confusion.countsToCSV())
        }
    }

    private fun createMetricsCsv(dir: File, corpus: UUID, job: String, reference: String?) {
        val metrics = getMetrics(corpus, job = job, reference = reference)
        val globFile = CSVFile(dir.resolve("metrics-global.csv"))
        globFile.appendText(metrics.toGlobalCsv())

        metrics.metricTypes.values.forEach { mt ->
            val file = CSVFile(dir.resolve("metrics-${mt.setting.id}.csv"))
            file.appendText(mt.toGroupedCsv())
        }
    }

    private fun writeMetadataToDir(
        corpus: UUID, job: String, reference: String?, dir: File,
    ): CorpusMetadata {
        val metadata = corpora.readAsReaderOrThrow(corpus, user).immutableMetadata

        val metadataFile = File(dir.resolve("metadata.txt").toURI())
        metadataFile.appendText("Evaluation generated by Galahad\n")
        metadataFile.appendText("${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n")
        metadataFile.appendText("Corpus: ${metadata.name}\n")
        metadataFile.appendText("Documents: ${metadata.numDocs}\n")
        metadataFile.appendText("Era: ${metadata.eraFrom}-${metadata.eraTo}\n")
        metadataFile.appendText("Hypothesis: $job\n")
        if (reference != null) metadataFile.appendText("Reference: $reference\n")
        return metadata
    }

    private fun annotationTypesForTagger(job: String, corpus: UUID): List<AnnotationType> {
        val corpusObj = corpora.readAsReaderOrThrow(corpus, user)
        return Tagger.readOrThrow(job, corpusObj).annotationTypes
    }

    fun samplesToZip(
        corpus: UUID,
        job: String,
        reference: String?,
        csvBody: String,
        fileName: String,
    ): ByteArray {
        // Create csv file.
        val dir: File = createTempDirectory("samples").toFile()
        val validFileName = fileName.toValidFileName()
        val file = CSVFile(dir.resolve(validFileName))
        file.appendText(csvBody)
        // Write metadata & create zip
        val metadata = writeMetadataToDir(corpus, job, reference, dir)
        val zipFile = createZipFile(dir.listFiles()!!.asSequence())
        // Configure response for zip.
        response!!.contentType = "application/zip"
        response.setContentDisposition(metadata.name + "-evaluation.zip")
        // zip the directory
        return zipFile.readBytes()
    }

    fun getTokenFrequency(corpus: UUID, job: String, reference: String?): CorpusMetrics {
        val corpusObj = corpora.readAsReaderOrThrow(corpus, user)
        val setting = FrequencyMetricsSettings(TokenFrequency(corpusObj, job), LemmaByLemmaMetricsSettings())
        val settings = listOf(setting)
        val cm = CorpusMetrics(
            corpusObj,
            settings = settings,
            hypothesis = job,
            reference = if (reference.isNullOrBlank()) SOURCE_LAYER_NAME else reference
        )
        return cm
    }
}
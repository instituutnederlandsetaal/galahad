package org.ivdnt.galahad.jobs

import java.io.File
import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.files.DiskValue
import org.ivdnt.galahad.files.GalahadFolder

/**
 * Represents a job that processes a single document in a corpus. Corresponds to a directory in
 * jobs/[jobname]/documents/[documentname], containing:
 * - result: a json [Layer]: when not [Layer.Companion.EMPTY], [JobStatus.FINISHED]
 * - processingID: a plaintext [UUID]: when present, [JobStatus.PROCESSING]
 * - error: a plaintext error message: when present, [JobStatus.ERROR]
 */
class JobResult(dir: File) : GalahadFolder(dir), Logging {
    // Files in the document job folder.
    private val processingIDFile = dir.resolve(PROCESSING_ID_File)
    private val errorFile = dir.resolve(ERROR_FILE)
    private val layerFile = dir.resolve(LAYER_FILE)

    // Values in those files.

    var layer: Layer?
        get() = DiskValue<Layer?>(layerFile).readOrNull()
        set(value) {
            if (value == null) throw IllegalArgumentException("Layer cannot be set to null")
            DiskValue<Layer>(layerFile).write(value)
            processingIDFile.delete()
        }

    val isProcessing: Boolean
        get() = processingIDFile.exists() // TODO check if resolving the file does not create it

    var processingID: UUID?
        get() =
            if (processingIDFile.exists()) UUID.fromString(processingIDFile.readText()) else null
        set(value) {
            if (value == null) throw IllegalArgumentException("Processing ID cannot be set to null")
            processingIDFile.writeText(value.toString())
            // If you are processing, we will reset any previous errors
            errorFile.delete()
        }

    var error: String?
        get() = if (errorFile.exists()) errorFile.readText() else null
        set(value) {
            if (value == null) throw IllegalArgumentException("Error cannot be set to null")
            errorFile.writeText(value)
            processingIDFile.delete()
        }

    // /** Determines the status based on the presence of the processing ID, error file, or result
    // file. */
    val status: JobStatus
        get() {
            if (errorFile.exists()) return JobStatus.ERROR
            if (processingIDFile.exists()) return JobStatus.PROCESSING
            if (layer != null) return JobStatus.FINISHED
            return JobStatus.PENDING
        }

    /** Cancels a job by deleting the processing ID. The [status] is updated accordingly. */
    fun cancel() { // TODO should be in DocumentJobs, i.e. Job
        processingIDFile.delete()
    }

    companion object {
        private const val PROCESSING_ID_File = "pid.txt"
        private const val ERROR_FILE = "error.txt"
        private const val LAYER_FILE = "layer.json"
    }
}

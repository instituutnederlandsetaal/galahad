package org.ivdnt.galahad.jobs

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.files.DiskValue
import org.ivdnt.galahad.files.GalahadFolder
import java.io.File
import java.util.*

/**
 * Represents a job that processes a single document in a corpus. Corresponds to a directory in
 * jobs/[jobname]/documents/[documentname], containing:
 * - result: a json [Layer]: when not [Layer.Companion.EMPTY], [JobStatus.FINISHED]
 * - processingID: a plaintext [UUID]: when present, [JobStatus.PROCESSING]
 * - error: a plaintext error message: when present, [JobStatus.ERROR]
 */
class JobResult(dir: File) : GalahadFolder(dir), Logging {

    var error: String?
        get() = DiskValue<String>(dir.resolve(ERROR_FILE)).readOrNull()
        set(value) {
            if (value == null) throw IllegalArgumentException("Error cannot be set to null")
            DiskValue<String>(dir.resolve(ERROR_FILE)).write(value)
        }

    // /** Determines the status based on the presence of the processing ID, error file, or result
    // file. */
    val status: JobStatus
        get() {
            if (error != null) return JobStatus.ERROR
//            if (processingIDFile.exists()) return JobStatus.PROCESSING
            return JobStatus.FINISHED
        }

    companion object {
        private const val ERROR_FILE = "error.txt"
    }
}

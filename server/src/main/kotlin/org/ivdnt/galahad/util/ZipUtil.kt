package org.ivdnt.galahad.util

import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempFile
import org.ivdnt.galahad.util.ThreadPoolUtil.pool

typealias FileMapper = Pair<String, (OutputStream) -> Unit>

fun Sequence<FileMapper>.dedupeFilenames(): Sequence<FileMapper> {
    val seen = mutableMapOf<String, Int>()

    fun nextName(name: String): String {
        val dot = name.lastIndexOf('.')
        val base = if (dot >= 0) name.substring(0, dot) else name
        val ext = if (dot >= 0) name.substring(dot) else ""

        val count = seen.getOrDefault(name, 0)
        if (count == 0) {
            seen[name] = 1
            return name
        }

        var i = count
        var candidate: String
        do {
            i++
            candidate = "$base-${i - 1}$ext"
        } while (candidate in seen)

        seen[name] = i
        seen[candidate] = 1
        return candidate
    }

    return map { (name, writer) -> nextName(name) to writer }
}

/**
 * Create zip file for the given files, optionally to a specific stream. Can be used as a streaming
 * response zip, for when expensive transformations are applied to the Sequence<File>.
 *
 * @param files Sequence of files to be zipped. As it is a Sequence type, you may want to perform
 *   some transformations to map data to a file.
 * @param out If provided, used as a ZipOutputStream.
 * @param includeCMDI include the GaLAHaD CMDI template files in the zip.
 * @return The flushed and closed zipfile.
 */
fun createZipFile(files: Sequence<FileMapper>, out: OutputStream) {
    // Create zip and stream.
    val zipStream = ZipOutputStream(BufferedOutputStream(out))
    // Loop through the Sequence of files
    // Any transformations occur on demand.
    // TODO execute in threadpool
    for (f in files) {
        zipStream.putNextEntry(ZipEntry(f.first))
        f.second(BufferedOutputStream(zipStream))
        zipStream.closeEntry()
    }

    val cmdis = arrayOf("TextProfileINT_GaLAHaD.xml", "TextProfileINT_GaLAHaD.xsd")
    for (cmdi in cmdis) {
        val cmdiFile = getResourceStream(cmdi)
        zipStream.putNextEntry(ZipEntry("metadata/$cmdi"))
        zipStream.write(cmdiFile!!.readBytes())
    }
    // Close
    zipStream.flush()
    zipStream.close()
}

fun zipDir(dir: File): File {
    val zipFile = createTempFile(suffix = ".zip").toFile()
    val stream = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))
    stream.use { zip ->
        dir.walk().forEach { file ->
            if (file.isFile) {
                val entryName = file.relativeTo(dir).path
                zip.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }
    return zipFile
}

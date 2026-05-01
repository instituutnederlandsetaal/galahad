package org.ivdnt.galahad.util

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

typealias FileMapper = Pair<String, (OutputStream) -> Unit>

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
fun createZipFile(
    files: Sequence<FileMapper>,
    out: OutputStream? = null,
    includeCMDI: Boolean = false,
): File {
    // Create zip and stream.
    val zipFile = File.createTempFile("tmp", ".zip")
    val zipStream = ZipOutputStream(BufferedOutputStream(out ?: FileOutputStream(zipFile)))
    // Loop through the Sequence of files
    // Any transformations occur on demand.
    for (f in files) {
        zipStream.putNextEntry(ZipEntry(f.first))
        f.second(BufferedOutputStream(zipStream))
        zipStream.closeEntry()
    }

    if (includeCMDI) {
        val cmdis = arrayOf("TextProfileINT_GaLAHaD.xml", "TextProfileINT_GaLAHaD.xsd")
        for (cmdi in cmdis) {
            val cmdiFile = getResourceStream(cmdi)
            zipStream.putNextEntry(ZipEntry("metadata/$cmdi"))
            zipStream.write(cmdiFile!!.readBytes())
        }
    }
    // Close
    zipStream.flush()
    zipStream.close()
    return zipFile
}

fun zipDir(dir: File): File {
    val zipFile = File.createTempFile("tmp", ".zip")
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

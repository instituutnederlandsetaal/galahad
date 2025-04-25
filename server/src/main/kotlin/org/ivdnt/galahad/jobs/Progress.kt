package org.ivdnt.galahad.jobs

class Progress(
    val pending: Int = 0,
    val processing: Int = 0,
    val failed: Int = 0,
    val finished: Int = 0,
    val errors: Map<String, String> = mapOf(), // Map<doc name, error text>
) {
    // is-prefixes for boolean are removed by the json parser, so do not call this "isBusy".
    val busy: Boolean = processing > 0

    val total: Int = pending + processing + failed + finished

    val untagged: Int = total - finished

    val hasError: Boolean = failed > 0
}
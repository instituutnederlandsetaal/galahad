package org.ivdnt.galahad.jobs

class Progress(
    val untagged: Int = 0,
    val processing: Int = 0,
    val failed: Int = 0,
    val finished: Int = 0,
    val errors: Map<String, String> = mapOf(), // Map<doc name, error text>
) {
    val total: Int = untagged + processing + failed + finished
}

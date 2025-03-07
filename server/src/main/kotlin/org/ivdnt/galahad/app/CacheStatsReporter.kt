package org.ivdnt.galahad.app

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.filesystem.DiskValue
import org.ivdnt.galahad.util.toFixed
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CacheStatsReporter : Logging {
    @Scheduled(fixedDelayString = "\${galahad.logging.cache-period}")
    fun reportStats() {
        val stats = DiskValue.cache.stats()
        val rate = stats.hitRate().toFixed()
        val hits = stats.hitCount()
        val miss = stats.missCount()
        val evictions = stats.evictionCount()
        val entries = DiskValue.cache.estimatedSize()
        logger.info("Cache: [rate: $rate; hits: $hits;  miss: $miss;  evictions: $evictions; entries: $entries]")
    }
}
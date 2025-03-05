package org.ivdnt.galahad.app

import org.springframework.scheduling.annotation.Scheduled
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.filesystem.FileBackedValue
import org.ivdnt.galahad.util.toFixed
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class CacheStatsReporter : Logging {
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    fun reportStats() {
        val stats = FileBackedValue.cache.stats()
        logger.info("Cache hit rate: ${stats.hitRate().toFixed()} [hit: ${stats.hitCount()} miss: ${stats.missCount()} evictions: ${stats.evictionCount()}]")
    }
}
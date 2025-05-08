package org.ivdnt.galahad.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ThreadPoolUtil {
    val pool: ExecutorService = Executors.newFixedThreadPool(100)
}
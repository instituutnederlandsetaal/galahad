package org.ivdnt.galahad.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ThreadPoolUtil {
    val pool: ExecutorService = Executors.newFixedThreadPool(100)
}

fun <A, B> Iterable<A>.parallelMap(f: (A) -> B): List<B> {
    val futures = map { item -> ThreadPoolUtil.pool.submit<B> { f(item) } }
    return futures.map { it.get() }
}

package org.ivdnt.galahad.app

import jakarta.servlet.*
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.kotlin.Logging
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.Duration
import java.time.Instant

@Component
@WebFilter("/*")
class Performance : Filter, Logging {
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig?) {
        // empty
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest, resp: ServletResponse?, chain: FilterChain) {
        val start = Instant.now()
        try {
            chain.doFilter(req, resp)
        } finally {
            var url = (req as HttpServletRequest).requestURI
            if (url != "/user") {
                val finish = Instant.now()
                val time: Long = Duration.between(start, finish).toMillis()
                val method = req.method
                logger.info("in $time ms: $method $url")
            }
        }
    }

    override fun destroy() {
        // empty
    }
}
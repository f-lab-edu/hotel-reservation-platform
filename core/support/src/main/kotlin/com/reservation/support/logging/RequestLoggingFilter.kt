package com.reservation.support.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        // 1. Trace ID 생성
        val traceId = UUID.randomUUID().toString().substring(0, 8)
        MDC.put("traceId", traceId) // 로그에 traceId 자동 삽입
        val startTime = System.currentTimeMillis()

        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        try {
            // 2. 요청 정보 로그
            log.info("➡️ [{}] {} {}", getClientIp(request), request.method, request.requestURI)

            chain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            val duration = System.currentTimeMillis() - startTime

            // 3. 요청 바디 로그
            val requestBody = String(wrappedRequest.contentAsByteArray, StandardCharsets.UTF_8)

            // 4. 응답 바디 로그
            val responseBody = String(wrappedResponse.contentAsByteArray, StandardCharsets.UTF_8)
            wrappedResponse.copyBodyToResponse() // body 재전송!

            log.info(
                "⬅️ [{}] {} {} ({}ms)\n📝 요청 바디: {}\n📦 응답 바디: {}",
                getClientIp(request),
                request.method,
                request.requestURI,
                duration,
                requestBody,
                responseBody
            )

            MDC.clear()
        }
    }

    private fun getClientIp(request: HttpServletRequest): String? {
        var ip = request.getHeader("X-Forwarded-For")
        if (ip == null || ip.isBlank()) {
            ip = request.remoteAddr
        }
        return ip
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
    }
}

package com.msa.gatewayservice.filters

import com.msa.gatewayservice.auth.consts.AuthConstants.AUTH_HEADER_PREFIX
import com.msa.gatewayservice.auth.consts.AuthConstants.CONTEXT_NOT_FOUND_MESSAGE
import com.msa.gatewayservice.auth.consts.AuthConstants.getActiveJtiKey
import com.msa.gatewayservice.auth.token.JwtTokenDecoder
import com.msa.gatewayservice.exception.BusinessErrorCode
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component


@Component
class AuthenticationGatewayFilterFactory(
    private val jwtTokenDecoder: JwtTokenDecoder,
    private val redisTemplate: RedisTemplate<String, String>
) : AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config>(Config::class.java) {

    class Config

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            // 1. 헤더에서 Authorization 토큰 가져오기
            val token = getAccessToken(exchange.request)

            // 2. 토큰 검증
            val tokenAuthInfo = jwtTokenDecoder.extractAuthInfo(token) // 만료되거나 위변조 시 예외 발생

            // 3. 활성 토큰이 맞는지 확인
            val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
            val storedUserId = redisTemplate.opsForValue().get(activeJtiKey)
            if (storedUserId == null || storedUserId != tokenAuthInfo.userId.toString()) {
                throw BusinessErrorCode.UNAUTHORIZED.exception("인증 정보가 유효하지 않습니다")
            }

            // 4. 검증 성공 시, 요청 헤더에 사용자 정보 추가
            val mutatedRequest = exchange.request.mutate()
                .header("X-User-Id", tokenAuthInfo.userId.toString())
                .header("X-User-Role", tokenAuthInfo.role.name)
                .header("X-User-Email", tokenAuthInfo.email)
                .build()

            val mutatedExchange = exchange.mutate().request(mutatedRequest).build()

            chain.filter(mutatedExchange)
        }
    }

    private fun getAccessToken(request: ServerHttpRequest): String {
        val headers = request.headers
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            throw BusinessErrorCode.UNAUTHORIZED.exception(CONTEXT_NOT_FOUND_MESSAGE)
        }

        return headers[HttpHeaders.AUTHORIZATION]!![0].substring(AUTH_HEADER_PREFIX.length)
    }

}

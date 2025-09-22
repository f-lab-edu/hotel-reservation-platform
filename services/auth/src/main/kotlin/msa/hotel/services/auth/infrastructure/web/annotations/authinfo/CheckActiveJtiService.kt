package msa.hotel.services.auth.infrastructure.web.annotations.authinfo

import msa.hotel.modules.jwt.token.JwtDecoder
import msa.hotel.modules.jwt.token.dto.TokenAuthInfo
import msa.hotel.modules.web.exception.ErrorCode
import msa.hotel.services.auth.domain.auth.port.AuthTokenRepository
import msa.hotel.services.auth.infrastructure.web.auth.header.RequestHeaderTokenExtractor
import org.springframework.stereotype.Service

@Service
class CheckActiveJtiService(
    private val tokenExtractor: RequestHeaderTokenExtractor,
    private val jwtDecoder: JwtDecoder,
    private val tokenRepo: AuthTokenRepository,
) {
    fun checkActiveJti(): TokenAuthInfo {
        val accessToken = tokenExtractor.getAccessToken()
        val authInfo = jwtDecoder.extractAuthInfo(accessToken)

        if (!tokenRepo.existActiveJti(authInfo.jti)) {
            throw ErrorCode.UNAUTHORIZED.exception("로그아웃 처리된 인증 정보입니다")
        }

        return authInfo
    }
}

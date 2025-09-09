package com.msa.identityservice.config.properties

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated


@Validated
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    @field:NotBlank(message = "jwt 시크릿 키가 비어있을 수 없습니다.")
    val secretKey: String,

    @field:Min(1, message = "Access Token 만료 시간은 최소 1시간 이상이어야 합니다.")
    @field:Max(10, message = "Access Token 만료 시간은 최대 10시간 넘을 수 없습니다.")
    val accessTokenExpirationHours: Long,

    @field:Min(23, message = "Access Token 만료 시간은 최소 23시간 이상이어야 합니다.")
    @field:Max(49, message = "Access Token 만료 시간은 최대 49시간 넘을 수 없습니다.")
    val refreshTokenExpirationHours: Long,

    @field:Min(1, message = "jwt 토큰 발급이 가능한 최대 기기 개수는 최소 1개 이상이어야 합니다.")
    @field:Max(10, message = "jwt 토큰 발급이 가능한 최대 기기 개수는 최대 10개를 넘을 수 없습니다.")
    val maxDeviceCount: Int
)

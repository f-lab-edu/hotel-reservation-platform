package com.msa.gatewayservice.config.properties

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProperties(
    @field:NotBlank(message = "redis host가 비어있을 수 없습니다.")
    val host: String,

    @field:Min(6000, message = "redis port는 최소 6000 이상이어야 합니다.")
    @field:Max(32000, message = "redis port는 최대 32000 넘을 수 없습니다.")
    val port: Int,

    @field:NotBlank(message = "redis password가 비어있을 수 없습니다.")
    val password: String,
)

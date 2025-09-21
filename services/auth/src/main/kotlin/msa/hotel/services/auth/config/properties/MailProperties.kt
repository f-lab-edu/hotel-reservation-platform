package msa.hotel.services.auth.config.properties

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Suppress("ktlint:standard:no-blank-line-in-list")
@Validated
@ConfigurationProperties(prefix = "spring.mail")
data class MailProperties(
    @field:NotBlank(message = "smtp host가 비어있을 수 없습니다.")
    val host: String,

    @field:Min(500, message = "smtp port는 최소 500 이상이어야 합니다.")
    @field:Max(1000, message = "smtp port는 최대 1000 넘을 수 없습니다.")
    val port: Int,

    @field:NotBlank(message = "smtp username가 비어있을 수 없습니다.")
    val username: String,

    @field:NotBlank(message = "smtp password가 비어있을 수 없습니다.")
    val password: String,
)

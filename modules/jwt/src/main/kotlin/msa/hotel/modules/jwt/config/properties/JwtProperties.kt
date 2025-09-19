package msa.hotel.modules.jwt.config.properties

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    @field:NotBlank(message = "jwt 시크릿 키가 비어있을 수 없습니다.")
    val secretKey: String,
)

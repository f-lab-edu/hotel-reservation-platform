package msa.hotel.services.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "msa.hotel.services.auth",
        "msa.hotel.modules.web",
        "msa.hotel.modules.jwt",
    ],
)
@ConfigurationPropertiesScan(
    basePackages = [
        "msa.hotel.services.auth.config.properties",
        "msa.hotel.modules.jwt.config.properties",
    ],
)
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}

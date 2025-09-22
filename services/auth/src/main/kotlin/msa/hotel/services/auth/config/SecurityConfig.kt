package msa.hotel.services.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/auth/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/identity",
                        "/identity/verify/request",
                        "/identity/verify/confirm",
                        "/identity/password/change",
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/identity/verify/confirm",
                    ).permitAll()
                    .anyRequest()
                    .authenticated()
            }

        return http.build()
    }
}

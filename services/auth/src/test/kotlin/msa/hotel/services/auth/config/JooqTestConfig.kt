package msa.hotel.services.auth.config

import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JooqTestConfig {
    // 입력 스키마를 Test으로 변경
    @Bean
    fun jooqTestConfigurationCustomizer(): DefaultConfigurationCustomizer =
        DefaultConfigurationCustomizer { config ->
            config
                .settings()
                .withRenderMapping(
                    RenderMapping().withSchemata(
                        MappedSchema()
                            .withInput("auth-db")
                            .withOutput("test-auth-db"),
                    ),
                )
        }
}

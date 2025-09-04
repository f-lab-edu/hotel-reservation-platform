package com.msa.identityservice.config

import org.jooq.conf.RenderMapping
import org.jooq.conf.MappedSchema
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JooqTestConfig {

    @Bean
    fun jooqTestConfigurationCustomizer(): DefaultConfigurationCustomizer {
        return DefaultConfigurationCustomizer { config ->
            config.settings()
                .withRenderMapping(
                    RenderMapping().withSchemata(
                        // 입력 스키마(코드에 하드코딩된 값)가 "identity-db"일 경우,
                        MappedSchema().withInput("identity-db")
                                     // 출력 스키마(실제 쿼리에 나갈 값)를 "identity-test-db"로 변경합니다.
                                     .withOutput("identity-test-db")
                    )
                )
        }
    }
}

package com.msa.accommodationservice.config

import com.msa.accommodationservice.config.properties.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory


@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties
) {

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisHost = redisProperties.host
        val redisPort = redisProperties.port
        val redisPassword = redisProperties.password

        val redisStandaloneConfiguration = RedisStandaloneConfiguration(redisHost, redisPort)
        redisStandaloneConfiguration.setPassword(redisPassword)

        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }
    
}

package com.msa.identityservice.config

import com.msa.identityservice.config.properties.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.serializer.StringRedisSerializer


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

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.connectionFactory = redisConnectionFactory()

        // Key는 String, Value는 모든 객체를 저장할 수 있도록 설정
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.hashKeySerializer = StringRedisSerializer()
        redisTemplate.hashValueSerializer = StringRedisSerializer()

        return redisTemplate
    }

    @Bean
    fun evictAndAddSessionScript(): DefaultRedisScript<Long> {
        val script = DefaultRedisScript<Long>()
        script.setLocation(ClassPathResource("scripts/atomic-login.lua"))
        script.resultType = Long::class.java
        return script
    }
}

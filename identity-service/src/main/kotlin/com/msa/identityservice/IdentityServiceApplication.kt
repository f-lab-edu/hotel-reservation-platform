package com.msa.identityservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication


@SpringBootApplication(scanBasePackages = ["com.msa.identityservice", "com.msa.supportmodule"])
@ConfigurationPropertiesScan(basePackages = ["com.msa.identityservice.config.properties"])
class IdentityServiceApplication

fun main(args: Array<String>) {
    runApplication<IdentityServiceApplication>(*args)
}

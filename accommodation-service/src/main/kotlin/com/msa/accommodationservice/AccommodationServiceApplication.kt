package com.msa.accommodationservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication


@SpringBootApplication(scanBasePackages = ["com.msa.accommodationservice", "com.msa.supportmodule"])
@ConfigurationPropertiesScan(basePackages = ["com.msa.accommodationservice.config.properties"])
class AccommodationServiceApplication

fun main(args: Array<String>) {
    runApplication<AccommodationServiceApplication>(*args)
}

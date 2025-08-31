package com.msa.accommodationservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AccommodationServiceApplication

fun main(args: Array<String>) {
    runApplication<AccommodationServiceApplication>(*args)
}

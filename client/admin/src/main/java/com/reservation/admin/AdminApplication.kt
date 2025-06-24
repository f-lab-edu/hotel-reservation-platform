package com.reservation.admin

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(
    scanBasePackages = ["com.reservation.admin", "com.reservation.domain", "com.reservation.querysupport", "com.reservation.support"]
)
open class AdminApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(AdminApplication::class.java, *args)
        }
    }
}

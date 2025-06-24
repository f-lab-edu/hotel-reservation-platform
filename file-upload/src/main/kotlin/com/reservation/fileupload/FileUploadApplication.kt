package com.reservation.fileupload

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(
    scanBasePackages = ["com.reservation.auth", "com.reservation.domain", "com.reservation.fileupload"
    ]
)
open class FileUploadApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(FileUploadApplication::class.java, *args)
        }
    }
}

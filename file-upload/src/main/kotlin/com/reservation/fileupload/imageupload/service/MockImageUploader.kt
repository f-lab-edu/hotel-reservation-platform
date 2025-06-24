package com.reservation.fileupload.imageupload.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class MockImageUploader : ImageUploader {
    private val log: Logger = LoggerFactory.getLogger(MockImageUploader::class.java)

    override fun upload(file: MultipartFile, url: String): String {
        return url
    }

    override fun delete(url: String) {
        log.info("이미지 삭제됨 (mock): {}", url)
    }
}

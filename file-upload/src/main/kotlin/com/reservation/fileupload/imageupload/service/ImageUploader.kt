package com.reservation.fileupload.imageupload.service

import org.springframework.web.multipart.MultipartFile

interface ImageUploader {
    fun upload(file: MultipartFile, url: String): String

    fun delete(url: String)
}

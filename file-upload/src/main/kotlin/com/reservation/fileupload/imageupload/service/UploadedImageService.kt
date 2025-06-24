package com.reservation.fileupload.imageupload.service

import com.reservation.domain.uploadedimage.UploadedImage
import com.reservation.fileupload.imageupload.repository.JpaUploadedImageRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@RequiredArgsConstructor
class UploadedImageService {
    private val jpaUploadedImageRepository: JpaUploadedImageRepository? = null
    private val imageUploader: ImageUploader? = null

    fun uploadImage(image: UploadedImage, file: MultipartFile): String {
        val url = S3_DOMAIN + image.uploadDate + image.uuid

        imageUploader!!.upload(file, url)

        jpaUploadedImageRepository!!.save(image)

        return url
    }

    companion object {
        private const val S3_DOMAIN = "https://mock-bucket.s3.fake/"
    }
}

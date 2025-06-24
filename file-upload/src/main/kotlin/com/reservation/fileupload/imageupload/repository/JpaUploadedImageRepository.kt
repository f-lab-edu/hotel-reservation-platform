package com.reservation.fileupload.imageupload.repository

import com.reservation.domain.uploadedimage.UploadedImage
import org.springframework.data.jpa.repository.JpaRepository

interface JpaUploadedImageRepository : JpaRepository<UploadedImage, Long>

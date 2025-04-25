package com.reservation.fileupload.imageupload.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.uploadedimage.UploadedImage;

public interface JpaUploadedImageRepository extends JpaRepository<UploadedImage, Long> {
}

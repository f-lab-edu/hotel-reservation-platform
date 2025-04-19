package com.reservation.common.uploadedimage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.common.uploadedimage.domain.UploadedImage;

public interface JpaUploadedImageRepository extends JpaRepository<UploadedImage, Long> {
}

package com.reservation.common.uploadedimage.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.reservation.common.support.imageuploader.ImageUploader;
import com.reservation.common.uploadedimage.domain.UploadedImage;
import com.reservation.common.uploadedimage.repository.JpaUploadedImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadedImageService {
	private static final String S3_DOMAIN = "https://mock-bucket.s3.fake/";

	private final JpaUploadedImageRepository jpaRepository;
	private final ImageUploader imageUploader;

	public String uploadImage(UploadedImage image, MultipartFile file) {
		String url = S3_DOMAIN + image.getUploadDate() + image.getUuid();

		imageUploader.upload(file, url);

		jpaRepository.save(image);
		return url;
	}
}

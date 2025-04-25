package com.reservation.fileupload.imageupload.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.reservation.domain.uploadedimage.UploadedImage;
import com.reservation.fileupload.imageupload.repository.JpaUploadedImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadedImageService {
	private static final String S3_DOMAIN = "https://mock-bucket.s3.fake/";

	private final JpaUploadedImageRepository jpaUploadedImageRepository;
	private final ImageUploader imageUploader;

	public String uploadImage(UploadedImage image, MultipartFile file) {
		String url = S3_DOMAIN + image.getUploadDate() + image.getUuid();

		imageUploader.upload(file, url);

		jpaUploadedImageRepository.save(image);

		return url;
	}
}

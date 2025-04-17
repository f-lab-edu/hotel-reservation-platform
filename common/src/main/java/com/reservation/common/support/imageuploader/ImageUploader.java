package com.reservation.common.support.imageuploader;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {
	String upload(MultipartFile file);

	void delete(String url);
}

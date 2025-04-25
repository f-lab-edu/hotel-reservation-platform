package com.reservation.fileupload.imageupload.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {
	String upload(MultipartFile file, String url);

	void delete(String url);
}

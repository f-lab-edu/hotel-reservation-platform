package com.reservation.common.support.imageuploader;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class MockImageUploader implements ImageUploader {
	@Override
	public String upload(MultipartFile file) {
		return "https://mock-bucket.s3.fake/" + UUID.randomUUID();
	}

	@Override
	public void delete(String url) {
		log.info("이미지 삭제됨 (mock): {}", url);
	}
}

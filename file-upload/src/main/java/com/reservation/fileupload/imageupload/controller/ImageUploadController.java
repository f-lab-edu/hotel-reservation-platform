package com.reservation.fileupload.imageupload.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.reservation.common.response.ApiResponse;
import com.reservation.common.uploadedimage.domain.UploadedImage;
import com.reservation.common.uploadedimage.domain.UploadedImage.UploadedImageBuilder;
import com.reservation.common.uploadedimage.service.UploadedImageService;
import com.reservation.fileupload.auth.annotation.LoginUser;
import com.reservation.fileupload.auth.domain.UserAuth;
import com.reservation.fileupload.imageupload.controller.dto.request.UploadImageRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/uploaded-image")
@Tag(name = "이미지 업로드 API", description = "업로드 이미지 관리 API입니다.")
@RequiredArgsConstructor
public class ImageUploadController {
	private final UploadedImageService uploadedImageService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "이미지 업로드", description = "이미지를 스토리지 서버에 업로드 합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<String> uploadImage(
		@RequestPart("body") UploadImageRequest request,
		@RequestPart MultipartFile file,
		@Schema(hidden = true) @LoginUser UserAuth userAuth) {

		UploadedImage uploadedImage = new UploadedImageBuilder()
			.domain(request.domain())
			.uploaderId(userAuth.getUserId())
			.uploaderRole(userAuth.getRole())
			.uploadDate(LocalDate.now())
			.uuid(UUID.randomUUID().toString())
			.build();

		String imageUrl = uploadedImageService.uploadImage(uploadedImage, file);

		return ApiResponse.ok(imageUrl);
	}
}

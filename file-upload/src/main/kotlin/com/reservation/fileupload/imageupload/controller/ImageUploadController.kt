package com.reservation.fileupload.imageupload.controller

import com.reservation.auth.annotation.LoginUser
import com.reservation.auth.annotation.dto.UserAuth
import com.reservation.domain.uploadedimage.UploadedImage
import com.reservation.domain.uploadedimage.enums.ImageDomain
import com.reservation.fileupload.imageupload.controller.dto.response.UploadImageResponse
import com.reservation.fileupload.imageupload.service.UploadedImageService
import com.reservation.support.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/uploaded-image")
@Tag(name = "이미지 업로드 API", description = "업로드 이미지 관리 API입니다.")
@RequiredArgsConstructor
class ImageUploadController {
    private val uploadedImageService: UploadedImageService? = null

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "이미지 업로드", description = "이미지를 스토리지 서버에 업로드 합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadImage(
        @RequestParam domain: ImageDomain?,
        @RequestPart file: MultipartFile,
        @LoginUser userAuth: UserAuth
    ): ApiResponse<UploadImageResponse> {
        val uploadedImage = UploadedImage.builder()
            .domain(domain)
            .uploaderId(userAuth.userId)
            .uploaderRole(userAuth.role)
            .uploadDate(LocalDate.now())
            .uuid(UUID.randomUUID().toString())
            .build()

        val imageUrl = uploadedImageService!!.uploadImage(uploadedImage, file)

        return ApiResponse.ok(UploadImageResponse(imageUrl))
    }
}

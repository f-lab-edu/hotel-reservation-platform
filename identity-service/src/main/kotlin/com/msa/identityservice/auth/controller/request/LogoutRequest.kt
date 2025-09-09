package com.msa.identityservice.auth.controller.request

import jakarta.validation.constraints.NotBlank

data class LogoutRequest(
    @field:NotBlank(message = "로그아웃 기기 ID는 필수입니다.")
    val deviceId: String? // TODO: Device Fingerprinting
)

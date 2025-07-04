package com.reservation.support.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(private val httpStatus: HttpStatus, private val defaultMessage: String) {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "유효성 검증에 실패했거나, 요청 값이 잘못되었습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "유효성 검증에 실패했거나, 요청 값이 잘못되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 없거나, 인증에 실패했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "이미 존재하거나, 처리된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");

    fun status(): HttpStatus {
        return httpStatus
    }

    fun message(): String {
        return defaultMessage
    }

    fun exception(): BusinessException {
        return BusinessException(this, this.defaultMessage)
    }

    fun exception(customMessage: String?): BusinessException {
        return BusinessException(this, customMessage)
    }
}

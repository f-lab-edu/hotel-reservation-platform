package com.reservation.support.exception

import org.springframework.http.HttpStatus

class BusinessException internal constructor(private val errorCode: ErrorCode, message: String?) :
    RuntimeException(message) {
    fun errorCode(): ErrorCode {
        return errorCode
    }

    fun status(): HttpStatus {
        return errorCode.status()
    }
}

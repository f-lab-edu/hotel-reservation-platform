package com.msa.identityservice.exception

import org.springframework.http.HttpStatus


class BusinessException (
    private val errorCode: BusinessErrorCode,
    override val message: String
) : RuntimeException(message) {

    fun errorCode(): BusinessErrorCode {
        return errorCode
    }

    fun httpStatus(): HttpStatus {
        return errorCode.httpStatus()
    }
}

package com.msa.identityservice.exception

import org.springframework.http.HttpStatus


class BusinessException(
    val errorCode: BusinessErrorCode,
    override val message: String
) : RuntimeException(message) {
    
    fun httpStatus(): HttpStatus {
        return errorCode.httpStatus
    }

}

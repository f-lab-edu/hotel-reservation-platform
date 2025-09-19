package msa.hotel.modules.web.exception

import org.springframework.http.HttpStatus

class BusinessException(
    val errorCode: ErrorCode,
    override val message: String,
) : RuntimeException(message) {
    fun httpStatus(): HttpStatus = errorCode.httpStatus
}

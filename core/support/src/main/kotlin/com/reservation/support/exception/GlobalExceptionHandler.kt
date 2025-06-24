package com.reservation.support.exception

import com.reservation.support.response.ApiErrorResponse
import com.reservation.support.response.ApiErrorResponse.Companion.of
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.stream.Collectors

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handle404(e: NoHandlerFoundException?): ResponseEntity<ApiErrorResponse> {
        val response = of(ErrorCode.NOT_FOUND.name, NOT_FOUND_ERROR_MESSAGE)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handle404(e: HttpMessageNotReadableException): ResponseEntity<ApiErrorResponse> {
        val response =
            of(
                ErrorCode.BAD_REQUEST.name,
                e.fillInStackTrace().message!!
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        methodArgumentNotValidException: MethodArgumentNotValidException
    ): ResponseEntity<ApiErrorResponse> {
        val fieldErrors = methodArgumentNotValidException.bindingResult.fieldErrors
        val message = fieldErrors.stream()
            .map { error: FieldError -> String.format("[%s] %s", error.field, error.defaultMessage) }
            .collect(Collectors.joining(" | "))

        val responseCode = ErrorCode.VALIDATION_ERROR.name
        val response = of(responseCode, message)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(businessException: BusinessException): ResponseEntity<ApiErrorResponse> {
        val status = businessException.errorCode().status()
        val responseCode = businessException.errorCode().name
        val response = of(responseCode, businessException.message!!)

        return ResponseEntity.status(status).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ResponseEntity<ApiErrorResponse> {
        log.error(exception.message, exception)
        val response = of(ErrorCode.INTERNAL_SERVER_ERROR.name, DEFAULT_ERROR_MESSAGE)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    companion object {
        private const val NOT_FOUND_ERROR_MESSAGE = "존재하지 않는 URL입니다"
        private const val DEFAULT_ERROR_MESSAGE = "서버 내부 오류로 인한 작업 실패"
    }
}

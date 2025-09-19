package msa.hotel.modules.web.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import msa.hotel.modules.web.response.Response
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.util.stream.Collectors

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(
        NoHandlerFoundException::class,
        NoResourceFoundException::class,
        HttpRequestMethodNotSupportedException::class,
    )
    fun handleNotFound(exception: Exception): ResponseEntity<Response<Nothing>> {
        logger.error { exception }

        val response =
            Response.error(
                code = ErrorCode.NOT_FOUND.name,
                message = NOT_FOUND_ERROR_MESSAGE,
            )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(methodArgumentNotValidException: MethodArgumentNotValidException): ResponseEntity<Response<Nothing>> {
        logger.error { methodArgumentNotValidException }

        val fieldErrors = methodArgumentNotValidException.bindingResult.fieldErrors
        val message =
            fieldErrors
                .stream()
                .map { error: FieldError -> String.format("[%s] %s", error.field, error.defaultMessage) }
                .collect(Collectors.joining(" | "))

        val response =
            Response.error(
                code = ErrorCode.VALIDATION_ERROR.name,
                message = message,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(constraintViolationException: ConstraintViolationException): ResponseEntity<Response<Nothing>> {
        logger.error { constraintViolationException }

        val message =
            constraintViolationException.constraintViolations.joinToString(" | ") {
                // ConstraintViolation에서 필드명과 메시지를 추출합니다.
                val propertyPath = it.propertyPath.toString()
                val fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                "[$fieldName] ${it.message}"
            }

        val response =
            Response.error(
                code = ErrorCode.VALIDATION_ERROR.name,
                message = message,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(businessException: BusinessException): ResponseEntity<Response<Nothing>> {
        val status: HttpStatus = businessException.errorCode.httpStatus
        val responseCode = businessException.errorCode.name

        val response =
            Response.error(
                code = responseCode,
                message = businessException.message,
            )

        return ResponseEntity.status(status).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ResponseEntity<Response<Nothing>> {
        logger.error { exception }

        val response =
            Response.error(
                code = ErrorCode.INTERNAL_SERVER_ERROR.name,
                message = DEFAULT_ERROR_MESSAGE,
            )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    companion object {
        private const val NOT_FOUND_ERROR_MESSAGE = "지원하지 않는 API 유형입니다"
        private const val DEFAULT_ERROR_MESSAGE = "서버 내부 오류로 인한 작업 실패"
    }
}

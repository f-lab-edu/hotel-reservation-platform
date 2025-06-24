package com.reservation.support.response

@JvmRecord
data class ApiErrorResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val dataOrNull: Any? // or null, or errors 리스트
) {
    companion object {
        @JvmStatic
        fun of(code: String, message: String): ApiErrorResponse {
            return ApiErrorResponse(false, code, message, null)
        }

        fun of(code: String, message: String, dataOrNull: Any?): ApiErrorResponse {
            return ApiErrorResponse(false, code, message, dataOrNull)
        }
    }
}

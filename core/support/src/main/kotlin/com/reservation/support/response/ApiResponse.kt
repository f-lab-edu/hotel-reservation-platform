package com.reservation.support.response

@JvmRecord
data class ApiResponse<T>(
    val success: Boolean,
    val data: T
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> {
            return ApiResponse(true, data)
        }

        @JvmStatic
        fun <T> noContent(): ApiResponse<T?> {
            return ApiResponse(true, null)
        }
    }
}


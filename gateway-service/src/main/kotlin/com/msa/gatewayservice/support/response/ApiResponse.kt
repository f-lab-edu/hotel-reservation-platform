package com.msa.gatewayservice.support.response

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?
) {

    companion object {
        fun <T> create(message: String, data: T): ApiResponse<T> {
            return ApiResponse(true, "create", message, data)
        }

        fun <T> read(message: String, data: T): ApiResponse<T> {
            return ApiResponse(true, "read", message, data)
        }

        fun <T> update(message: String, data: T): ApiResponse<T> {
            return ApiResponse(true, "update", message, data)
        }

        fun <T> delete(message: String, data: T): ApiResponse<T> {
            return ApiResponse(true, "delete", message, data)
        }

        fun noContent(message: String): ApiResponse<Nothing> {
            return ApiResponse(true, "noContent", message, null)
        }

        fun error(code: String, message: String): ApiResponse<Nothing> {
            return ApiResponse(false, code, message, null)
        }
    }

}

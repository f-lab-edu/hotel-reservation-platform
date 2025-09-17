package msa.hotel.modules.web.response

data class Response<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?,
) {
    companion object {
        fun <T> create(
            message: String,
            data: T,
        ): Response<T> = Response(true, "CREATED", message, data)

        fun <T> read(
            message: String,
            data: T,
        ): Response<T> = Response(true, "READ", message, data)

        fun <T> update(
            message: String,
            data: T,
        ): Response<T> = Response(true, "UPDATED", message, data)

        fun <T> delete(
            message: String,
            data: T,
        ): Response<T> = Response(true, "DELETED", message, data)

        fun noContent(message: String): Response<Nothing> = Response(true, "NO_CONTENT", message, null)

        fun error(
            code: String,
            message: String,
        ): Response<Nothing> = Response(false, code, message, null)
    }
}

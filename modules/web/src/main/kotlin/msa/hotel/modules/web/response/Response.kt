package msa.hotel.modules.web.response

data class Response<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?
) {

    companion object {
        fun <T> create(message: String, data: T): Response<T> {
            return Response(true, "create", message, data)
        }

        fun <T> read(message: String, data: T): Response<T> {
            return Response(true, "read", message, data)
        }

        fun <T> update(message: String, data: T): Response<T> {
            return Response(true, "update", message, data)
        }

        fun <T> delete(message: String, data: T): Response<T> {
            return Response(true, "delete", message, data)
        }

        fun noContent(message: String): Response<Nothing> {
            return Response(true, "noContent", message, null)
        }

        fun error(code: String, message: String): Response<Nothing> {
            return Response(false, code, message, null)
        }
    }

}

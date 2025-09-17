package msa.hotel.modules.web.support

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import msa.hotel.modules.web.response.Response
import org.springframework.test.web.servlet.MvcResult

inline fun <reified T> MvcResult.toResponse(objectMapper: ObjectMapper): Response<T> {
    val content = this.response.contentAsString
    val typeRef = object : TypeReference<Response<T>>() {}

    return objectMapper.readValue(content, typeRef)
}

package com.msa.accommodationservice.config

import com.msa.supportmodule.annotation.HostIdArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class AnnotationConfig(
    private val hostIdArgumentResolver: HostIdArgumentResolver
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver?>) {
        resolvers.add(hostIdArgumentResolver)
    }
}

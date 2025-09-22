package msa.hotel.services.auth.config

import msa.hotel.services.auth.infrastructure.web.annotations.authinfo.AuthInfoArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class AnnotationConfig(
    private val authInfoArgumentResolver: AuthInfoArgumentResolver,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authInfoArgumentResolver)
    }
}

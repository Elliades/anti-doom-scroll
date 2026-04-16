package app.antidoomscroll.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import org.springframework.core.io.Resource

/**
 * Serves the React SPA from classpath:/static/ and forwards
 * all non-API, non-file routes to index.html so React Router
 * handles client-side routing (deep links, refresh).
 */
@Configuration
class SpaForwardingConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(object : PathResourceResolver() {
                override fun getResource(resourcePath: String, location: Resource): Resource? {
                    val requested = ClassPathResource("static/$resourcePath")
                    return if (requested.exists() && requested.isReadable) {
                        requested
                    } else if (!resourcePath.startsWith("api/")) {
                        ClassPathResource("static/index.html")
                    } else {
                        null
                    }
                }
            })
    }
}

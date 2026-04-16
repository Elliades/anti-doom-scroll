package app.antidoomscroll.web

import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Handles GET / so users who open the API root see a helpful message instead of a 404/500.
 * Only active when the frontend runs separately (local dev). On railway/prod the SPA
 * static files serve the React app at / via SpaForwardingConfig.
 */
@RestController
@Profile("!railway")
class RootController {

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    fun root(): String = """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8"><title>Anti-Doom Scroll API</title></head>
        <body style="font-family:sans-serif;padding:2rem;max-width:40rem;">
            <h1>Anti-Doom Scroll API</h1>
            <p>This is the backend. For the app UI, open <a href="http://localhost:5174">http://localhost:5174</a>.</p>
            <p><a href="/api/health">Health check</a> &middot; <a href="/api/subjects">Subjects</a> &middot; <a href="/api/journey?code=default">Journey</a></p>
        </body></html>
    """.trimIndent()
}

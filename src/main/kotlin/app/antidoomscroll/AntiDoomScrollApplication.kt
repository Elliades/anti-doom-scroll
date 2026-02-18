package app.antidoomscroll

import app.antidoomscroll.config.JourneyConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JourneyConfigProperties::class)
class AntiDoomScrollApplication

fun main(args: Array<String>) {
    runApplication<AntiDoomScrollApplication>(*args)
}

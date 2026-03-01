package app.antidoomscroll

import app.antidoomscroll.config.JourneyConfigProperties
import app.antidoomscroll.config.LadderConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JourneyConfigProperties::class, LadderConfigProperties::class)
class AntiDoomScrollApplication

fun main(args: Array<String>) {
    runApplication<AntiDoomScrollApplication>(*args)
}

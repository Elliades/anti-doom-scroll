package app.antidoomscroll.config

import app.antidoomscroll.domain.Journey
import app.antidoomscroll.domain.JourneyStepDef
import app.antidoomscroll.domain.JourneyStepType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "app.journey")
data class JourneyConfigProperties(
    @DefaultValue("default") val code: String,
    val name: String? = null,
    val steps: List<JourneyStepConfig> = emptyList(),
    val content: Map<String, ReflectionContentConfig> = emptyMap()
) {
    data class JourneyStepConfig(
        val stepIndex: Int,
        val type: String,
        val config: Map<String, Any?> = emptyMap()
    )

    data class ReflectionContentConfig(
        val title: String = "",
        val body: String = ""
    )

    fun toJourney(): Journey = Journey(
        code = code,
        name = name,
        steps = steps.map { s ->
            JourneyStepDef(
                stepIndex = s.stepIndex,
                type = JourneyStepType.valueOf(s.type.uppercase().replace("-", "_")),
                config = s.config
            )
        }.sortedBy { it.stepIndex }
    }
}

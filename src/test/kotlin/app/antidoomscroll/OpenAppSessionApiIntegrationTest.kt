package app.antidoomscroll

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Integration tests for OpenApp session: GET /api/session/start?mode=openapp
 * returns 3 random ultra-easy/easy exercises from all subjects.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class OpenAppSessionApiIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun startSessionWithModeOpenApp_returnsOkAndProfileAndSteps() {
        mvc.perform(get("/api/session/start").param("mode", "openapp"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileId").exists())
            .andExpect(jsonPath("$.steps").isArray())
            .andExpect(jsonPath("$.sessionDefaultSeconds").isNumber())
            .andExpect(jsonPath("$.lowBatteryModeSeconds").isNumber())
    }

    @Test
    fun startSessionWithModeOpenApp_returnsUpToThreeSteps() {
        val result = mvc.perform(get("/api/session/start").param("mode", "openapp"))
            .andExpect(status().isOk())
            .andReturn()
        val tree = com.fasterxml.jackson.databind.ObjectMapper().readTree(result.response.contentAsString)
        val steps = tree.get("steps")
        assertTrue(steps.size() <= 3) { "Expected at most 3 steps, got ${steps.size()}" }
    }

    @Test
    fun startSessionWithModeOpenApp_allStepsAreUltraEasyOrEasy() {
        val result = mvc.perform(get("/api/session/start").param("mode", "openapp"))
            .andExpect(status().isOk())
            .andReturn()
        val tree = com.fasterxml.jackson.databind.ObjectMapper().readTree(result.response.contentAsString)
        val steps = tree.get("steps")
        val allowed = setOf("ULTRA_EASY", "EASY")
        for (i in 0 until steps.size()) {
            val difficulty = steps[i].get("difficulty").asText()
            assertTrue(difficulty in allowed) {
                "Step ${i + 1} difficulty must be ULTRA_EASY or EASY, got: $difficulty"
            }
        }
    }

    @Test
    fun startSessionWithModeOpenApp_eachStepHasExerciseWithRequiredFields() {
        mvc.perform(get("/api/session/start").param("mode", "openapp"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.steps[0].stepIndex").value(1))
            .andExpect(jsonPath("$.steps[0].exercise.id").exists())
            .andExpect(jsonPath("$.steps[0].exercise.subjectId").exists())
            .andExpect(jsonPath("$.steps[0].exercise.type").exists())
            .andExpect(jsonPath("$.steps[0].exercise.difficulty").exists())
            .andExpect(jsonPath("$.steps[0].exercise.prompt").exists())
            .andExpect(jsonPath("$.steps[0].exercise.expectedAnswers").isArray())
    }

    @Test
    fun startSessionWithoutMode_unchangedDefaultBehavior() {
        mvc.perform(get("/api/session/start"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileId").exists())
            .andExpect(jsonPath("$.steps").isArray())
            .andExpect(jsonPath("$.steps[0].exercise.subjectId").exists())
    }

    @Test
    fun startSessionWithModeOpenAppAndProfileId_returnsSession() {
        mvc.perform(
            get("/api/session/start")
                .param("mode", "openapp")
                .param("profileId", "b0000000-0000-0000-0000-000000000001")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.steps").isArray())
    }
}

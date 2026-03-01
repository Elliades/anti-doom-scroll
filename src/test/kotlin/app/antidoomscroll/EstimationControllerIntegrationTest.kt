package app.antidoomscroll

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
 * Integration tests for ESTIMATION exercise type.
 * Verifies that estimationParams (correctAnswer, unit, toleranceFactor, category, hint)
 * are returned correctly by the API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class EstimationControllerIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    private val mapper = ObjectMapper()

    // -------------------------------------------------------------------------
    // GET /api/exercises/{id} — geography exercise with hint
    // -------------------------------------------------------------------------

    @Test
    fun `ESTIMATION geography exercise returns estimationParams with all fields`() {
        mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000301"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.type").value("ESTIMATION"))
            .andExpect(jsonPath("$.difficulty").value("ULTRA_EASY"))
            .andExpect(jsonPath("$.subjectCode").value("ESTIMATION"))
            .andExpect(jsonPath("$.estimationParams").exists())
            .andExpect(jsonPath("$.estimationParams.correctAnswer").value(330.0))
            .andExpect(jsonPath("$.estimationParams.unit").value("m"))
            .andExpect(jsonPath("$.estimationParams.toleranceFactor").value(1.5))
            .andExpect(jsonPath("$.estimationParams.category").value("geography"))
            .andExpect(jsonPath("$.estimationParams.hint").exists())
    }

    // -------------------------------------------------------------------------
    // GET /api/exercises/{id} — math exercise (no hint)
    // -------------------------------------------------------------------------

    @Test
    fun `ESTIMATION math exercise returns estimationParams without hint`() {
        val res = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000300"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.type").value("ESTIMATION"))
            .andExpect(jsonPath("$.difficulty").value("ULTRA_EASY"))
            .andExpect(jsonPath("$.estimationParams.correctAnswer").value(365.0))
            .andExpect(jsonPath("$.estimationParams.unit").value("days"))
            .andExpect(jsonPath("$.estimationParams.toleranceFactor").value(1.03))
            .andExpect(jsonPath("$.estimationParams.category").value("math"))
            .andReturn().response.contentAsString

        val tree = mapper.readTree(res)
        val hint = tree.get("estimationParams").get("hint")
        assertTrue(hint == null || hint.isNull) { "No hint expected for this exercise, got: $hint" }
    }

    // -------------------------------------------------------------------------
    // GET /api/exercises/{id} — EASY geography exercise
    // -------------------------------------------------------------------------

    @Test
    fun `ESTIMATION EASY geography exercise returns correct answer and tolerance`() {
        mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000305"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.type").value("ESTIMATION"))
            .andExpect(jsonPath("$.difficulty").value("EASY"))
            .andExpect(jsonPath("$.estimationParams.correctAnswer").value(8849.0))
            .andExpect(jsonPath("$.estimationParams.unit").value("m"))
            .andExpect(jsonPath("$.estimationParams.toleranceFactor").value(1.3))
            .andExpect(jsonPath("$.estimationParams.category").value("geography"))
    }

    // -------------------------------------------------------------------------
    // GET /api/exercises/{id} — EASY math estimation
    // -------------------------------------------------------------------------

    @Test
    fun `ESTIMATION EASY math exercise returns hint and tight tolerance`() {
        mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000308"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.type").value("ESTIMATION"))
            .andExpect(jsonPath("$.difficulty").value("EASY"))
            .andExpect(jsonPath("$.estimationParams.correctAnswer").value(391.0))
            .andExpect(jsonPath("$.estimationParams.toleranceFactor").value(1.1))
            .andExpect(jsonPath("$.estimationParams.category").value("math"))
            .andExpect(jsonPath("$.estimationParams.hint").exists())
    }

    // -------------------------------------------------------------------------
    // GET /api/subjects/ESTIMATION/exercises — list returns only ESTIMATION exercises
    // -------------------------------------------------------------------------

    @Test
    fun `ESTIMATION subject returns exercises with estimationParams`() {
        val res = mvc.perform(get("/api/subjects/ESTIMATION/exercises"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val tree = mapper.readTree(res)
        assertTrue(tree.isArray && tree.size() >= 4) { "Expected at least 4 ESTIMATION exercises, got ${tree.size()}" }
        for (ex in tree) {
            assertEquals("ESTIMATION", ex.get("type").asText())
            assertNotNull(ex.get("estimationParams")) { "Each ESTIMATION exercise must have estimationParams" }
            assertTrue(ex.get("estimationParams").get("correctAnswer").asDouble() > 0)
            assertTrue(ex.get("estimationParams").get("toleranceFactor").asDouble() > 1.0)
        }
    }

    // -------------------------------------------------------------------------
    // Verify prompt and expectedAnswers are passed through as-is (no generation)
    // -------------------------------------------------------------------------

    @Test
    fun `ESTIMATION exercise prompt and expectedAnswers are not modified by mapper`() {
        val res = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000301"))
            .andReturn().response.contentAsString

        val tree = mapper.readTree(res)
        assertEquals("How tall is the Eiffel Tower (in meters)?", tree.get("prompt").asText())
        val answers = tree.get("expectedAnswers")
        assertTrue(answers.isArray && answers.size() == 1)
        assertEquals("330", answers[0].asText())
    }

    // -------------------------------------------------------------------------
    // GET /api/subjects/ESTIMATION — subject is accessible
    // -------------------------------------------------------------------------

    @Test
    fun `ESTIMATION subject is accessible via subjects API`() {
        mvc.perform(get("/api/subjects/ESTIMATION"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("ESTIMATION"))
            .andExpect(jsonPath("$.name").value("Estimation"))
    }
}

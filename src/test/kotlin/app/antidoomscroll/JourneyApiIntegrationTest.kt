package app.antidoomscroll

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
 * Integration tests for Journey API: GET /api/journey and GET /api/journey/steps/{stepIndex}/content.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class JourneyApiIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun getJourney_returnsOkWithSteps() {
        mvc.perform(get("/api/journey").param("code", "default"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("default"))
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.steps").isArray())
            .andExpect(jsonPath("$.steps.length()").value(3))
            .andExpect(jsonPath("$.steps[0].stepIndex").value(0))
            .andExpect(jsonPath("$.steps[0].type").value("OPEN_APP"))
            .andExpect(jsonPath("$.steps[1].type").value("REFLECTION"))
            .andExpect(jsonPath("$.steps[2].type").value("CHAPTER_EXERCISES"))
    }

    @Test
    fun getJourney_unknownCode_returns404() {
        mvc.perform(get("/api/journey").param("code", "unknown"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun getStepContent_step0OpenApp_returnsSession() {
        mvc.perform(
            get("/api/journey/steps/0/content")
                .param("journeyCode", "default")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stepIndex").value(0))
            .andExpect(jsonPath("$.type").value("OPEN_APP"))
            .andExpect(jsonPath("$.session").exists())
            .andExpect(jsonPath("$.session.profileId").exists())
            .andExpect(jsonPath("$.session.steps").isArray())
    }

    @Test
    fun getStepContent_step1Reflection_returnsReflectionContent() {
        mvc.perform(
            get("/api/journey/steps/1/content")
                .param("journeyCode", "default")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stepIndex").value(1))
            .andExpect(jsonPath("$.type").value("REFLECTION"))
            .andExpect(jsonPath("$.reflection").exists())
            .andExpect(jsonPath("$.reflection.title").value("Why am I doom scrolling?"))
            .andExpect(jsonPath("$.reflection.body").exists())
    }

    @Test
    fun getStepContent_step2ChapterExercises_returnsChapterSeries() {
        mvc.perform(
            get("/api/journey/steps/2/content")
                .param("journeyCode", "default")
                .param("chapterIndex", "0")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stepIndex").value(2))
            .andExpect(jsonPath("$.type").value("CHAPTER_EXERCISES"))
            .andExpect(jsonPath("$.chapterSeries").exists())
            .andExpect(jsonPath("$.chapterSeries.chapters").isArray())
            .andExpect(jsonPath("$.chapterSeries.currentChapterIndex").value(0))
    }

    @Test
    fun getStepContent_step2WithChapterIndex_returnsChapterSeries() {
        mvc.perform(
            get("/api/journey/steps/2/content")
                .param("journeyCode", "default")
                .param("chapterIndex", "0")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chapterSeries").exists())
            .andExpect(jsonPath("$.chapterSeries.chapters").isArray())
            .andExpect(jsonPath("$.chapterSeries.currentChapterIndex").value(0))
        val result = mvc.perform(
            get("/api/journey/steps/2/content")
                .param("journeyCode", "default")
                .param("chapterIndex", "1")
        )
            .andExpect(status().isOk())
            .andReturn()
        val json = com.fasterxml.jackson.databind.ObjectMapper().readTree(result.response.contentAsString)
        val chapterSeries = json.get("chapterSeries")
        val currentIdx = chapterSeries.get("currentChapterIndex").asInt()
        val chapters = chapterSeries.get("chapters")
        val size = chapters.size()
        org.junit.jupiter.api.Assertions.assertTrue(size == 0 || currentIdx in 0 until size) { "currentChapterIndex must be in range [0, ${size})" }
    }

    @Test
    fun getStepContent_stepOutOfRange_returns404() {
        mvc.perform(
            get("/api/journey/steps/99/content")
                .param("journeyCode", "default")
        )
            .andExpect(status().isNotFound())
    }

    @Test
    fun getStepContent_withProfileId_returnsSessionWithProfile() {
        mvc.perform(
            get("/api/journey/steps/0/content")
                .param("journeyCode", "default")
                .param("profileId", "b0000000-0000-0000-0000-000000000001")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.session.profileId").exists())
    }

    /** Ensures N_BACK exercises in OPEN_APP session always have nBackParams.sequence (fixes "missing sequence" in app). */
    @Test
    fun getStepContent_step0OpenApp_nBackStepsHaveSequence() {
        val result = mvc.perform(
            get("/api/journey/steps/0/content")
                .param("journeyCode", "default")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("OPEN_APP"))
            .andExpect(jsonPath("$.session.steps").isArray())
            .andReturn()
        val tree = com.fasterxml.jackson.databind.ObjectMapper().readTree(result.response.contentAsString)
        val steps = tree.get("session").get("steps")
        for (i in 0 until steps.size()) {
            val exercise = steps.get(i).get("exercise")
            if (exercise.has("type") && exercise.get("type").asText() == "N_BACK") {
                val params = exercise.get("nBackParams") ?: exercise.get("nbackParams")
                org.junit.jupiter.api.Assertions.assertTrue(params != null && !params.isNull) {
                    "N_BACK step ${i + 1} must have nBackParams (app would show 'missing sequence')"
                }
                org.junit.jupiter.api.Assertions.assertTrue(params.has("sequence")) {
                    "nBackParams must have sequence"
                }
                org.junit.jupiter.api.Assertions.assertTrue(params.get("sequence").isArray && params.get("sequence").size() > 0) {
                    "nBackParams.sequence must be a non-empty array"
                }
            }
        }
    }
}

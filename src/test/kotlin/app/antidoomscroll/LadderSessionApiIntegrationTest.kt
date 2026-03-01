package app.antidoomscroll

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class LadderSessionApiIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun startLadderSession_returnsExerciseAndLadderState() {
        val response = mvc.perform(get("/api/session/start").param("mode", "ladder"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileId").exists())
            .andExpect(jsonPath("$.mode").value("ladder"))
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.exercise.id").exists())
            .andExpect(jsonPath("$.exercise.type").exists())
            .andExpect(jsonPath("$.ladderState").exists())
            .andExpect(jsonPath("$.ladderState.ladderCode").value("default"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.ladderState.recentScores").isArray())
            .andExpect(jsonPath("$.ladderState.overallScoreSum").value(0))
            .andExpect(jsonPath("$.ladderState.overallTotal").value(0))
            .andReturn()

        val body = response.response.contentAsString
        assert(body.contains("exercise"))
        assert(body.contains("ladderState"))
    }

    @Test
    fun ladderNext_returnsNextExerciseAndUpdatedState() {
        val startResponse = mvc.perform(get("/api/session/start").param("mode", "ladder"))
            .andExpect(status().isOk())
            .andReturn()

        val startJson = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
            .readTree(startResponse.response.contentAsString)

        val ladderState = startJson.get("ladderState")
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "${ladderState.get("ladderCode").asText()}",
                    "currentLevelIndex": ${ladderState.get("currentLevelIndex").asInt()},
                    "recentScores": [0.8, 0.9, 0.85, 0.9, 0.88],
                    "overallScoreSum": 4.33,
                    "overallTotal": 5
                },
                "lastScore": 0.88
            }
        """.trimIndent()

        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.ladderState").exists())
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(1))
            .andExpect(jsonPath("$.ladderState.recentScores").isEmpty) // Reset on level change
            .andExpect(jsonPath("$.levelChanged").exists())
            .andExpect(jsonPath("$.levelChanged.direction").value("up"))
    }

    @Test
    fun ladderNext_below40Percent_demotesLevel() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "default",
                    "currentLevelIndex": 1,
                    "recentScores": [0.3, 0.25, 0.35, 0.2, 0.3],
                    "overallScoreSum": 1.45,
                    "overallTotal": 5
                },
                "lastScore": 0.3
            }
        """.trimIndent()

        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.ladderState.recentScores").isEmpty) // Reset on demotion
            .andExpect(jsonPath("$.levelChanged.direction").value("down"))
    }

    @Test
    fun startSumLadderSession_returnsAddExercise() {
        mvc.perform(
            get("/api/session/start")
                .param("mode", "ladder")
                .param("ladderCode", "sum")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mode").value("ladder"))
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.exercise.type").value("FLASHCARD_QA"))
            .andExpect(jsonPath("$.exercise.mathOperation").value("ADD"))
            .andExpect(jsonPath("$.ladderState.ladderCode").value("sum"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
    }

    @Test
    fun sumLadderNext_returnsAddOrSubtractExercise() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "sum",
                    "currentLevelIndex": 2,
                    "recentScores": [0.8, 0.9, 0.85, 0.9, 0.88],
                    "overallScoreSum": 4.33,
                    "overallTotal": 5
                },
                "lastScore": 0.88
            }
        """.trimIndent()

        val result = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.ladderState.ladderCode").value("sum"))
            .andReturn()

        val mathOp = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
            .readTree(result.response.contentAsString)
            .path("exercise").path("mathOperation").asText()
        assert(mathOp == "ADD" || mathOp == "SUBTRACT") { "Expected ADD or SUBTRACT, got $mathOp" }
    }

    @Test
    fun ladderNext_between40And75_staysSameLevel() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "default",
                    "currentLevelIndex": 0,
                    "recentScores": [0.5, 0.6, 0.55, 0.5, 0.6],
                    "overallScoreSum": 2.75,
                    "overallTotal": 5
                },
                "lastScore": 0.6
            }
        """.trimIndent()

        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
    }
}

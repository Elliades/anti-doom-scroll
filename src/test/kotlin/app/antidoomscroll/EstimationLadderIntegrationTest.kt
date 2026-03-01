package app.antidoomscroll

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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

/**
 * Integration tests for the ESTIMATION dedicated ladder.
 * The test config (application-test.yml) defines a 9-level estimation ladder covering all
 * phases and bridges. Seed data (data.sql) provides 2 exercises per difficulty (UE→VH).
 *
 * Level map in test config:
 *   L0-L1: ULTRA_EASY       L2: bridge UE+EASY
 *   L3: EASY                 L4: bridge EASY+MEDIUM
 *   L5: MEDIUM               L6: bridge MEDIUM+HARD
 *   L7: HARD                 L8: VERY_HARD
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class EstimationLadderIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    private val mapper = jacksonObjectMapper()

    // -------------------------------------------------------------------------
    // Start ladder session
    // -------------------------------------------------------------------------

    @Test
    fun `estimation ladder start returns ULTRA_EASY ESTIMATION exercise at level 0`() {
        mvc.perform(
            get("/api/session/start")
                .param("mode", "ladder")
                .param("ladderCode", "estimation")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mode").value("ladder"))
            .andExpect(jsonPath("$.ladderState.ladderCode").value("estimation"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.ladderState.recentScores").isArray)
            .andExpect(jsonPath("$.ladderState.overallScoreSum").value(0))
            .andExpect(jsonPath("$.ladderState.overallTotal").value(0))
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.exercise.type").value("ESTIMATION"))
            .andExpect(jsonPath("$.exercise.difficulty").value("ULTRA_EASY"))
            .andExpect(jsonPath("$.exercise.estimationParams").exists())
            .andExpect(jsonPath("$.exercise.estimationParams.correctAnswer").exists())
            .andExpect(jsonPath("$.exercise.estimationParams.toleranceFactor").exists())
            .andExpect(jsonPath("$.exercise.estimationParams.category").exists())
    }

    @Test
    fun `estimation ladder start returns estimationParams not null`() {
        val res = mvc.perform(
            get("/api/session/start")
                .param("mode", "ladder")
                .param("ladderCode", "estimation")
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val tree = mapper.readTree(res)
        val ep = tree.path("exercise").path("estimationParams")
        assertNotNull(ep) { "estimationParams must be present at level 0" }
        assertTrue(ep.path("correctAnswer").asDouble() > 0)
        assertTrue(ep.path("toleranceFactor").asDouble() > 1.0)
        val category = ep.path("category").asText()
        assertTrue(category in listOf("math", "geography", "science", "history")) {
            "category must be one of math/geography/science/history, got: $category"
        }
    }

    // -------------------------------------------------------------------------
    // Advance: 5 high scores → level up
    // -------------------------------------------------------------------------

    @Test
    fun `estimation ladder 5 high scores advances to level 1 and resets scores`() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "estimation",
                    "currentLevelIndex": 0,
                    "recentScores": [0.8, 0.85, 0.9, 0.82, 0.88],
                    "overallScoreSum": 4.25,
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
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ladderState.ladderCode").value("estimation"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(1))
            .andExpect(jsonPath("$.ladderState.recentScores").isEmpty)
            .andExpect(jsonPath("$.levelChanged").exists())
            .andExpect(jsonPath("$.levelChanged.from").value(0))
            .andExpect(jsonPath("$.levelChanged.to").value(1))
            .andExpect(jsonPath("$.levelChanged.direction").value("up"))
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.exercise.type").value("ESTIMATION"))
    }

    // -------------------------------------------------------------------------
    // Stay: scores between 40-75% → stay at same level
    // -------------------------------------------------------------------------

    @Test
    fun `estimation ladder mid scores stays at same level with no levelChanged`() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "estimation",
                    "currentLevelIndex": 0,
                    "recentScores": [0.5, 0.6, 0.55, 0.5, 0.6],
                    "overallScoreSum": 2.75,
                    "overallTotal": 5
                },
                "lastScore": 0.6
            }
        """.trimIndent()

        val res = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.exercise").exists())
            .andReturn().response.contentAsString

        val tree = mapper.readTree(res)
        val levelChanged = tree.get("levelChanged")
        assertTrue(levelChanged == null || levelChanged.isNull) {
            "No levelChanged expected for mid scores, got: $levelChanged"
        }
    }

    // -------------------------------------------------------------------------
    // Demote: 5 low scores at level 1 → back to level 0
    // -------------------------------------------------------------------------

    @Test
    fun `estimation ladder low scores at level 1 demotes to level 0`() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "estimation",
                    "currentLevelIndex": 1,
                    "recentScores": [0.2, 0.25, 0.3, 0.2, 0.28],
                    "overallScoreSum": 1.23,
                    "overallTotal": 5
                },
                "lastScore": 0.28
            }
        """.trimIndent()

        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.ladderState.recentScores").isEmpty)
            .andExpect(jsonPath("$.levelChanged.direction").value("down"))
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.exercise.type").value("ESTIMATION"))
    }

    // -------------------------------------------------------------------------
    // Phase transitions: bridge levels mix difficulties
    // -------------------------------------------------------------------------

    @Test
    fun `estimation ladder at bridge level 2 returns ULTRA_EASY or EASY exercise`() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "estimation",
                    "currentLevelIndex": 2,
                    "recentScores": [],
                    "overallScoreSum": 0,
                    "overallTotal": 0
                },
                "lastScore": 0.5
            }
        """.trimIndent()

        val res = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.exercise.type").value("ESTIMATION"))
            .andReturn().response.contentAsString

        val difficulty = mapper.readTree(res).path("exercise").path("difficulty").asText()
        assertTrue(difficulty in listOf("ULTRA_EASY", "EASY")) {
            "Bridge L2 must return ULTRA_EASY or EASY, got: $difficulty"
        }
    }

    @Test
    fun `estimation ladder advancing to MEDIUM phase returns MEDIUM exercise`() {
        // Force into the MEDIUM phase (level 5) via high scores from level 4 (EASY bridge)
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "estimation",
                    "currentLevelIndex": 4,
                    "recentScores": [0.8, 0.85, 0.9, 0.82, 0.88],
                    "overallScoreSum": 4.25,
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
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.levelChanged.direction").value("up"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(5))
            .andExpect(jsonPath("$.exercise.type").value("ESTIMATION"))
            .andExpect(jsonPath("$.exercise.difficulty").value("MEDIUM"))
    }

    @Test
    fun `estimation ladder HARD phase returns HARD exercise`() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "estimation",
                    "currentLevelIndex": 7,
                    "recentScores": [],
                    "overallScoreSum": 0,
                    "overallTotal": 0
                },
                "lastScore": 0.5
            }
        """.trimIndent()

        val res = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exercise.type").value("ESTIMATION"))
            .andReturn().response.contentAsString

        val difficulty = mapper.readTree(res).path("exercise").path("difficulty").asText()
        assertEquals("HARD", difficulty) { "Level 7 is pure HARD phase, got: $difficulty" }
    }

    @Test
    fun `estimation ladder VERY_HARD phase returns VERY_HARD exercise`() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "estimation",
                    "currentLevelIndex": 8,
                    "recentScores": [],
                    "overallScoreSum": 0,
                    "overallTotal": 0
                },
                "lastScore": 0.5
            }
        """.trimIndent()

        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exercise.type").value("ESTIMATION"))
            .andExpect(jsonPath("$.exercise.difficulty").value("VERY_HARD"))
    }

    // -------------------------------------------------------------------------
    // At max level: stays at top (no level 9 exists in test config)
    // -------------------------------------------------------------------------

    @Test
    fun `estimation ladder at max level with perfect scores stays at max`() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "estimation",
                    "currentLevelIndex": 8,
                    "recentScores": [1.0, 1.0, 1.0, 1.0, 1.0],
                    "overallScoreSum": 5.0,
                    "overallTotal": 5
                },
                "lastScore": 1.0
            }
        """.trimIndent()

        val res = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(8))
            .andExpect(jsonPath("$.exercise").exists())
            .andReturn().response.contentAsString

        val tree = mapper.readTree(res)
        val levelChanged = tree.get("levelChanged")
        assertTrue(levelChanged == null || levelChanged.isNull) {
            "No levelChanged expected at max level, got: $levelChanged"
        }
    }

    // -------------------------------------------------------------------------
    // At min level with bad scores: stays at 0 (no crash)
    // -------------------------------------------------------------------------

    @Test
    fun `estimation ladder at level 0 with terrible scores stays at 0`() {
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "estimation",
                    "currentLevelIndex": 0,
                    "recentScores": [0.1, 0.05, 0.1, 0.0, 0.1],
                    "overallScoreSum": 0.35,
                    "overallTotal": 5
                },
                "lastScore": 0.1
            }
        """.trimIndent()

        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.exercise").exists())
    }

    // -------------------------------------------------------------------------
    // GET /api/ladders: estimation ladder is listed
    // -------------------------------------------------------------------------

    @Test
    fun `listLadders includes estimation ladder with correct level count and name`() {
        val res = mvc.perform(get("/api/ladders"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.code == 'estimation')]").exists())
            .andReturn().response.contentAsString

        val tree = mapper.readTree(res)
        val estimLadder = tree.find { it.get("code")?.asText() == "estimation" }
        assertNotNull(estimLadder) { "estimation ladder must be listed" }
        assertEquals("Estimation Ladder", estimLadder!!.get("name")?.asText()) {
            "estimation ladder name mismatch"
        }
        // Test config has 9 levels
        assertEquals(9, estimLadder.get("levelCount")?.asInt()) {
            "estimation test ladder should have 9 levels"
        }
    }

    // -------------------------------------------------------------------------
    // Continuous play: every next response includes an exercise (no gap)
    // -------------------------------------------------------------------------

    @Test
    fun `estimation ladder continuous play always returns an exercise`() {
        // Start
        val startRes = mvc.perform(
            get("/api/session/start")
                .param("mode", "ladder")
                .param("ladderCode", "estimation")
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val startTree = mapper.readTree(startRes)
        val state = startTree.get("ladderState")

        // Submit first answer (level-up case)
        val nextBody = """
            {
                "ladderState": {
                    "ladderCode": "${state.get("ladderCode").asText()}",
                    "currentLevelIndex": ${state.get("currentLevelIndex").asInt()},
                    "recentScores": [0.9, 0.9, 0.9, 0.9, 0.9],
                    "overallScoreSum": 4.5,
                    "overallTotal": 5
                },
                "lastScore": 0.9
            }
        """.trimIndent()

        val nextRes = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nextBody)
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val nextTree = mapper.readTree(nextRes)
        // Exercise must be present in the SAME response as levelChanged (no extra round-trip)
        assertNotNull(nextTree.get("exercise")) { "exercise must be in the same response as levelChanged" }
        assertEquals("ESTIMATION", nextTree.path("exercise").path("type").asText())
        assertNotNull(nextTree.path("exercise").path("estimationParams")) {
            "estimationParams must be present in continuous play response"
        }
    }
}

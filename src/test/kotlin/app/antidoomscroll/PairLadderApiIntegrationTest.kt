package app.antidoomscroll

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
 * Integration tests for the "pair" ladder:
 *  - Phase 1 (levels 0–2): MEMORY_CARD_PAIRS only
 *  - Phase 2 (levels 3–5): SUM_PAIR only
 *
 * Test config (application-test.yml) uses a compact 6-level ladder to keep
 * tests fast while covering the full two-phase transition.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class PairLadderApiIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    private val mapper = jacksonObjectMapper()

    // ------------------------------------------------------------------
    // Phase 1 — MEMORY_CARD_PAIRS
    // ------------------------------------------------------------------

    @Test
    fun startPairLadder_returnsMemoryCardPairsAtLevelZero() {
        mvc.perform(
            get("/api/session/start")
                .param("mode", "ladder")
                .param("ladderCode", "pair")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mode").value("ladder"))
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.exercise.type").value("MEMORY_CARD_PAIRS"))
            .andExpect(jsonPath("$.ladderState.ladderCode").value("pair"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.ladderState.overallScoreSum").value(0))
            .andExpect(jsonPath("$.ladderState.overallTotal").value(0))
    }

    @Test
    fun pairLadderNext_atLevel1_stillReturnsMemoryCardPairs() {
        val body = nextRequestBody(ladderCode = "pair", levelIndex = 1, lastScore = 0.7)

        val result = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.ladderState.ladderCode").value("pair"))
            .andReturn()

        val type = mapper.readTree(result.response.contentAsString)
            .path("exercise").path("type").asText()
        assert(type == "MEMORY_CARD_PAIRS") {
            "Expected MEMORY_CARD_PAIRS at level 1 but got $type"
        }
    }

    @Test
    fun pairLadderNext_atLevel2_stillReturnsMemoryCardPairs() {
        val body = nextRequestBody(ladderCode = "pair", levelIndex = 2, lastScore = 0.6)

        val result = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exercise").exists())
            .andReturn()

        val type = mapper.readTree(result.response.contentAsString)
            .path("exercise").path("type").asText()
        assert(type == "MEMORY_CARD_PAIRS") {
            "Expected MEMORY_CARD_PAIRS at level 2 but got $type"
        }
    }

    // ------------------------------------------------------------------
    // Phase 2 — SUM_PAIR
    // ------------------------------------------------------------------

    @Test
    fun pairLadderNext_atLevel3_returnsSumPair() {
        val body = nextRequestBody(ladderCode = "pair", levelIndex = 3, lastScore = 0.6)

        val result = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exercise").exists())
            .andReturn()

        val type = mapper.readTree(result.response.contentAsString)
            .path("exercise").path("type").asText()
        assert(type == "SUM_PAIR") {
            "Expected SUM_PAIR at level 3 (sum phase) but got $type"
        }
    }

    @Test
    fun pairLadderNext_atLevel4_returnsSumPair() {
        val body = nextRequestBody(ladderCode = "pair", levelIndex = 4, lastScore = 0.5)

        val result = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exercise").exists())
            .andReturn()

        val type = mapper.readTree(result.response.contentAsString)
            .path("exercise").path("type").asText()
        assert(type == "SUM_PAIR") {
            "Expected SUM_PAIR at level 4 but got $type"
        }
    }

    @Test
    fun pairLadderNext_atLevel5_returnsSumPair() {
        val body = nextRequestBody(ladderCode = "pair", levelIndex = 5, lastScore = 0.4)

        val result = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exercise").exists())
            .andReturn()

        val type = mapper.readTree(result.response.contentAsString)
            .path("exercise").path("type").asText()
        assert(type == "SUM_PAIR") {
            "Expected SUM_PAIR at level 5 (hard sum phase) but got $type"
        }
    }

    // ------------------------------------------------------------------
    // Advancement logic
    // ------------------------------------------------------------------

    @Test
    fun pairLadder_highScore_advancesToNextLevel() {
        // Start at level 0 with 5 perfect scores → should advance to level 1
        val body = nextRequestBody(
            ladderCode = "pair",
            levelIndex = 0,
            lastScore = 1.0,
            recentScores = listOf(1.0, 1.0, 1.0, 1.0, 1.0)
        )

        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(1))
            .andExpect(jsonPath("$.levelChanged").exists())
            .andExpect(jsonPath("$.levelChanged.direction").value("up"))
            .andExpect(jsonPath("$.ladderState.recentScores").isEmpty)
    }

    @Test
    fun pairLadder_lowScore_demotesToPreviousLevel() {
        // At level 1 with 5 poor scores → should demote to level 0
        val body = nextRequestBody(
            ladderCode = "pair",
            levelIndex = 1,
            lastScore = 0.2,
            recentScores = listOf(0.2, 0.3, 0.25, 0.1, 0.2)
        )

        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.levelChanged.direction").value("down"))
            .andExpect(jsonPath("$.ladderState.recentScores").isEmpty)
    }

    @Test
    fun pairLadder_midScore_staysSameLevel() {
        // 5 scores averaging ~0.55 → stay at level 2
        val body = nextRequestBody(
            ladderCode = "pair",
            levelIndex = 2,
            lastScore = 0.6,
            recentScores = listOf(0.5, 0.6, 0.55, 0.5, 0.6)
        )

        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(2))
            .andExpect(jsonPath("$.levelChanged").doesNotExist())
    }

    @Test
    fun pairLadder_advanceFromCardPairsToSumPair_switchesExerciseType() {
        // At level 2 (last MEMORY_CARD_PAIRS level in test config) with high scores → advance to level 3 (SUM_PAIR)
        val body = nextRequestBody(
            ladderCode = "pair",
            levelIndex = 2,
            lastScore = 1.0,
            recentScores = listOf(1.0, 1.0, 1.0, 1.0, 1.0)
        )

        val result = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(3))
            .andExpect(jsonPath("$.levelChanged.direction").value("up"))
            .andExpect(jsonPath("$.exercise").exists())
            .andReturn()

        val type = mapper.readTree(result.response.contentAsString)
            .path("exercise").path("type").asText()
        assert(type == "SUM_PAIR") {
            "After advancing from level 2 → 3, expected SUM_PAIR but got $type"
        }
    }

    // ------------------------------------------------------------------
    // Discovery
    // ------------------------------------------------------------------

    @Test
    fun listLadders_includesPairLadder() {
        mvc.perform(get("/api/ladders"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.code == 'pair')]").exists())
    }

    @Test
    fun listLadders_pairLadderHasCorrectNameAndLevelCount() {
        val result = mvc.perform(get("/api/ladders"))
            .andExpect(status().isOk)
            .andReturn()

        val tree = mapper.readTree(result.response.contentAsString)
        val pairLadder = tree.find { it.get("code")?.asText() == "pair" }
        assert(pairLadder != null) { "pair ladder not found in /api/ladders response" }
        assert(pairLadder!!.get("name")?.asText() == "Memory Pair Ladder") {
            "pair ladder name mismatch: ${pairLadder.get("name")?.asText()}"
        }
        assert(pairLadder.get("levelCount")?.asInt() == 6) {
            "pair ladder should have 6 levels in test config, got ${pairLadder.get("levelCount")?.asInt()}"
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun nextRequestBody(
        ladderCode: String,
        levelIndex: Int,
        lastScore: Double,
        recentScores: List<Double> = listOf(0.6, 0.55, 0.5, 0.6, 0.55)
    ): String {
        val scoresJson = recentScores.joinToString(", ")
        val overallSum = recentScores.sum()
        return """
            {
                "ladderState": {
                    "ladderCode": "$ladderCode",
                    "currentLevelIndex": $levelIndex,
                    "recentScores": [$scoresJson],
                    "overallScoreSum": $overallSum,
                    "overallTotal": ${recentScores.size}
                },
                "lastScore": $lastScore
            }
        """.trimIndent()
    }
}

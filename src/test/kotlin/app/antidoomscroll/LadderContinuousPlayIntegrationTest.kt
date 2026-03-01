package app.antidoomscroll

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
 * Verifies the ladder continuous-play contract:
 *   - The API always returns a next exercise immediately — no blocking state.
 *   - The client never needs to "wait" or "click Continue"; it simply calls /ladder/next
 *     and receives the next exercise in a single round-trip.
 *   - Level transitions are embedded in the response (levelChanged) without interrupting the flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class LadderContinuousPlayIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    private val mapper = jacksonObjectMapper()

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun startLadder(ladderCode: String = "default"): JsonNode {
        val result = mvc.perform(
            get("/api/session/start")
                .param("mode", "ladder")
                .param("ladderCode", ladderCode)
        )
            .andExpect(status().isOk())
            .andReturn()
        return mapper.readTree(result.response.contentAsString)
    }

    private fun nextExercise(
        ladderCode: String,
        levelIndex: Int,
        recentScores: List<Double>,
        overallScoreSum: Double,
        overallTotal: Int,
        lastScore: Double
    ): JsonNode {
        val body = mapper.writeValueAsString(
            mapOf(
                "ladderState" to mapOf(
                    "ladderCode" to ladderCode,
                    "currentLevelIndex" to levelIndex,
                    "recentScores" to recentScores,
                    "overallScoreSum" to overallScoreSum,
                    "overallTotal" to overallTotal
                ),
                "lastScore" to lastScore
            )
        )
        val result = mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk())
            .andReturn()
        return mapper.readTree(result.response.contentAsString)
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Core contract: the very first response from the ladder API already contains an exercise.
     * The client must be able to display it immediately — no extra round-trip needed.
     */
    @Test
    fun firstEntry_alwaysReturnsExerciseImmediately() {
        val resp = startLadder()
        assertAll(
            { assert(resp.has("exercise")) { "First response must contain exercise" } },
            { assert(!resp.get("exercise").isNull) { "exercise must not be null on start" } },
            { assert(resp.get("exercise").has("id")) { "exercise must have an id" } },
            { assert(resp.get("exercise").has("prompt")) { "exercise must have a prompt" } },
            { assert(resp.has("ladderState")) { "Response must carry ladderState for the client" } }
        )
    }

    /**
     * After every completed exercise, /ladder/next must return the next exercise
     * in a single call — no intermediate blocking state.
     * Simulates 5 consecutive correct answers staying on level 0.
     */
    @Test
    fun continuousPlay_nextExerciseReturnedAfterEveryAnswer() {
        val start = startLadder()
        val ladderCode = start.get("ladderState").get("ladderCode").asText()

        val recentScores = mutableListOf<Double>()
        var overallSum = 0.0
        var overallTotal = 0
        var currentLevel = start.get("ladderState").get("currentLevelIndex").asInt()

        // Play 4 answers (not enough to trigger level change with score ~60%)
        repeat(4) { round ->
            val score = 0.6
            val resp = nextExercise(
                ladderCode = ladderCode,
                levelIndex = currentLevel,
                recentScores = recentScores.toList(),
                overallScoreSum = overallSum,
                overallTotal = overallTotal,
                lastScore = score
            )

            // Every single response must carry an exercise — no blocking gap
            assertAll(
                { assert(resp.has("exercise")) { "Round $round: exercise must always be present" } },
                { assert(!resp.get("exercise").isNull) { "Round $round: exercise must not be null" } },
                { assert(resp.has("ladderState")) { "Round $round: ladderState must always be present" } }
            )

            recentScores.add(score)
            overallSum += score
            overallTotal++
            currentLevel = resp.get("ladderState").get("currentLevelIndex").asInt()
        }
    }

    /**
     * When the player scores >= 75% over the last 5 answers, the next response contains
     * the next exercise for the NEW level AND a levelChanged descriptor.
     * The exercise is still present — the player never waits.
     */
    @Test
    fun levelUp_exerciseImmediatelyAvailableInSameResponse() {
        val resp = nextExercise(
            ladderCode = "default",
            levelIndex = 0,
            recentScores = listOf(0.8, 0.9, 0.85, 0.9, 0.88),
            overallScoreSum = 4.33,
            overallTotal = 5,
            lastScore = 0.88
        )

        assertAll(
            // The next exercise is available in the same response — no extra round-trip
            { assert(resp.has("exercise")) { "exercise must be present after level-up" } },
            { assert(!resp.get("exercise").isNull) { "exercise must not be null after level-up" } },
            // Level-change metadata is embedded in the response
            { assert(resp.has("levelChanged")) { "levelChanged must be reported" } },
            { assert(resp.get("levelChanged").get("direction").asText() == "up") { "direction must be 'up'" } },
            { assert(resp.get("levelChanged").get("from").asInt() == 0) },
            { assert(resp.get("levelChanged").get("to").asInt() == 1) },
            // recentScores reset so the new level is evaluated fresh
            { assert(resp.get("ladderState").get("recentScores").isEmpty) { "recentScores must reset on level-up" } },
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 1) }
        )
    }

    /**
     * When the player scores < 40% over the last 5 answers, the next response contains
     * the next exercise at the PREVIOUS level AND a levelChanged descriptor.
     * The player never waits.
     */
    @Test
    fun levelDown_exerciseImmediatelyAvailableInSameResponse() {
        val resp = nextExercise(
            ladderCode = "default",
            levelIndex = 1,
            recentScores = listOf(0.3, 0.2, 0.35, 0.25, 0.3),
            overallScoreSum = 1.4,
            overallTotal = 5,
            lastScore = 0.3
        )

        assertAll(
            { assert(resp.has("exercise")) { "exercise must be present after level-down" } },
            { assert(!resp.get("exercise").isNull) { "exercise must not be null after level-down" } },
            { assert(resp.has("levelChanged")) { "levelChanged must be reported" } },
            { assert(resp.get("levelChanged").get("direction").asText() == "down") { "direction must be 'down'" } },
            { assert(resp.get("levelChanged").get("to").asInt() == 0) },
            { assert(resp.get("ladderState").get("recentScores").isEmpty) { "recentScores must reset on level-down" } },
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 0) }
        )
    }

    /**
     * When the player is already on level 0 and scores < 40%, there is no level below.
     * The API must still return an exercise and NOT crash.
     */
    @Test
    fun levelDown_atLowestLevel_staysAndContinues() {
        val resp = nextExercise(
            ladderCode = "default",
            levelIndex = 0,
            recentScores = listOf(0.1, 0.2, 0.15, 0.1, 0.2),
            overallScoreSum = 0.75,
            overallTotal = 5,
            lastScore = 0.2
        )

        assertAll(
            { assert(resp.has("exercise")) { "exercise must still be returned at lowest level" } },
            { assert(!resp.get("exercise").isNull) },
            // Stays on level 0, no demotion below 0
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 0) },
            // levelChanged should be null — we didn't actually change level
            { assert(resp.get("levelChanged").isNull || !resp.has("levelChanged") || resp.get("levelChanged").isNull) }
        )
    }

    /**
     * Scores between 40% and 75% keep the player on the same level.
     * Exercise must still be returned immediately.
     */
    @Test
    fun stayLevel_exerciseImmediatelyAvailable() {
        val resp = nextExercise(
            ladderCode = "default",
            levelIndex = 0,
            recentScores = listOf(0.55, 0.6, 0.5, 0.55, 0.6),
            overallScoreSum = 2.8,
            overallTotal = 5,
            lastScore = 0.6
        )

        assertAll(
            { assert(resp.has("exercise")) { "exercise must be present when staying at level" } },
            { assert(!resp.get("exercise").isNull) },
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 0) },
            // No level change
            { assert(!resp.has("levelChanged") || resp.get("levelChanged").isNull) { "No levelChanged when staying" } }
        )
    }
}

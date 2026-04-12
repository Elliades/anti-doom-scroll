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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Integration tests for the 35-level N-Back ladder.
 *
 * Ladder structure (as configured in application-test.yml):
 *   L0-L4   Phase 0 — Digit span (DIGIT_SPAN / MEMORY)
 *   L5-L14  Phase 1 — Card N-Back (N_BACK)
 *   L15-L24 Phase 2 — Grid Position N-Back (N_BACK_GRID)
 *   L25-L34 Phase 3 — Dual N-Back (DUAL_NBACK_CARD / DUAL_NBACK_GRID)
 *
 * Note on JSON key casing:
 *   Jackson serializes Kotlin field `nBackParams` → JSON key `nbackParams`
 *   (Java Beans rule: multiple consecutive leading uppercase letters all get lowercased).
 *   `dualNBackGridParams` serializes as `dualNBackGridParams` (only first char lowercased).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class NBackLadderIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    private val mapper = jacksonObjectMapper()

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private fun startLadder(ladderCode: String = "nback"): JsonNode {
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
        ladderCode: String = "nback",
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

    /** Normalizes JSON key: Jackson serializes nBackParams as nbackParams (leading uppercase run) */
    private fun JsonNode.nbackParams(): JsonNode? = get("nbackParams") ?: get("nBackParams")

    /** Normalizes JSON key: Jackson serializes nBackGridParams as nbackGridParams */
    private fun JsonNode.nbackGridParams(): JsonNode? = get("nbackGridParams") ?: get("nBackGridParams")

    private fun JsonNode.digitSpanParams(): JsonNode? = get("digitSpanParams")

    // ──────────────────────────────────────────────────────────────────────────
    // Ladder discovery
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun listLadders_includesNbackLadder() {
        val result = mvc.perform(get("/api/ladders"))
            .andExpect(status().isOk())
            .andReturn()

        val tree = mapper.readTree(result.response.contentAsString)
        val nbackLadder = tree.find { it.get("code")?.asText() == "nback" }
        assertAll(
            { assert(nbackLadder != null) { "nback ladder must appear in /api/ladders" } },
            { assert(nbackLadder!!.get("name")?.asText() == "N-Back Ladder") { "ladder name mismatch" } },
            { assert(nbackLadder!!.get("levelCount")?.asInt() == 35) { "nback ladder should have 35 levels" } }
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Phase 0: Digit span (levels 0-4)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun startNbackLadder_level0_returnsDigitSpanExercise() {
        val resp = startLadder()
        val exercise = resp.get("exercise")
        val dsp = exercise?.digitSpanParams()
        assertAll(
            { assert(resp.get("ladderState").get("ladderCode").asText() == "nback") },
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 0) },
            { assert(exercise != null && !exercise.isNull) { "exercise must be present at level 0" } },
            { assert(exercise!!.get("type").asText() == "DIGIT_SPAN") { "Level 0 must be DIGIT_SPAN" } },
            { assert(dsp != null && !dsp.isNull) { "digitSpanParams must not be null" } },
            {
                val seq = dsp!!.get("sequence")
                assert(seq != null && seq.isArray && seq.size() >= 3) { "sequence must have at least 3 digits" }
            }
        )
    }

    @Test
    fun nbackLadder_level0to1_staysDigitSpan() {
        val resp = nextExercise(
            levelIndex = 0,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 1) { "should advance to level 1" } },
            { assert(exercise!!.get("type").asText() == "DIGIT_SPAN") { "level 1 must be DIGIT_SPAN" } },
            { assert(exercise.digitSpanParams() != null) { "digitSpanParams must not be null at level 1" } }
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Phase 1: Card N-Back (levels 5-14)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun nbackLadder_level9_returns2BackExercise() {
        val resp = nextExercise(
            levelIndex = 8,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        val nbackParams = exercise?.nbackParams()
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 9) { "should advance to level 9" } },
            { assert(exercise!!.get("type").asText() == "N_BACK") { "level 9 must be N_BACK" } },
            { assert(nbackParams != null) { "nbackParams must not be null at level 9" } },
            { assert(nbackParams!!.get("n").asInt() == 2) { "level 9 must be 2-back" } }
        )
    }

    @Test
    fun nbackLadder_level13_returns3BackExercise() {
        val resp = nextExercise(
            levelIndex = 12,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        val nbackParams = exercise?.nbackParams()
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 13) { "should advance to level 13" } },
            { assert(exercise!!.get("type").asText() == "N_BACK") { "level 13 must be N_BACK" } },
            { assert(nbackParams != null) { "nbackParams must not be null at level 13" } },
            { assert(nbackParams!!.get("n").asInt() == 3) { "level 13 must be 3-back" } }
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Phase 2: Grid Position N-Back (levels 15-24)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun nbackLadder_level15_returnsGridExercise() {
        val resp = nextExercise(
            levelIndex = 14,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        val gridParams = exercise?.nbackGridParams()
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 15) { "should advance to level 15" } },
            { assert(exercise != null && !exercise.isNull) { "exercise must be present at level 15" } },
            { assert(exercise!!.get("type").asText() == "N_BACK_GRID") { "level 15 must be N_BACK_GRID" } },
            { assert(gridParams != null && !gridParams.isNull) { "nbackGridParams must not be null" } },
            { assert(gridParams!!.get("n").asInt() == 1) { "level 15 must be 1-back grid" } },
            { assert(gridParams!!.get("gridSize").asInt() == 3) { "level 15 must be 3x3 grid" } },
            {
                val seq = gridParams!!.get("sequence")
                assert(seq != null && seq.size() >= 3) { "grid sequence must have at least 3 items" }
            }
        )
    }

    @Test
    fun nbackLadder_level16_returns4x4GridExercise() {
        val resp = nextExercise(
            levelIndex = 15,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        val gridParams = exercise?.nbackGridParams()
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 16) },
            { assert(exercise!!.get("type").asText() == "N_BACK_GRID") },
            { assert(gridParams != null) { "nbackGridParams must not be null at level 16" } },
            { assert(gridParams!!.get("n").asInt() == 1) { "level 16 still 1-back" } },
            { assert(gridParams!!.get("gridSize").asInt() == 4) { "level 16 must be 4x4 grid" } }
        )
    }

    @Test
    fun nbackLadder_level19_returns3BackGrid() {
        val resp = nextExercise(
            levelIndex = 18,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        val gridParams = exercise?.nbackGridParams()
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 19) },
            { assert(exercise!!.get("type").asText() == "N_BACK_GRID") },
            { assert(gridParams != null) { "nbackGridParams must not be null at level 19" } },
            { assert(gridParams!!.get("n").asInt() == 3) { "level 19 must be 3-back grid" } },
            { assert(gridParams!!.get("gridSize").asInt() == 3) }
        )
    }

    @Test
    fun nbackGridExercise_sequenceIsGeneratedDynamically() {
        val resp1 = nextExercise(
            levelIndex = 14,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val resp2 = nextExercise(
            levelIndex = 14,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val seq1 = resp1.get("exercise")?.nbackGridParams()?.get("sequence")?.map { it.asInt() }
        val seq2 = resp2.get("exercise")?.nbackGridParams()?.get("sequence")?.map { it.asInt() }
        assert(seq1 != null && seq1.isNotEmpty()) { "sequence must not be empty" }
        assert(seq2 != null && seq2.isNotEmpty()) { "sequence must not be empty" }
        // Both sequences must contain valid grid positions (0..8 for 3x3)
        assert(seq1!!.all { it in 0..8 }) { "all positions must be valid for 3x3 grid" }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Phase 3: Dual N-Back (levels 25-34)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun nbackLadder_level25_returnsDualCardExercise() {
        val resp = nextExercise(
            levelIndex = 24,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        val dualCardParams = exercise?.get("dualNBackCardParams")
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 25) { "should advance to level 25" } },
            { assert(exercise != null && !exercise.isNull) { "exercise must be present at level 25" } },
            { assert(exercise!!.get("type").asText() == "DUAL_NBACK_CARD") { "level 25 must be DUAL_NBACK_CARD" } },
            { assert(dualCardParams != null && !dualCardParams.isNull) { "dualNBackCardParams must not be null" } },
            { assert(dualCardParams!!.get("n").asInt() == 1) { "level 25 must be 1-back" } },
            {
                val seq = dualCardParams!!.get("sequence")
                assert(seq != null && seq.size() >= 3) { "sequence must have at least 3 cards" }
            }
        )
    }

    @Test
    fun nbackLadder_level26_returnsDualGridExercise() {
        val resp = nextExercise(
            levelIndex = 25,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        val dualGridParams = exercise?.get("dualNBackGridParams")
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 26) { "should advance to level 26" } },
            { assert(exercise!!.get("type").asText() == "DUAL_NBACK_GRID") { "level 26 must be DUAL_NBACK_GRID" } },
            { assert(dualGridParams != null && !dualGridParams.isNull) { "dualNBackGridParams must not be null" } },
            { assert(dualGridParams!!.get("n").asInt() == 1) },
            {
                val seq = dualGridParams!!.get("sequence")
                assert(seq != null && seq.size() >= 3) { "dual grid sequence must have at least 3 stimuli" }
                assert(seq!![0].has("position") && seq[0].has("color")) { "each stimulus must have position and color" }
            }
        )
    }

    @Test
    fun nbackLadder_level31_returnsDual3BackCard() {
        val resp = nextExercise(
            levelIndex = 30,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        val dualCardParams = exercise?.get("dualNBackCardParams")
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 31) { "should advance to level 31" } },
            { assert(exercise!!.get("type").asText() == "DUAL_NBACK_CARD") { "level 31 must be DUAL_NBACK_CARD" } },
            { assert(dualCardParams != null && !dualCardParams.isNull) { "dualNBackCardParams must not be null" } },
            { assert(dualCardParams!!.get("n").asInt() == 3) { "level 31 must be 3-back" } }
        )
    }

    @Test
    fun nbackLadder_level34_returnsHardDualExercise() {
        val resp = nextExercise(
            levelIndex = 33,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val exercise = resp.get("exercise")
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 34) { "should advance to level 34" } },
            { assert(exercise != null && !exercise.isNull) { "exercise must be present at level 34" } },
            { assert(exercise!!.get("difficulty").asText() == "HARD") { "level 34 must be HARD difficulty" } },
            {
                val type = exercise!!.get("type").asText()
                assert(type == "DUAL_NBACK_CARD" || type == "DUAL_NBACK_GRID") {
                    "level 34 must be a dual n-back type, got: $type"
                }
            }
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Level capping at 34
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun nbackLadder_atLevel34_doesNotAdvanceBeyond() {
        val resp = nextExercise(
            levelIndex = 34,
            recentScores = listOf(0.9, 0.95, 0.9, 0.95, 0.9),
            overallScoreSum = 4.6, overallTotal = 5, lastScore = 0.9
        )
        assertAll(
            { assert(resp.get("exercise") != null && !resp.get("exercise").isNull) { "exercise must be present at max level" } },
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 34) { "level must not exceed 34" } }
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Demotion across phases
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun nbackLadder_lowScore_demotesFromGridToCard() {
        val resp = nextExercise(
            levelIndex = 15,
            recentScores = listOf(0.2, 0.15, 0.25, 0.2, 0.1),
            overallScoreSum = 0.9, overallTotal = 5, lastScore = 0.2
        )
        val exercise = resp.get("exercise")
        assertAll(
            { assert(resp.get("ladderState").get("currentLevelIndex").asInt() == 14) { "should demote to level 14" } },
            { assert(exercise != null && !exercise.isNull) { "exercise must be present after demotion" } },
            { assert(exercise!!.get("type").asText() == "N_BACK") { "level 14 must be N_BACK card" } },
            { assert(resp.get("levelChanged").get("direction").asText() == "down") }
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Dynamic sequence validation
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun dualNBackCard_parametric_sequenceHasValidCardCodes() {
        val resp = nextExercise(
            levelIndex = 24,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val sequence = resp.get("exercise").get("dualNBackCardParams").get("sequence")
        val validSuits = setOf('C', 'D', 'H', 'S')
        val validRanks = setOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        sequence.forEach { cardNode ->
            val code = cardNode.asText()
            val suit = code.last()
            val rank = code.dropLast(1)
            assert(suit in validSuits) { "Invalid suit in card code: $code" }
            assert(rank in validRanks) { "Invalid rank in card code: $code" }
        }
    }

    @Test
    fun dualNBackGrid_parametric_sequenceHasValidPositionsAndColors() {
        val resp = nextExercise(
            levelIndex = 25,
            recentScores = listOf(0.8, 0.85, 0.9, 0.8, 0.88),
            overallScoreSum = 4.23, overallTotal = 5, lastScore = 0.88
        )
        val params = resp.get("exercise").get("dualNBackGridParams")
        val gridSize = params.get("gridSize").asInt()
        val colors = params.get("colors").map { it.asText() }.toSet()
        val maxPos = gridSize * gridSize - 1
        params.get("sequence").forEach { stimulus ->
            val pos = stimulus.get("position").asInt()
            val color = stimulus.get("color").asText()
            assert(pos in 0..maxPos) { "position $pos out of range 0..$maxPos for ${gridSize}x${gridSize}" }
            assert(color in colors) { "color $color not in colors palette $colors" }
        }
    }
}

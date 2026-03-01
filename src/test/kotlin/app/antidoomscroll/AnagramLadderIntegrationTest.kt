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

/**
 * Integration tests for the anagram ladder (ladderCode=anagram).
 *
 * Test config: 5 levels mirroring the production 30-level structure:
 *   0: ULTRA_EASY       (2-3 letter words)
 *   1: ULTRA_EASY+EASY  (bridge)
 *   2: EASY             (3-4 letter words)
 *   3: EASY+MEDIUM      (bridge)
 *   4: MEDIUM           (4-5 letter words)
 *
 * data.sql seeds one ANAGRAM exercise per difficulty for subject WORD.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class AnagramLadderIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    // ------------------------------------------------------------------
    // Start session
    // ------------------------------------------------------------------

    @Test
    fun startAnagramLadderSession_returnsAnagramExerciseAtLevelZero() {
        mvc.perform(
            get("/api/session/start")
                .param("mode", "ladder")
                .param("ladderCode", "anagram")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mode").value("ladder"))
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.exercise.type").value("ANAGRAM"))
            .andExpect(jsonPath("$.ladderState.ladderCode").value("anagram"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.ladderState.recentScores").isArray())
            .andExpect(jsonPath("$.ladderState.overallScoreSum").value(0))
            .andExpect(jsonPath("$.ladderState.overallTotal").value(0))
    }

    // ------------------------------------------------------------------
    // Level advancement
    // ------------------------------------------------------------------

    @Test
    fun anagramLadderNext_highScores_advancesToBridgeLevel() {
        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "ladderState": {
                            "ladderCode": "anagram",
                            "currentLevelIndex": 0,
                            "recentScores": [0.8, 0.9, 0.85, 0.9, 0.88],
                            "overallScoreSum": 4.33,
                            "overallTotal": 5
                        },
                        "lastScore": 0.88
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exercise").exists())
            .andExpect(jsonPath("$.exercise.type").value("ANAGRAM"))
            .andExpect(jsonPath("$.ladderState.ladderCode").value("anagram"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(1))
            .andExpect(jsonPath("$.ladderState.recentScores").isEmpty)
            .andExpect(jsonPath("$.levelChanged.direction").value("up"))
    }

    @Test
    fun anagramLadderNext_lowScores_staysAtLevelZero() {
        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "ladderState": {
                            "ladderCode": "anagram",
                            "currentLevelIndex": 0,
                            "recentScores": [0.5, 0.6, 0.55, 0.5, 0.6],
                            "overallScoreSum": 2.75,
                            "overallTotal": 5
                        },
                        "lastScore": 0.6
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exercise.type").value("ANAGRAM"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
    }

    @Test
    fun anagramLadderNext_veryLowScores_demotesFromBridgeToUltraEasy() {
        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "ladderState": {
                            "ladderCode": "anagram",
                            "currentLevelIndex": 1,
                            "recentScores": [0.3, 0.25, 0.35, 0.2, 0.3],
                            "overallScoreSum": 1.4,
                            "overallTotal": 5
                        },
                        "lastScore": 0.3
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exercise.type").value("ANAGRAM"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(0))
            .andExpect(jsonPath("$.ladderState.recentScores").isEmpty)
            .andExpect(jsonPath("$.levelChanged.direction").value("down"))
    }

    @Test
    fun anagramLadderNext_highScoresAtBridge_advancesToEasyPhase() {
        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "ladderState": {
                            "ladderCode": "anagram",
                            "currentLevelIndex": 1,
                            "recentScores": [0.8, 0.9, 0.85, 0.9, 0.88],
                            "overallScoreSum": 4.33,
                            "overallTotal": 10
                        },
                        "lastScore": 0.88
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exercise.type").value("ANAGRAM"))
            .andExpect(jsonPath("$.ladderState.currentLevelIndex").value(2))
            .andExpect(jsonPath("$.levelChanged.direction").value("up"))
    }

    // ------------------------------------------------------------------
    // Discovery: GET /api/ladders includes anagram
    // ------------------------------------------------------------------

    @Test
    fun listLadders_includesAnagramLadder() {
        mvc.perform(get("/api/ladders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.code == 'anagram')]").exists())
            .andExpect(jsonPath("$[?(@.code == 'anagram')].name").value("Anagram Ladder"))
            .andExpect(jsonPath("$[?(@.code == 'anagram')].levelCount").value(5))
    }

    // ------------------------------------------------------------------
    // Exercise type is always ANAGRAM throughout the ladder
    // ------------------------------------------------------------------

    @Test
    fun anagramLadderNext_atMediumLevel_returnsAnagramExercise() {
        mvc.perform(
            post("/api/session/ladder/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "ladderState": {
                            "ladderCode": "anagram",
                            "currentLevelIndex": 4,
                            "recentScores": [],
                            "overallScoreSum": 0,
                            "overallTotal": 0
                        },
                        "lastScore": 0.7
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exercise.type").value("ANAGRAM"))
            .andExpect(jsonPath("$.ladderState.ladderCode").value("anagram"))
    }
}

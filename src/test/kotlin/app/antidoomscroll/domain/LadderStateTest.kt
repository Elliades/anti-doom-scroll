package app.antidoomscroll.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LadderStateTest {

    @Test
    fun withScoreAdded_appendsScoreAndTrimsToMaxRecent() {
        val state = LadderState(
            ladderCode = "default",
            currentLevelIndex = 0,
            recentScores = listOf(0.8, 0.9),
            overallScoreSum = 1.7,
            overallTotal = 2
        )
        val updated = state.withScoreAdded(0.85, 5)

        assertThat(updated.recentScores).containsExactly(0.8, 0.9, 0.85)
        assertThat(updated.overallScoreSum).isEqualTo(2.55)
        assertThat(updated.overallTotal).isEqualTo(3)
    }

    @Test
    fun withScoreAdded_keepsOnlyLastN() {
        val state = LadderState(
            ladderCode = "default",
            currentLevelIndex = 0,
            recentScores = listOf(0.8, 0.9, 0.85, 0.9, 0.88),
            overallScoreSum = 4.33,
            overallTotal = 5
        )
        val updated = state.withScoreAdded(0.9, 5)

        assertThat(updated.recentScores).hasSize(5)
        assertThat(updated.recentScores).containsExactly(0.9, 0.85, 0.9, 0.88, 0.9)
    }

    @Test
    fun currentLevelScorePercent_returnsAverageOrNull() {
        assertThat(LadderState("d", 0, emptyList(), 0.0, 0).currentLevelScorePercent()).isNull()
        assertThat(LadderState("d", 0, listOf(0.8, 1.0), 0.0, 0).currentLevelScorePercent()).isEqualTo(0.9)
    }

    @Test
    fun overallScorePercent_returnsFraction() {
        assertThat(LadderState("d", 0, emptyList(), 0.0, 0).overallScorePercent()).isEqualTo(0.0)
        assertThat(LadderState("d", 0, emptyList(), 4.5, 5).overallScorePercent()).isEqualTo(0.9)
    }

    @Test
    fun withLevelChanged_resetsRecentScores() {
        val state = LadderState(
            ladderCode = "default",
            currentLevelIndex = 1,
            recentScores = listOf(0.8, 0.9),
            overallScoreSum = 1.7,
            overallTotal = 2
        )
        val updated = state.withLevelChanged(0)

        assertThat(updated.currentLevelIndex).isEqualTo(0)
        assertThat(updated.recentScores).isEmpty()
    }
}

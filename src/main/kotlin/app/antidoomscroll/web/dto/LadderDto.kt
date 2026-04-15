package app.antidoomscroll.web.dto

/**
 * Ladder session state (client holds and resubmits).
 */
data class LadderStateDto(
    val ladderCode: String,
    val currentLevelIndex: Int,
    val recentScores: List<Double>,
    val overallScoreSum: Double,
    val overallTotal: Int
)

/**
 * Response when starting a ladder session.
 */
data class LadderSessionResponseDto(
    val profileId: String,
    val mode: String = "ladder",
    val exercise: ExerciseDto,
    val ladderState: LadderStateDto,
    val levelCount: Int,
    val sessionDefaultSeconds: Int,
    val lowBatteryModeSeconds: Int
)

/**
 * Request body for getting next ladder exercise.
 */
data class LadderNextRequestDto(
    val profileId: String? = null,
    val ladderState: LadderStateDto,
    val lastScore: Double
)

/**
 * Response for next ladder exercise.
 */
data class LadderNextResponseDto(
    val exercise: ExerciseDto?,
    val ladderState: LadderStateDto,
    val levelChanged: LevelChangeDto? = null
)

data class LevelChangeDto(
    val from: Int,
    val to: Int,
    val direction: String
)

// --- Ladder Mix ---

data class PerLadderStateDto(
    val recentScores: List<Double>,
    val overallScoreSum: Double,
    val overallTotal: Int
)

data class LadderMixStateDto(
    val mixCode: String,
    val ladderCodes: List<String>,
    val currentLevelIndex: Int,
    val perLadderStates: Map<String, PerLadderStateDto>,
    val nextLadderIndex: Int
)

data class LadderMixSessionResponseDto(
    val profileId: String,
    val mode: String = "ladderMix",
    val exercise: ExerciseDto,
    val ladderMixState: LadderMixStateDto,
    val levelCount: Int,
    val sessionDefaultSeconds: Int,
    val lowBatteryModeSeconds: Int
)

data class LadderMixNextRequestDto(
    val profileId: String? = null,
    val ladderMixState: LadderMixStateDto,
    val lastCompletedLadderCode: String,
    val lastScore: Double
)

data class LadderMixNextResponseDto(
    val exercise: ExerciseDto?,
    val ladderMixState: LadderMixStateDto,
    val levelChanged: LevelChangeDto? = null
)

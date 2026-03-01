package app.antidoomscroll.web

import app.antidoomscroll.application.GetNextLadderExerciseUseCase
import app.antidoomscroll.application.GetNextLadderMixExerciseUseCase
import app.antidoomscroll.application.StartLadderMixSessionUseCase
import app.antidoomscroll.application.StartLadderSessionUseCase
import app.antidoomscroll.application.StartSessionUseCase
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.LadderMixState
import app.antidoomscroll.domain.LadderState
import app.antidoomscroll.web.dto.LadderMixNextRequestDto
import app.antidoomscroll.web.dto.LadderMixNextResponseDto
import app.antidoomscroll.web.dto.LadderMixSessionResponseDto
import app.antidoomscroll.web.dto.LadderMixStateDto
import app.antidoomscroll.web.dto.LadderNextRequestDto
import app.antidoomscroll.web.dto.LadderNextResponseDto
import app.antidoomscroll.web.dto.LadderSessionResponseDto
import app.antidoomscroll.web.dto.LadderStateDto
import app.antidoomscroll.web.dto.LevelChangeDto
import app.antidoomscroll.web.dto.PerLadderStateDto
import app.antidoomscroll.web.dto.SessionResponseDto
import app.antidoomscroll.web.dto.SessionStepDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller: start session (reopen flow) and ladder mode.
 */
@RestController
@RequestMapping("/api/session")
class SessionController(
    private val startSessionUseCase: StartSessionUseCase,
    private val startLadderSessionUseCase: StartLadderSessionUseCase,
    private val startLadderMixSessionUseCase: StartLadderMixSessionUseCase,
    private val getNextLadderExerciseUseCase: GetNextLadderExerciseUseCase,
    private val getNextLadderMixExerciseUseCase: GetNextLadderMixExerciseUseCase,
    private val exerciseDtoMapper: ExerciseDtoMapper
) {

    @GetMapping("/start", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startSession(
        @RequestParam(required = false) profileId: String?,
        @RequestParam(required = false) preferType: String?,
        @RequestParam(required = false) mode: String?,
        @RequestParam(required = false) ladderCode: String?,
        @RequestParam(required = false) ladderMixCode: String?
    ): ResponseEntity<*> {
        return when (mode?.lowercase()) {
            "laddermix" -> {
                val code = ladderMixCode ?: return ResponseEntity.badRequest().build<Unit>()
                val result = startLadderMixSessionUseCase.start(profileId, code)
                ResponseEntity.ok(toLadderMixSessionResponseDto(result))
            }
            "ladder" -> {
                val code = ladderCode ?: "default"
                val result = startLadderSessionUseCase.start(profileId, code)
                ResponseEntity.ok(toLadderSessionResponseDto(result))
            }
            "openapp" -> {
                val result = startSessionUseCase.startOpenAppSession(profileId)
                ResponseEntity.ok(toSessionResponseDto(result))
            }
            else -> {
                val type = preferType?.takeIf { it.isNotBlank() }?.let { runCatching { ExerciseType.valueOf(it) }.getOrNull() }
                val result = startSessionUseCase.startSession(profileId, type)
                ResponseEntity.ok(toSessionResponseDto(result))
            }
        }
    }

    @PostMapping("/ladder/next", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun ladderNext(@RequestBody body: LadderNextRequestDto): ResponseEntity<LadderNextResponseDto> {
        val state = toLadderState(body.ladderState)
        val result = getNextLadderExerciseUseCase.getNext(state, body.lastScore)
            ?: return ResponseEntity.notFound().build()
        val exerciseDto = result.exercise?.let { exerciseDtoMapper.toExerciseDto(it, result.subjectCode) }
        return ResponseEntity.ok(
            LadderNextResponseDto(
                exercise = exerciseDto,
                ladderState = toLadderStateDto(result.ladderState),
                levelChanged = result.levelChanged?.let { LevelChangeDto(it.from, it.to, it.direction) }
            )
        )
    }

    @PostMapping("/ladder-mix/next", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun ladderMixNext(@RequestBody body: LadderMixNextRequestDto): ResponseEntity<LadderMixNextResponseDto> {
        val state = toLadderMixState(body.ladderMixState)
        val result = getNextLadderMixExerciseUseCase.getNext(state, body.lastCompletedLadderCode, body.lastScore)
            ?: return ResponseEntity.notFound().build()
        val exerciseDto = result.exercise?.let { exerciseDtoMapper.toExerciseDto(it, result.subjectCode) }
        return ResponseEntity.ok(
            LadderMixNextResponseDto(
                exercise = exerciseDto,
                ladderMixState = toLadderMixStateDto(result.ladderMixState),
                levelChanged = result.levelChanged?.let { LevelChangeDto(it.from, it.to, it.direction) }
            )
        )
    }

    private fun toSessionResponseDto(result: StartSessionUseCase.SessionResult): SessionResponseDto =
        SessionResponseDto(
            profileId = result.profileId,
            steps = result.steps.map { stepWithCode ->
                SessionStepDto(
                    stepIndex = stepWithCode.step.stepIndex,
                    difficulty = stepWithCode.step.difficulty.name,
                    exercise = exerciseDtoMapper.toExerciseDto(stepWithCode.step.exercise, stepWithCode.subjectCode)
                )
            },
            sessionDefaultSeconds = result.sessionDefaultSeconds,
            lowBatteryModeSeconds = result.lowBatteryModeSeconds
        )

    private fun toLadderSessionResponseDto(result: StartLadderSessionUseCase.LadderSessionResult): LadderSessionResponseDto =
        LadderSessionResponseDto(
            profileId = result.profileId,
            exercise = exerciseDtoMapper.toExerciseDto(result.exercise, result.subjectCode),
            ladderState = toLadderStateDto(result.ladderState),
            sessionDefaultSeconds = result.sessionDefaultSeconds,
            lowBatteryModeSeconds = result.lowBatteryModeSeconds
        )

    private fun toLadderStateDto(s: LadderState): LadderStateDto =
        LadderStateDto(
            ladderCode = s.ladderCode,
            currentLevelIndex = s.currentLevelIndex,
            recentScores = s.recentScores,
            overallScoreSum = s.overallScoreSum,
            overallTotal = s.overallTotal
        )

    private fun toLadderState(d: LadderStateDto): LadderState =
        LadderState(
            ladderCode = d.ladderCode,
            currentLevelIndex = d.currentLevelIndex,
            recentScores = d.recentScores,
            overallScoreSum = d.overallScoreSum,
            overallTotal = d.overallTotal
        )

    private fun toLadderMixSessionResponseDto(result: StartLadderMixSessionUseCase.LadderMixSessionResult): LadderMixSessionResponseDto =
        LadderMixSessionResponseDto(
            profileId = result.profileId,
            exercise = exerciseDtoMapper.toExerciseDto(result.exercise, result.subjectCode),
            ladderMixState = toLadderMixStateDto(result.ladderMixState),
            sessionDefaultSeconds = result.sessionDefaultSeconds,
            lowBatteryModeSeconds = result.lowBatteryModeSeconds
        )

    private fun toLadderMixStateDto(s: LadderMixState): LadderMixStateDto =
        LadderMixStateDto(
            mixCode = s.mixCode,
            ladderCodes = s.ladderCodes,
            currentLevelIndex = s.currentLevelIndex,
            perLadderStates = s.perLadderStates.mapValues { (_, v) ->
                PerLadderStateDto(
                    recentScores = v.recentScores,
                    overallScoreSum = v.overallScoreSum,
                    overallTotal = v.overallTotal
                )
            },
            nextLadderIndex = s.nextLadderIndex
        )

    private fun toLadderMixState(d: LadderMixStateDto): LadderMixState =
        LadderMixState(
            mixCode = d.mixCode,
            ladderCodes = d.ladderCodes,
            currentLevelIndex = d.currentLevelIndex,
            perLadderStates = d.perLadderStates.mapValues { (_, v) ->
                LadderMixState.PerLadderState(
                    recentScores = v.recentScores,
                    overallScoreSum = v.overallScoreSum,
                    overallTotal = v.overallTotal
                )
            },
            nextLadderIndex = d.nextLadderIndex
        )
}

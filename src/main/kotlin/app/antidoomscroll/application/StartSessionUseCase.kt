package app.antidoomscroll.application

import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.application.port.ProfilePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.SessionStep
import app.antidoomscroll.domain.UserProfile
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Start a session: step 1 ultra-easy (cached, <1s), step 2 easy/medium by subject, step 3 optional hard.
 * Uses subjects for scaling: daily "axes" = subject codes.
 * OpenApp session: 3 random ultra-easy/easy exercises from all subjects (app start flow).
 */
@Service
class StartSessionUseCase(
    private val exercisePort: ExercisePort,
    private val profilePort: ProfilePort,
    private val subjectPort: SubjectPort
) {

    private val openAppExerciseCount = 3

    /**
     * OpenApp session: 3 random ultra-easy or easy exercises from all registered subjects.
     * Used when the app starts to begin with ultra-easy tasks.
     */
    fun startOpenAppSession(profileIdOpt: String?): SessionResult {
        val profile = profileIdOpt
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?.let { profilePort.findById(it) }
            ?: profilePort.getOrCreateAnonymousProfile()

        val exercises = exercisePort.findRandomUltraEasyOrEasy(openAppExerciseCount)
        val steps = exercises.mapIndexed { index, ex -> SessionStep.of(index + 1, ex) }

        val subjectCodesById = steps.map { it.exercise.subjectId }.toSet()
            .mapNotNull { id -> subjectPort.findById(id)?.let { id to it.code } }
            .toMap()

        return SessionResult(
            profileId = profile.id.toString(),
            steps = steps.map { step -> StepWithSubjectCode(step, subjectCodesById[step.exercise.subjectId]) },
            sessionDefaultSeconds = profile.sessionDefaultSeconds,
            lowBatteryModeSeconds = profile.lowBatteryModeSeconds
        )
    }

    /**
     * Returns at least step 1 (ultra-easy). Steps 2 and 3 may be empty if no exercises.
     * First subject comes from profile.dailyAxes (subject codes) or first available subject.
     * @param preferType if set (e.g. N_BACK), returns that exercise type for step 1 when available.
     */
    fun startSession(profileIdOpt: String?, preferType: ExerciseType? = null): SessionResult {
        val profile: UserProfile = profileIdOpt
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?.let { profilePort.findById(it) }
            ?: profilePort.getOrCreateAnonymousProfile()

        val steps = mutableListOf<SessionStep>()

        // First subject: from daily subject codes (profile.dailyAxes) or default
        val firstSubjectCode = profile.dailyAxes.firstOrNull() ?: "default"
        val firstSubject = subjectPort.findByCode(firstSubjectCode) ?: subjectPort.listAll().firstOrNull()

        // Step 1: ultra-easy (success guaranteed, fast)
        val step1 = preferType?.let { exercisePort.findOneUltraEasyByType(it) }
            ?: exercisePort.findOneUltraEasy(firstSubject?.code)
        step1?.let { ex ->
            steps.add(SessionStep.of(1, ex))
        }

        // Step 2: easy or medium from first subject
        firstSubject?.let { subject ->
            exercisePort.findBySubjectAndDifficulty(subject.id, Difficulty.EASY, 1).firstOrNull()
                ?: exercisePort.findBySubjectAndDifficulty(subject.id, Difficulty.MEDIUM, 1).firstOrNull()
                ?.let { ex -> steps.add(SessionStep.of(2, ex)) }
        }

        // Resolve subject codes for response (one lookup per subject id used)
        val subjectCodesById = steps.map { it.exercise.subjectId }.toSet()
            .mapNotNull { id -> subjectPort.findById(id)?.let { id to it.code } }
            .toMap()

        return SessionResult(
            profileId = profile.id.toString(),
            steps = steps.map { step ->
                StepWithSubjectCode(step, subjectCodesById[step.exercise.subjectId])
            },
            sessionDefaultSeconds = profile.sessionDefaultSeconds,
            lowBatteryModeSeconds = profile.lowBatteryModeSeconds
        )
    }

    /**
     * Session for one chapter (subject): ultra-easy then easy/medium from that subject.
     * Used by journey CHAPTER_EXERCISES step.
     */
    fun startSessionForSubject(profileIdOpt: String?, subjectCode: String): SessionResult {
        val profile = profileIdOpt
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?.let { profilePort.findById(it) }
            ?: profilePort.getOrCreateAnonymousProfile()
        val subject = subjectPort.findByCode(subjectCode) ?: return SessionResult(
            profileId = profile.id.toString(),
            steps = emptyList(),
            sessionDefaultSeconds = profile.sessionDefaultSeconds,
            lowBatteryModeSeconds = profile.lowBatteryModeSeconds
        )
        val steps = mutableListOf<SessionStep>()
        exercisePort.findOneUltraEasy(subject.code)?.let { ex -> steps.add(SessionStep.of(1, ex)) }
        exercisePort.findBySubjectAndDifficulty(subject.id, Difficulty.EASY, 1).firstOrNull()
            ?: exercisePort.findBySubjectAndDifficulty(subject.id, Difficulty.MEDIUM, 1).firstOrNull()
            ?.let { ex -> steps.add(SessionStep.of(2, ex)) }
        val subjectCodesById = steps.map { it.exercise.subjectId }.toSet()
            .mapNotNull { id -> subjectPort.findById(id)?.let { id to it.code } }
            .toMap()
        return SessionResult(
            profileId = profile.id.toString(),
            steps = steps.map { step -> StepWithSubjectCode(step, subjectCodesById[step.exercise.subjectId]) },
            sessionDefaultSeconds = profile.sessionDefaultSeconds,
            lowBatteryModeSeconds = profile.lowBatteryModeSeconds
        )
    }

    data class StepWithSubjectCode(val step: SessionStep, val subjectCode: String?)

    data class SessionResult(
        val profileId: String,
        val steps: List<StepWithSubjectCode>,
        val sessionDefaultSeconds: Int,
        val lowBatteryModeSeconds: Int
    )
}

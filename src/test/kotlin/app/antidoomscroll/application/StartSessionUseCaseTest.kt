package app.antidoomscroll.application

import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.application.port.ProfilePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.Subject
import app.antidoomscroll.domain.SubjectScoringConfig
import app.antidoomscroll.domain.UserProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class StartSessionUseCaseTest {

    @Mock
    private lateinit var exercisePort: ExercisePort

    @Mock
    private lateinit var profilePort: ProfilePort

    @Mock
    private lateinit var subjectPort: SubjectPort

    private lateinit var useCase: StartSessionUseCase

    private val subjectId = UUID.fromString("b0000000-0000-0000-0000-000000000001")
    private val profileId = UUID.fromString("a0000000-0000-0000-0000-000000000001")

    private fun initUseCase() {
        useCase = StartSessionUseCase(exercisePort, profilePort, subjectPort)
    }

    private fun exercise(id: UUID, difficulty: Difficulty = Difficulty.ULTRA_EASY) = Exercise(
        id = id,
        subjectId = subjectId,
        type = ExerciseType.FLASHCARD_QA,
        difficulty = difficulty,
        prompt = "Q $id",
        expectedAnswers = listOf("A"),
        timeLimitSeconds = 30,
        exerciseParams = null
    )

    @Test
    fun startOpenAppSession_returnsThreeStepsWhenPortReturnsThreeExercises() {
        initUseCase()
        val ex1 = exercise(UUID.fromString("a0000000-0000-0000-0000-000000000001"))
        val ex2 = exercise(UUID.fromString("a0000000-0000-0000-0000-000000000002"), Difficulty.EASY)
        val ex3 = exercise(UUID.fromString("a0000000-0000-0000-0000-000000000003"))
        `when`(exercisePort.findRandomUltraEasyOrEasy(3)).thenReturn(listOf(ex1, ex2, ex3))

        val profile = UserProfile(
            id = profileId,
            displayName = null,
            timezone = ZoneId.of("UTC"),
            dailyAxes = emptyList(),
            sessionDefaultSeconds = 180,
            lowBatteryModeSeconds = 45,
            anonymous = true
        )
        `when`(profilePort.getOrCreateAnonymousProfile()).thenReturn(profile)

        val subject = Subject(
            id = subjectId,
            code = "default",
            name = "Default",
            description = null,
            parentSubjectId = null,
            scoringConfig = SubjectScoringConfig(),
            createdAt = Instant.now()
        )
        `when`(subjectPort.findById(subjectId)).thenReturn(subject)

        val result = useCase.startOpenAppSession(null)

        assertEquals(profileId.toString(), result.profileId)
        assertEquals(3, result.steps.size)
        assertEquals(180, result.sessionDefaultSeconds)
        assertEquals(45, result.lowBatteryModeSeconds)
        result.steps.forEachIndexed { index, stepWithCode ->
            assertEquals(index + 1, stepWithCode.step.stepIndex)
            assertTrue(
                stepWithCode.step.difficulty == Difficulty.ULTRA_EASY ||
                    stepWithCode.step.difficulty == Difficulty.EASY
            )
        }
        verify(exercisePort).findRandomUltraEasyOrEasy(eq(3))
    }

    @Test
    fun startOpenAppSession_returnsFewerStepsWhenPortReturnsFewerExercises() {
        initUseCase()
        val ex1 = exercise(UUID.fromString("a0000000-0000-0000-0000-000000000001"))
        `when`(exercisePort.findRandomUltraEasyOrEasy(3)).thenReturn(listOf(ex1))

        val profile = UserProfile(
            id = profileId,
            displayName = null,
            timezone = ZoneId.of("UTC"),
            dailyAxes = emptyList(),
            sessionDefaultSeconds = 180,
            lowBatteryModeSeconds = 45,
            anonymous = true
        )
        `when`(profilePort.getOrCreateAnonymousProfile()).thenReturn(profile)
        `when`(subjectPort.findById(subjectId)).thenReturn(
            Subject(
                id = subjectId,
                code = "default",
                name = "Default",
                description = null,
                parentSubjectId = null,
                scoringConfig = SubjectScoringConfig(),
                createdAt = Instant.now()
            )
        )

        val result = useCase.startOpenAppSession(null)

        assertEquals(1, result.steps.size)
        assertEquals(1, result.steps[0].step.stepIndex)
    }

    @Test
    fun startOpenAppSession_returnsEmptyStepsWhenPortReturnsNoExercises() {
        initUseCase()
        `when`(exercisePort.findRandomUltraEasyOrEasy(3)).thenReturn(emptyList())
        val profile = UserProfile(
            id = profileId,
            displayName = null,
            timezone = ZoneId.of("UTC"),
            dailyAxes = emptyList(),
            sessionDefaultSeconds = 180,
            lowBatteryModeSeconds = 45,
            anonymous = true
        )
        `when`(profilePort.getOrCreateAnonymousProfile()).thenReturn(profile)

        val result = useCase.startOpenAppSession(null)

        assertEquals(0, result.steps.size)
    }
}

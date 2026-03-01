package app.antidoomscroll.application

import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Exercise
import org.springframework.stereotype.Service

/**
 * Returns a single N-back exercise for the given level (1, 2, or 3).
 * Used by the dedicated N-back controller; controller uses ExerciseDtoMapper for DTO (including nBackParams resolution).
 */
@Service
class GetNBackExerciseByLevelUseCase(
    private val exercisePort: ExercisePort,
    private val subjectPort: SubjectPort
) {

    /**
     * @param level 1 = 1-back, 2 = 2-back, 3 = 3-back
     * @return domain Exercise or null if not found
     */
    fun getByLevel(level: Int): Exercise? {
        if (level !in 1..3) return null
        return exercisePort.findNBackByLevel(level)
    }
}

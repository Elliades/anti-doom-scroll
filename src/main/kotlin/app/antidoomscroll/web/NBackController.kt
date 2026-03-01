package app.antidoomscroll.web

import app.antidoomscroll.application.GetNBackExerciseByLevelUseCase
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.web.dto.ExerciseDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Dedicated entry for N-back exercises by level.
 * GET /api/nback/1 (1-back), /api/nback/2 (2-back), /api/nback/3 (3-back).
 * Uses ExerciseDtoMapper so nBackParams are always resolved (e.g. via findExerciseParamsById when cached exercise has null params).
 */
@RestController
@RequestMapping("/api/nback")
class NBackController(
    private val getNBackExerciseByLevelUseCase: GetNBackExerciseByLevelUseCase,
    private val subjectPort: SubjectPort,
    private val exerciseDtoMapper: ExerciseDtoMapper
) {

    @GetMapping(
        path = ["/{level}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getByLevel(@PathVariable level: Int): ResponseEntity<ExerciseDto?> {
        val exercise = getNBackExerciseByLevelUseCase.getByLevel(level) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        val subjectCode = subjectPort.findById(exercise.subjectId)?.code
        val dto = exerciseDtoMapper.toExerciseDto(exercise, subjectCode)
        return ResponseEntity.ok(dto)
    }
}

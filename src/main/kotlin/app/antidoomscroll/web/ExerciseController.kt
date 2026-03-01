package app.antidoomscroll.web

import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.web.dto.ExerciseDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * REST controller: list all exercises or get a single exercise by ID.
 * GET /api/exercises — returns all exercises (all subjects).
 * GET /api/exercises/{id} — returns exercise DTO or 404.
 */
@RestController
@RequestMapping("/api/exercises")
class ExerciseController(
    private val exercisePort: ExercisePort,
    private val subjectPort: SubjectPort,
    private val exerciseDtoMapper: ExerciseDtoMapper
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listAll(): List<ExerciseDto> =
        subjectPort.listAll().flatMap { subject ->
            exercisePort.findBySubjectId(subject.id, 500)
                .map { exerciseDtoMapper.toExerciseDto(it, subject.code) }
        }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: UUID): ResponseEntity<ExerciseDto?> {
        val exercise = exercisePort.findById(id) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        val subjectCode = subjectPort.findById(exercise.subjectId)?.code
        val dto = exerciseDtoMapper.toExerciseDto(exercise, subjectCode)
        return ResponseEntity.ok(dto)
    }
}

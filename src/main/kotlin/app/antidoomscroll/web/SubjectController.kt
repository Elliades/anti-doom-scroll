package app.antidoomscroll.web

import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Subject
import app.antidoomscroll.web.dto.SubjectDto
import app.antidoomscroll.web.dto.SubjectScoringConfigDto
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller: list and get subjects. Enables adding subjects/exercises without code change.
 */
@RestController
@RequestMapping("/api/subjects")
class SubjectController(
    private val subjectPort: SubjectPort
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): List<SubjectDto> =
        subjectPort.listAll().map(::toDto)

    @GetMapping("/{code}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByCode(@PathVariable code: String): SubjectDto? =
        subjectPort.findByCode(code)?.let(::toDto)

    private fun toDto(s: Subject): SubjectDto =
        SubjectDto(
            id = s.id,
            code = s.code,
            name = s.name,
            description = s.description,
            parentSubjectId = s.parentSubjectId,
            scoringConfig = SubjectScoringConfigDto(
                accuracyType = s.scoringConfig.accuracyType.name,
                speedTargetMs = s.scoringConfig.speedTargetMs,
                confidenceWeight = s.scoringConfig.confidenceWeight,
                streakBonusCap = s.scoringConfig.streakBonusCap,
                partialMatchThreshold = s.scoringConfig.partialMatchThreshold
            )
        )
}

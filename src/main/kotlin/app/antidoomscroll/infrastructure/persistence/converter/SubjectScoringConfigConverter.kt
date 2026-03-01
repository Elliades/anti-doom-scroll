package app.antidoomscroll.infrastructure.persistence.converter

import app.antidoomscroll.domain.SubjectScoringConfig
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Converts SubjectScoringConfig to/from JSON string for DB storage.
 * Ensures H2 (VARCHAR) and PostgreSQL (jsonb) both deserialize correctly.
 */
@Converter
class SubjectScoringConfigConverter : AttributeConverter<SubjectScoringConfig, String?> {

    private val mapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: SubjectScoringConfig?): String? =
        if (attribute == null) null else mapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String?): SubjectScoringConfig {
        if (dbData == null || dbData.isBlank()) return SubjectScoringConfig()
        return try {
            mapper.readValue(dbData, SubjectScoringConfig::class.java)
        } catch (e: Exception) {
            SubjectScoringConfig()
        }
    }
}

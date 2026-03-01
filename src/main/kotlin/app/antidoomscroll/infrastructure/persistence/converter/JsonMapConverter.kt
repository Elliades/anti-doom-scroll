package app.antidoomscroll.infrastructure.persistence.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Converts Map<String, Any?> to/from JSON string for DB storage.
 * Ensures H2 (VARCHAR) and PostgreSQL (jsonb) both deserialize correctly when driver returns String.
 */
@Converter
class JsonMapConverter : AttributeConverter<Map<String, Any?>, String?> {

    private val mapper = ObjectMapper()
    private val typeRef = object : TypeReference<Map<String, Any?>>() {}

    override fun convertToDatabaseColumn(attribute: Map<String, Any?>?): String? =
        if (attribute == null) null else mapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String?): Map<String, Any?>? {
        if (dbData == null || dbData.isBlank()) return null
        return try {
            mapper.readValue(dbData, typeRef)
        } catch (e: Exception) {
            null
        }
    }
}

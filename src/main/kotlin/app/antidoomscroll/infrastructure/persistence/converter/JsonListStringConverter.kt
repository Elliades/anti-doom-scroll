package app.antidoomscroll.infrastructure.persistence.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Converts List<String> to/from JSON string for DB storage.
 * Ensures H2 (which returns VARCHAR as String) and PostgreSQL (jsonb as String) both deserialize correctly.
 */
@Converter
class JsonListStringConverter : AttributeConverter<List<String>, String?> {

    private val mapper = ObjectMapper()
    private val typeRef = object : TypeReference<List<String>>() {}

    override fun convertToDatabaseColumn(attribute: List<String>?): String? =
        if (attribute == null) null else mapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String?): List<String>? {
        if (dbData == null || dbData.isBlank()) return null
        return try {
            mapper.readValue(dbData, typeRef)
        } catch (e: Exception) {
            null
        }
    }
}

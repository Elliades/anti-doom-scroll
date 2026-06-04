package app.antidoomscroll

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class RememberNumberIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    private val mapper = ObjectMapper()

    @Test
    fun getExerciseById_REMEMBER_NUMBER_ULTRA_EASY_returnsParams() {
        mvc.perform(get("/api/exercises/f3000000-0000-0000-0000-000000000001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("REMEMBER_NUMBER"))
            .andExpect(jsonPath("$.difficulty").value("ULTRA_EASY"))
            .andExpect(jsonPath("$.subjectCode").value("MEMORY"))
            .andExpect(jsonPath("$.rememberNumberParams.numberToRemember").isNumber())
            .andExpect(jsonPath("$.rememberNumberParams.displayTimeMs").value(3000))
            .andExpect(jsonPath("$.rememberNumberParams.mathPrompt").isString())
            .andExpect(jsonPath("$.rememberNumberParams.mathExpectedAnswer").isString())
    }

    @Test
    fun rememberNumber_ULTRA_EASY_numberHas2Digits() {
        val res = mvc.perform(get("/api/exercises/f3000000-0000-0000-0000-000000000001"))
            .andReturn().response.contentAsString
        val tree = mapper.readTree(res)
        val number = tree.get("rememberNumberParams").get("numberToRemember").asInt()
        assertTrue(number in 10..99) { "ULTRA_EASY: 2-digit number expected, got $number" }
    }

    @Test
    fun rememberNumber_EASY_numberHas3Digits() {
        val res = mvc.perform(get("/api/exercises/f3000000-0000-0000-0000-000000000002"))
            .andReturn().response.contentAsString
        val tree = mapper.readTree(res)
        val number = tree.get("rememberNumberParams").get("numberToRemember").asInt()
        assertTrue(number in 100..999) { "EASY: 3-digit number expected, got $number" }
    }

    @Test
    fun rememberNumber_MEDIUM_numberHas4Digits() {
        val res = mvc.perform(get("/api/exercises/f3000000-0000-0000-0000-000000000003"))
            .andReturn().response.contentAsString
        val tree = mapper.readTree(res)
        val number = tree.get("rememberNumberParams").get("numberToRemember").asInt()
        assertTrue(number in 1000..9999) { "MEDIUM: 4-digit number expected, got $number" }
    }

    @Test
    fun rememberNumber_HARD_numberHas5Digits() {
        val res = mvc.perform(get("/api/exercises/f3000000-0000-0000-0000-000000000004"))
            .andReturn().response.contentAsString
        val tree = mapper.readTree(res)
        val number = tree.get("rememberNumberParams").get("numberToRemember").asInt()
        assertTrue(number in 10000..99999) { "HARD: 5-digit number expected, got $number" }
    }

    @Test
    fun rememberNumber_mathPromptIsValidAddition() {
        val res = mvc.perform(get("/api/exercises/f3000000-0000-0000-0000-000000000001"))
            .andReturn().response.contentAsString
        val tree = mapper.readTree(res)
        val mathPrompt = tree.get("rememberNumberParams").get("mathPrompt").asText()
        val mathAnswer = tree.get("rememberNumberParams").get("mathExpectedAnswer").asText()
        assertTrue(mathPrompt.startsWith("What is ") && mathPrompt.endsWith("?")) {
            "Math prompt should be a question, got: $mathPrompt"
        }
        assertTrue(mathAnswer.toIntOrNull() != null) {
            "Math answer should be a number, got: $mathAnswer"
        }
    }

    @Test
    fun rememberNumber_returnsDifferentContentOnEachRequest() {
        val id = "f3000000-0000-0000-0000-000000000001"
        val res1 = mvc.perform(get("/api/exercises/$id")).andReturn().response.contentAsString
        val res2 = mvc.perform(get("/api/exercises/$id")).andReturn().response.contentAsString
        assertNotEquals(res1, res2) { "Remember Number content should vary per request" }
    }

    @Test
    fun rememberNumber_VERY_HARD_numberHas6Digits() {
        val res = mvc.perform(get("/api/exercises/f3000000-0000-0000-0000-000000000005"))
            .andReturn().response.contentAsString
        val tree = mapper.readTree(res)
        val number = tree.get("rememberNumberParams").get("numberToRemember").asInt()
        assertTrue(number in 100000..999999) { "VERY_HARD: 6-digit number expected, got $number" }
        val displayTimeMs = tree.get("rememberNumberParams").get("displayTimeMs").asInt()
        assertEquals(1200, displayTimeMs) { "VERY_HARD display time should be 1200ms" }
    }
}

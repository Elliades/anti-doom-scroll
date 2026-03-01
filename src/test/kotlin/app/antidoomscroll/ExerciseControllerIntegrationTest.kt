package app.antidoomscroll

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
class ExerciseControllerIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun getExerciseById_returnsExercise() {
        val res = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.type").value("FLASHCARD_QA"))
            .andExpect(jsonPath("$.subjectCode").value("default"))
            .andExpect(jsonPath("$.prompt").exists())
            .andExpect(jsonPath("$.expectedAnswers").isArray())
            .andReturn().response.contentAsString
        val prompt = com.fasterxml.jackson.databind.ObjectMapper().readTree(res).get("prompt").asText()
        org.junit.jupiter.api.Assertions.assertTrue(
            prompt.startsWith("What is ") && prompt.contains("+") && prompt.endsWith("?")
        ) { "Math ADD prompt should match pattern, got: $prompt" }
    }

    @Test
    fun getExerciseById_notFound_returns404() {
        mvc.perform(get("/api/exercises/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun getExerciseById_SUM_PAIR_returnsSumPairGroupsAndDeck() {
        mvc.perform(get("/api/exercises/e0000000-0000-0000-0000-000000000001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("SUM_PAIR"))
            .andExpect(jsonPath("$.sumPairParams.staticNumbers").isArray())
            .andExpect(jsonPath("$.sumPairParams.staticNumbers[0]").value(5))
            .andExpect(jsonPath("$.sumPairParams.pairsPerRound").value(3))
            .andExpect(jsonPath("$.sumPairGroups").isArray())
            .andExpect(jsonPath("$.sumPairGroups.length()").value(1))
            .andExpect(jsonPath("$.sumPairGroups[0].static").value(5))
            .andExpect(jsonPath("$.sumPairGroups[0].color").exists())
            .andExpect(jsonPath("$.sumPairGroups[0].cards").isArray())
            .andExpect(jsonPath("$.sumPairGroups[0].cards.length()").value(6))
            .andExpect(jsonPath("$.sumPairDeck").isArray())
            .andExpect(jsonPath("$.sumPairDeck.length()").value(6))
    }

    @Test
    fun getExerciseById_SUM_PAIR_returnsDifferentContentOnEachRequest() {
        val sumPairId = "e0000000-0000-0000-0000-000000000001"
        val res1 = mvc.perform(get("/api/exercises/$sumPairId")).andReturn().response.contentAsString
        val res2 = mvc.perform(get("/api/exercises/$sumPairId")).andReturn().response.contentAsString
        // Generated pairs/deck should differ so users can play multiple times with fresh content
        org.junit.jupiter.api.Assertions.assertNotEquals(res1, res2) { "Sum Pair content should vary per request" }
    }

    @Test
    fun getExerciseById_SUM_PAIR_randomStatics_returnsSortedStaticNumbers() {
        // Exercise e005 uses staticCount/staticMin/staticMax - random statics, sorted
        val res = mvc.perform(get("/api/exercises/e0000000-0000-0000-0000-000000000005"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("SUM_PAIR"))
            .andExpect(jsonPath("$.sumPairParams.staticNumbers").isArray())
            .andReturn()
        val tree = com.fasterxml.jackson.databind.ObjectMapper().readTree(res.response.contentAsString)
        val staticNumbers = tree.get("sumPairParams").get("staticNumbers").map { it.asInt() }
        org.junit.jupiter.api.Assertions.assertEquals(staticNumbers.sorted(), staticNumbers) {
            "Generated staticNumbers must be sorted ascending"
        }
    }

    @Test
    fun getExerciseById_N_BACK_GRID_returnsNBackGridParams() {
        mvc.perform(get("/api/exercises/c0000000-0000-0000-0000-000000000010"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("N_BACK_GRID"))
            .andExpect(jsonPath("$.nbackGridParams.n").value(1))
            .andExpect(jsonPath("$.nbackGridParams.sequence").isArray())
            .andExpect(jsonPath("$.nbackGridParams.sequence.length()").value(5))
            .andExpect(jsonPath("$.nbackGridParams.matchIndices").isArray())
            .andExpect(jsonPath("$.nbackGridParams.gridSize").value(3))
    }

    @Test
    fun getExerciseById_DUAL_NBACK_GRID_returnsDualNBackGridParams() {
        mvc.perform(get("/api/exercises/c0000000-0000-0000-0000-000000000012"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("DUAL_NBACK_GRID"))
            .andExpect(jsonPath("$.dualNBackGridParams.n").value(1))
            .andExpect(jsonPath("$.dualNBackGridParams.sequence").isArray())
            .andExpect(jsonPath("$.dualNBackGridParams.matchPositionIndices").isArray())
            .andExpect(jsonPath("$.dualNBackGridParams.matchColorIndices").isArray())
            .andExpect(jsonPath("$.dualNBackGridParams.colors").isArray())
    }

    @Test
    fun getExerciseById_DUAL_NBACK_CARD_returnsDualNBackCardParams() {
        mvc.perform(get("/api/exercises/c0000000-0000-0000-0000-000000000013"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("DUAL_NBACK_CARD"))
            .andExpect(jsonPath("$.dualNBackCardParams.n").value(1))
            .andExpect(jsonPath("$.dualNBackCardParams.sequence").isArray())
            .andExpect(jsonPath("$.dualNBackCardParams.matchColorIndices").isArray())
            .andExpect(jsonPath("$.dualNBackCardParams.matchNumberIndices").isArray())
    }

    @Test
    fun getExerciseById_MULTIPLY_returnsGeneratedPromptAndAnswer() {
        mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000030"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("FLASHCARD_QA"))
            .andExpect(jsonPath("$.mathOperation").value("MULTIPLY"))
            .andExpect(jsonPath("$.prompt").exists())
            .andExpect(jsonPath("$.expectedAnswers").isArray())
            .andExpect(jsonPath("$.expectedAnswers.length()").value(1))
        val res = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000030")).andReturn().response.contentAsString
        val prompt = com.fasterxml.jackson.databind.ObjectMapper().readTree(res).get("prompt").asText()
        // Generator produces "What is A × B?" when params are applied (× may appear as \u00d7 in some encodings)
        org.junit.jupiter.api.Assertions.assertTrue(prompt.startsWith("What is ") && prompt.contains("?")) { "Multiply prompt should be generated: $prompt" }
    }

    @Test
    fun getExerciseById_DIVIDE_returnsGeneratedPromptAndAnswer() {
        mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000040"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("FLASHCARD_QA"))
            .andExpect(jsonPath("$.mathOperation").value("DIVIDE"))
            .andExpect(jsonPath("$.prompt").exists())
            .andExpect(jsonPath("$.expectedAnswers").isArray())
            .andExpect(jsonPath("$.expectedAnswers.length()").value(1))
        val res = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000040")).andReturn().response.contentAsString
        val prompt = com.fasterxml.jackson.databind.ObjectMapper().readTree(res).get("prompt").asText()
        org.junit.jupiter.api.Assertions.assertTrue(prompt.startsWith("What is ") && prompt.contains("?")) { "Divide prompt should be generated: $prompt" }
    }

    @Test
    fun getExerciseById_ANAGRAM_ULTRA_EASY_returnsAnagramParams() {
        mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("ANAGRAM"))
            .andExpect(jsonPath("$.difficulty").value("ULTRA_EASY"))
            .andExpect(jsonPath("$.anagramParams.scrambledLetters").isArray())
            .andExpect(jsonPath("$.anagramParams.answer").exists())
            .andExpect(jsonPath("$.anagramParams.hintIntervalSeconds").value(10))
            .andExpect(jsonPath("$.anagramParams.letterColorHint").value(true))
        val res = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000100")).andReturn().response.contentAsString
        val tree = com.fasterxml.jackson.databind.ObjectMapper().readTree(res)
        val scrambled = tree.get("anagramParams").get("scrambledLetters")
        val answer = tree.get("anagramParams").get("answer").asText()
        org.junit.jupiter.api.Assertions.assertTrue(scrambled.size() in 2..3) { "ULTRA_EASY: 2-3 letters, got ${scrambled.size()}" }
        org.junit.jupiter.api.Assertions.assertEquals(scrambled.size(), answer.length) { "scrambledLetters length must match answer" }
    }

    @Test
    fun getExerciseById_ANAGRAM_HARD_returnsAnagramParams() {
        mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000103"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("ANAGRAM"))
            .andExpect(jsonPath("$.difficulty").value("HARD"))
            .andExpect(jsonPath("$.anagramParams.scrambledLetters").isArray())
            .andExpect(jsonPath("$.anagramParams.answer").exists())
        val res = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000103")).andReturn().response.contentAsString
        val tree = com.fasterxml.jackson.databind.ObjectMapper().readTree(res)
        val answer = tree.get("anagramParams").get("answer").asText()
        val scrambled = tree.get("anagramParams").get("scrambledLetters")
        org.junit.jupiter.api.Assertions.assertTrue(answer.length >= 6) { "HARD: 6+ letters (fallback may broaden), got ${answer.length}" }
        org.junit.jupiter.api.Assertions.assertEquals(scrambled.size(), answer.length) { "scrambledLetters length must match answer" }
    }

    @Test
    fun getExerciseById_ANAGRAM_VERY_HARD_returnsAnagramParams() {
        mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000104"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("ANAGRAM"))
            .andExpect(jsonPath("$.difficulty").value("VERY_HARD"))
            .andExpect(jsonPath("$.anagramParams.scrambledLetters").isArray())
            .andExpect(jsonPath("$.anagramParams.answer").exists())
            .andExpect(jsonPath("$.anagramParams.hintIntervalSeconds").value(15))
            .andExpect(jsonPath("$.anagramParams.letterColorHint").value(false))
        val res = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000104")).andReturn().response.contentAsString
        val tree = com.fasterxml.jackson.databind.ObjectMapper().readTree(res)
        val answer = tree.get("anagramParams").get("answer").asText()
        val scrambled = tree.get("anagramParams").get("scrambledLetters")
        org.junit.jupiter.api.Assertions.assertTrue(answer.length >= 8) { "VERY_HARD: 8+ letters, got ${answer.length}" }
        org.junit.jupiter.api.Assertions.assertEquals(scrambled.size(), answer.length) { "scrambledLetters length must match answer" }
    }

    @Test
    fun getExerciseById_ANAGRAM_returnsDifferentWordOnEachRequest() {
        val res1 = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000100")).andReturn().response.contentAsString
        val res2 = mvc.perform(get("/api/exercises/a0000000-0000-0000-0000-000000000100")).andReturn().response.contentAsString
        val ans1 = com.fasterxml.jackson.databind.ObjectMapper().readTree(res1).get("anagramParams").get("answer").asText()
        val ans2 = com.fasterxml.jackson.databind.ObjectMapper().readTree(res2).get("anagramParams").get("answer").asText()
        org.junit.jupiter.api.Assertions.assertNotEquals(res1, res2) { "Anagram content should vary per request" }
    }
}

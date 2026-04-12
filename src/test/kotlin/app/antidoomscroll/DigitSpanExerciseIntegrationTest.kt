package app.antidoomscroll

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
class DigitSpanExerciseIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun `GET exercise DIGIT_SPAN returns digitSpanParams with sequence and tasks`() {
        mvc.perform(get("/api/exercises/f2000000-0000-0000-0000-000000000001"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.type").value("DIGIT_SPAN"))
            .andExpect(jsonPath("$.subjectCode").value("MEMORY"))
            .andExpect(jsonPath("$.digitSpanParams.sequence").isArray)
            .andExpect(jsonPath("$.digitSpanParams.sequence.length()").value(3))
            .andExpect(jsonPath("$.digitSpanParams.displaySeconds").value(3))
            .andExpect(jsonPath("$.digitSpanParams.tasks[0]").value("FORWARD_ORDER"))
            .andExpect(jsonPath("$.digitSpanParams.tasks[1]").value("ASCENDING"))
            .andExpect(jsonPath("$.digitSpanParams.progressive").value(true))
            .andExpect(jsonPath("$.digitSpanParams.maxLength").value(6))
    }

    @Test
    fun `GET MEMORY exercises list includes DIGIT_SPAN`() {
        val res = mvc.perform(get("/api/subjects/MEMORY/exercises"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        assertTrue(res.contains("DIGIT_SPAN")) { "MEMORY subject should list DIGIT_SPAN exercises" }
    }

    @Test
    fun `GET exercise DIGIT_SPAN returns same sequence on repeated requests`() {
        val id = "f2000000-0000-0000-0000-000000000001"
        val mapper = ObjectMapper()
        val seq1 = mapper.readTree(
            mvc.perform(get("/api/exercises/$id")).andExpect(status().isOk).andReturn().response.contentAsString
        ).path("digitSpanParams").path("sequence").toString()
        val seq2 = mapper.readTree(
            mvc.perform(get("/api/exercises/$id")).andExpect(status().isOk).andReturn().response.contentAsString
        ).path("digitSpanParams").path("sequence").toString()
        assertEquals(seq1, seq2) { "Parametric digit span must be stable per exercise id (list → play, refresh)" }
    }

    @Test
    fun `digit span sequence is unique digits when possible`() {
        val tree = com.fasterxml.jackson.databind.ObjectMapper().readTree(
            mvc.perform(get("/api/exercises/f2000000-0000-0000-0000-000000000001"))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
        )
        val seq = tree.path("digitSpanParams").path("sequence")
        assertTrue(seq.isArray)
        assertEquals(3, seq.size())
        val set = mutableSetOf<Int>()
        for (n in seq) {
            set.add(n.asInt())
        }
        assertEquals(3, set.size) { "3 digits in 0..9 should be unique when length <= 10" }
    }
}

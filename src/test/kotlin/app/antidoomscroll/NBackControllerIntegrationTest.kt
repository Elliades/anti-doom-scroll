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

/**
 * Integration tests for N-back by level: GET /api/nback/{level}
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = ["/data.sql"])
class NBackControllerIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun getByLevel1_returns1BackExercise() {
        val result = mvc.perform(get("/api/nback/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("N_BACK"))
            .andReturn()
        val body = result.response.contentAsString
        val tree = com.fasterxml.jackson.databind.ObjectMapper().readTree(body)
        val params = tree.get("nBackParams") ?: tree.get("nbackParams")
        org.junit.jupiter.api.Assertions.assertNotNull(params) { "Response must have nBackParams or nbackParams" }
        org.junit.jupiter.api.Assertions.assertEquals(1, params.get("n").asInt())
        org.junit.jupiter.api.Assertions.assertTrue(params.get("sequence").isArray)
        org.junit.jupiter.api.Assertions.assertTrue(params.get("matchIndices").isArray)
    }

    @Test
    fun getByLevel2_returns2BackExercise() {
        val result = mvc.perform(get("/api/nback/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("N_BACK"))
            .andReturn()
        val params = com.fasterxml.jackson.databind.ObjectMapper().readTree(result.response.contentAsString).get("nBackParams") ?: com.fasterxml.jackson.databind.ObjectMapper().readTree(result.response.contentAsString).get("nbackParams")
        org.junit.jupiter.api.Assertions.assertNotNull(params)
        org.junit.jupiter.api.Assertions.assertEquals(2, params.get("n").asInt())
    }

    @Test
    fun getByLevel3_returns3BackExercise() {
        val result = mvc.perform(get("/api/nback/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("N_BACK"))
            .andReturn()
        val params = com.fasterxml.jackson.databind.ObjectMapper().readTree(result.response.contentAsString).get("nBackParams") ?: com.fasterxml.jackson.databind.ObjectMapper().readTree(result.response.contentAsString).get("nbackParams")
        org.junit.jupiter.api.Assertions.assertNotNull(params)
        org.junit.jupiter.api.Assertions.assertEquals(3, params.get("n").asInt())
    }

    @Test
    fun getByLevel0_returns404() {
        mvc.perform(get("/api/nback/0"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun getByLevel4_returns404() {
        mvc.perform(get("/api/nback/4"))
            .andExpect(status().isNotFound())
    }
}

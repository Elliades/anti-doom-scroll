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
class NBackSessionApiIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun startSessionWithPreferTypeNBack_returnsNBackExercise() {
        val result = mvc.perform(get("/api/session/start").param("preferType", "N_BACK"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileId").exists())
            .andExpect(jsonPath("$.steps").isArray())
            .andExpect(jsonPath("$.steps[0].exercise.type").value("N_BACK"))
            .andReturn()
        val exercise = com.fasterxml.jackson.databind.ObjectMapper().readTree(result.response.contentAsString).get("steps").get(0).get("exercise")
        val params = exercise.get("nBackParams") ?: exercise.get("nbackParams")
        org.junit.jupiter.api.Assertions.assertNotNull(params) { "N_BACK step must have nBackParams" }
        org.junit.jupiter.api.Assertions.assertEquals(1, params.get("n").asInt())
        org.junit.jupiter.api.Assertions.assertTrue(params.get("sequence").isArray)
        org.junit.jupiter.api.Assertions.assertTrue(params.get("matchIndices").isArray)
    }

    @Test
    fun startSessionWithoutPreferType_returnsAnyUltraEasy() {
        mvc.perform(get("/api/session/start"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.steps[0].exercise.type").exists())
    }
}

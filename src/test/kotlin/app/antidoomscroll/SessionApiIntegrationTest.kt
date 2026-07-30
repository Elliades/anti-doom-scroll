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
class SessionApiIntegrationTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun healthReturnsHomelabContract() {
        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.service").value("anti-doom-scroll"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.uptime").isNumber())
            .andExpect(jsonPath("$.checks.frontend.status").value("ok"))
            .andExpect(jsonPath("$.checks.backend.status").value("ok"))
            .andExpect(jsonPath("$.checks.database.status").value("ok"))
            .andExpect(jsonPath("$.checks.database.latencyMs").isNumber())
    }

    @Test
    fun startSessionWithoutProfileIdReturnsSessionWithSteps() {
        mvc.perform(get("/api/session/start"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileId").exists())
            .andExpect(jsonPath("$.steps").isArray())
            .andExpect(jsonPath("$.steps[0].exercise.subjectId").exists())
            .andExpect(jsonPath("$.sessionDefaultSeconds").isNumber())
            .andExpect(jsonPath("$.lowBatteryModeSeconds").isNumber())
    }

    @Test
    fun listSubjectsReturnsAtLeastDefault() {
        mvc.perform(get("/api/subjects"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].code").value("default"))
            .andExpect(jsonPath("$[0].scoringConfig").exists())
    }

    @Test
    fun listExercisesBySubject_returnsExercises() {
        mvc.perform(get("/api/subjects/default/exercises"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].subjectCode").value("default"))
    }
}

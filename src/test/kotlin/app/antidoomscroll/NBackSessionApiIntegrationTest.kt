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
        mvc.perform(get("/api/session/start").param("preferType", "N_BACK"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileId").exists())
            .andExpect(jsonPath("$.steps").isArray())
            .andExpect(jsonPath("$.steps[0].exercise.type").value("N_BACK"))
            .andExpect(jsonPath("$.steps[0].exercise.nBackParams").exists())
            .andExpect(jsonPath("$.steps[0].exercise.nBackParams.n").value(1))
            .andExpect(jsonPath("$.steps[0].exercise.nBackParams.sequence").isArray())
            .andExpect(jsonPath("$.steps[0].exercise.nBackParams.matchIndices").isArray())
    }

    @Test
    fun startSessionWithoutPreferType_returnsAnyUltraEasy() {
        mvc.perform(get("/api/session/start"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.steps[0].exercise.type").exists())
    }
}

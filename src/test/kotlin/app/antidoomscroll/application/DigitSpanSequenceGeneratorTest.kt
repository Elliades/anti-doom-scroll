package app.antidoomscroll.application

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class DigitSpanSequenceGeneratorTest {

    @Test
    fun `seedForExerciseId is stable`() {
        val id = UUID.fromString("f2000000-0000-0000-0000-000000000001")
        assertEquals(DigitSpanSequenceGenerator.seedForExerciseId(id), DigitSpanSequenceGenerator.seedForExerciseId(id))
    }

    @Test
    fun `generate with same seed produces same sequence`() {
        val a = DigitSpanSequenceGenerator.generate(4, 0, 9, seed = 42)
        val b = DigitSpanSequenceGenerator.generate(4, 0, 9, seed = 42)
        assertEquals(a, b)
    }
}

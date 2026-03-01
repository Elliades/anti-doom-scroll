package app.antidoomscroll.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArithmeticComplexityTest {

    @Test
    fun `digits returns correct count`() {
        assertEquals(1, ArithmeticComplexity.digits(0))
        assertEquals(1, ArithmeticComplexity.digits(5))
        assertEquals(2, ArithmeticComplexity.digits(10))
        assertEquals(2, ArithmeticComplexity.digits(99))
        assertEquals(3, ArithmeticComplexity.digits(100))
        assertEquals(4, ArithmeticComplexity.digits(9999))
    }

    @Test
    fun `complexityAdd no carries is low`() {
        val s = ArithmeticComplexity.complexityAdd(12, 34)  // 1+3=4, 2+4=6, no carry
        assertTrue(s < 10.0)
        assertTrue(s >= 0.0)
    }

    @Test
    fun `complexityAdd with carries is higher`() {
        val noCarry = ArithmeticComplexity.complexityAdd(11, 22)
        val withCarry = ArithmeticComplexity.complexityAdd(99, 1)
        assertTrue(withCarry > noCarry)
    }

    @Test
    fun `complexitySubtract no borrows is low`() {
        val s = ArithmeticComplexity.complexitySubtract(85, 42)
        assertTrue(s >= 0.0)
    }

    @Test
    fun `complexitySubtract with borrows is higher`() {
        val noBorrow = ArithmeticComplexity.complexitySubtract(99, 88)
        val withBorrow = ArithmeticComplexity.complexitySubtract(100, 1)
        assertTrue(withBorrow >= noBorrow)
    }

    @Test
    fun `complexityMultiply single digit`() {
        val easy = ArithmeticComplexity.complexityMultiply(2, 3)
        val hard = ArithmeticComplexity.complexityMultiply(7, 8)
        assertTrue(easy >= 0.0)
        assertTrue(hard >= easy)
    }

    @Test
    fun `complexityMultiply multi digit`() {
        val s = ArithmeticComplexity.complexityMultiply(24, 17)
        assertTrue(s > 5.0)
    }

    @Test
    fun `complexityDivide clean division`() {
        val s = ArithmeticComplexity.complexityDivide(20, 5)
        assertTrue(s >= 0.0)
    }

    @Test
    fun `scoreBandFor maps difficulties`() {
        val (ueMin, ueMax) = ArithmeticComplexity.scoreBandFor(Difficulty.ULTRA_EASY)
        assertEquals(0.0, ueMin)
        assertEquals(5.0, ueMax)
        val (eMin, eMax) = ArithmeticComplexity.scoreBandFor(Difficulty.EASY)
        assertEquals(5.0, eMin)
        assertEquals(15.0, eMax)
        val (mMin, mMax) = ArithmeticComplexity.scoreBandFor(Difficulty.MEDIUM)
        assertEquals(15.0, mMin)
        assertEquals(30.0, mMax)
        val (hMin, hMax) = ArithmeticComplexity.scoreBandFor(Difficulty.HARD)
        assertEquals(30.0, hMin)
        assertEquals(60.0, hMax)
    }
}

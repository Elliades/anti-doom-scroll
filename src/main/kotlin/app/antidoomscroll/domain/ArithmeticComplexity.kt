package app.antidoomscroll.domain

/**
 * Human arithmetic complexity model: computable scores for basic operations.
 * See [human_arithmetic_complexity_model.md].
 *
 * Difficulty bands (calibrate empirically):
 * - 0–5: very easy
 * - 5–15: elementary
 * - 15–30: intermediate
 * - 30+: advanced
 */
object ArithmeticComplexity {

    /** Target complexity band [min, max] per difficulty for problem generation. */
    fun scoreBandFor(difficulty: Difficulty): Pair<Double, Double> = when (difficulty) {
        Difficulty.ULTRA_EASY -> 0.0 to 5.0
        Difficulty.EASY -> 5.0 to 15.0
        Difficulty.MEDIUM -> 15.0 to 30.0
        Difficulty.HARD -> 30.0 to 60.0
        Difficulty.VERY_HARD -> 30.0 to 60.0
    }

    /**
     * Complexity score for A + B (column addition).
     * Score = 1.0*D + 2.5*C + 1.5*MC + 0.2*SD + 0.5*ID
     */
    fun complexityAdd(a: Int, b: Int): Double {
        if (a < 0 || b < 0) return 0.0
        val digitsA = digits(a)
        val digitsB = digits(b)
        val D = maxOf(digitsA, digitsB)
        val (c, mc, sd) = addCarriesAndMaxColumnSum(a, b)
        val ID = kotlin.math.abs(digitsA - digitsB)
        return 1.0 * D + 2.5 * c + 1.5 * mc + 0.2 * sd + 0.5 * ID
    }

    /**
     * Complexity score for A − B (column subtraction, A ≥ B).
     * Score = 1.0*D + 3.0*B + 2.0*MB + 1.5*Z
     */
    fun complexitySubtract(a: Int, b: Int): Double {
        if (a < 0 || b < 0 || b > a) return 0.0
        val D = digits(a)
        val (borrows, maxConsecutive, zerosInBorrowChains) = subtractBorrows(a, b)
        return 1.0 * D + 3.0 * borrows + 2.0 * maxConsecutive + 1.5 * zerosInBorrowChains
    }

    /**
     * Complexity score for A × B.
     * Single-digit (both ≤9): 2.0*(1-FT) + 1.0*H (FT=0 for unknown fact, H=1 if 7,8,9).
     * Multi-digit: 1.2*(D1+D2) + 0.8*SP + 2.0*C + 0.5*Z
     */
    fun complexityMultiply(a: Int, b: Int): Double {
        if (a < 0 || b < 0) return 0.0
        val d1 = digits(a)
        val d2 = digits(b)
        return if (a <= 9 && b <= 9) {
            val H = if (a in 7..9 || b in 7..9) 1 else 0
            2.0 * (1 - 0) + 1.0 * H  // FT=0 (not memorized)
        } else {
            val sp = d1 * d2
            val (carries, structuralZeros) = multiplyCarriesAndZeros(a, b)
            1.2 * (d1 + d2) + 0.8 * sp + 2.0 * carries + 0.5 * structuralZeros
        }
    }

    /**
     * Complexity score for dividend ÷ divisor (clean division: no remainder).
     * Score = 1.5*D1 + 2.0*D2 + 2.0*S + 3.0*R + 4.0*DEC (R=0, DEC=0 for clean).
     */
    fun complexityDivide(dividend: Int, divisor: Int): Double {
        if (divisor <= 0 || dividend < 0) return 0.0
        val quotient = dividend / divisor
        val remainder = dividend % divisor
        if (quotient <= 0) return 0.0
        val D1 = digits(dividend)
        val D2 = digits(divisor)
        val S = digits(quotient)  // number of subtraction steps
        val R = if (remainder != 0) 1 else 0
        val DEC = if (remainder != 0 || quotient != (dividend.toDouble() / divisor).toInt()) 1 else 0
        return 1.5 * D1 + 2.0 * D2 + 2.0 * S + 3.0 * R + 4.0 * DEC
    }

    fun digits(n: Int): Int {
        if (n <= 0) return 1
        var x = n
        var d = 0
        while (x > 0) {
            d++
            x /= 10
        }
        return d
    }

    private fun addCarriesAndMaxColumnSum(a: Int, b: Int): Triple<Int, Int, Int> {
        var totalCarries = 0
        var maxConsecutive = 0
        var currentConsecutive = 0
        var maxColumnSum = 0
        var carry = 0
        var pa = a
        var pb = b
        while (pa > 0 || pb > 0 || carry > 0) {
            val da = pa % 10
            val db = pb % 10
            val colSum = da + db + carry
            maxColumnSum = maxOf(maxColumnSum, colSum)
            if (colSum >= 10) {
                totalCarries++
                currentConsecutive++
                carry = 1
            } else {
                maxConsecutive = maxOf(maxConsecutive, currentConsecutive)
                currentConsecutive = 0
                carry = 0
            }
            pa /= 10
            pb /= 10
        }
        maxConsecutive = maxOf(maxConsecutive, currentConsecutive)
        return Triple(totalCarries, maxConsecutive, maxColumnSum)
    }

    /** Column i = 10^i place (0 = units). */
    private fun digitAt(n: Int, i: Int): Int = (n / pow10(i)) % 10

    private fun pow10(i: Int): Int = when (i) {
        0 -> 1
        1 -> 10
        2 -> 100
        3 -> 1000
        4 -> 10000
        else -> (1..i).fold(1) { acc, _ -> acc * 10 }
    }

    private fun subtractBorrows(a: Int, b: Int): Triple<Int, Int, Int> {
        var totalBorrows = 0
        var maxConsecutive = 0
        var currentConsecutive = 0
        var zerosInChains = 0
        val len = digits(a)
        var borrow = 0
        for (i in 0 until len) {
            var da = digitAt(a, i) - borrow
            val db = digitAt(b, i)
            if (da < db) {
                totalBorrows++
                currentConsecutive++
                if (da == 0) zerosInChains++
                borrow = 1
            } else {
                maxConsecutive = maxOf(maxConsecutive, currentConsecutive)
                currentConsecutive = 0
                borrow = 0
            }
        }
        maxConsecutive = maxOf(maxConsecutive, currentConsecutive)
        return Triple(totalBorrows, maxConsecutive, zerosInChains)
    }

    private fun digitList(n: Int): List<Int> {
        if (n <= 0) return listOf(0)
        val list = mutableListOf<Int>()
        var x = n
        while (x > 0) {
            list.add(x % 10)
            x /= 10
        }
        return list
    }

    private fun multiplyCarriesAndZeros(a: Int, b: Int): Pair<Int, Int> {
        var totalCarries = 0
        var structuralZeros = 0
        val digitsA = digitList(a)
        val digitsB = digitList(b)
        for (j in digitsB.indices) {
            val db = digitsB[j]
            var carry = 0
            for (i in digitsA.indices) {
                val step = digitsA[i] * db + carry
                if (step >= 10) {
                    totalCarries++
                    carry = step / 10
                } else {
                    carry = 0
                }
            }
            if (carry > 0) totalCarries++
        }
        val product = a.toLong() * b
        if (product > 0) {
            var p = product
            while (p > 0) {
                if (p % 10 == 0L) structuralZeros++
                p /= 10
            }
        }
        return totalCarries to structuralZeros
    }
}

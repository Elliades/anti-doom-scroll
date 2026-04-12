package app.antidoomscroll.application

import kotlin.random.Random

object DigitSpanSequenceGenerator {

    /**
     * Unique digits in the inclusive min/max range when possible; otherwise random digits in range.
     */
    fun generate(length: Int, minDigit: Int, maxDigit: Int, seed: Int): List<Int> {
        val rng = Random(seed)
        val span = length.coerceAtLeast(1)
        val lo = minDigit.coerceIn(0, 9)
        val hi = maxDigit.coerceIn(lo, 9)
        val rangeSize = hi - lo + 1
        return if (span <= rangeSize) {
            (lo..hi).shuffled(rng).take(span)
        } else {
            List(span) { rng.nextInt(lo, hi + 1) }
        }
    }
}

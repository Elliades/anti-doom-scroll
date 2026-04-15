package app.antidoomscroll.domain

/**
 * Params for DIGIT_SPAN exercise (forward + working-memory recall).
 *
 * The exercise is entirely client-driven: the backend only provides the
 * starting length and display/input timing.  The frontend generates
 * random digit sequences, progressively adds digits on success, and
 * applies challenge modes (ascending, descending, even/odd split,
 * every-other) after a correct forward recall.
 *
 * - startLength: how many digits in the first round (e.g. 3–4)
 * - displayTimeMs: how long the digits are shown (default 3 000 ms)
 * - maxLength: upper cap to stop the exercise (default 15)
 */
data class DigitSpanParams(
    val startLength: Int,
    val displayTimeMs: Int = 3000,
    val maxLength: Int = 15
) {
    init {
        require(startLength in 2..15) { "startLength must be 2–15" }
        require(displayTimeMs in 500..10_000) { "displayTimeMs must be 500–10 000" }
        require(maxLength >= startLength) { "maxLength must be >= startLength" }
    }
}

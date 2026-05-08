package app.antidoomscroll.domain

import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.roundToInt

/**
 * Heuristic Wordle setup difficulty: structural factors only (no secret word).
 *
 * **Indicators**
 * - [wordLength], [maxAttempts], [timeLimitSeconds]: from the exercise.
 * - [effectiveAlphabetSize]: convention for combinatorial size — 26 (en), 27 (fr); extend if you add languages.
 * - [searchSpaceLog10]: log10(A^L) = L × log10(A) (order of magnitude of uniform random-guess space).
 * - [entropyBits]: L × log2(A) (Shannon entropy of a uniform random letter string).
 * - [guessesPerLetter]: maxAttempts / wordLength (higher = more slack = easier).
 * - [secondsPerGuessBudget]: timeLimitSeconds / maxAttempts (lower = tighter clock per row).
 *
 * **difficultyScore0To100**: 0 = easiest supported setup, 100 = hardest. Weighted blend of
 * entropy (search hardness), tightness of attempt budget vs length, and time pressure.
 */
data class WordleComplexity(
    val wordLength: Int,
    val maxAttempts: Int,
    val timeLimitSeconds: Int,
    val effectiveAlphabetSize: Double,
    val searchSpaceLog10: Double,
    val entropyBits: Double,
    val guessesPerLetter: Double,
    val secondsPerGuessBudget: Double,
    val difficultyScore0To100: Int
) {
    companion object {
        private const val MIN_LEN = 3
        private const val MAX_LEN = 10
        private const val MIN_ATTEMPTS = 3
        private const val MAX_ATTEMPTS = 10
        private const val REF_TIME_CAP_SECONDS = 300.0

        /** Default alphabet size by language code (lowercase ISO-ish). */
        fun defaultAlphabetSize(language: String): Double =
            when (language.lowercase()) {
                "fr" -> 27.0
                else -> 26.0
            }

        fun compute(
            wordLength: Int,
            maxAttempts: Int,
            timeLimitSeconds: Int,
            language: String
        ): WordleComplexity {
            val L = wordLength.coerceIn(MIN_LEN, MAX_LEN)
            val G = maxAttempts.coerceIn(MIN_ATTEMPTS, MAX_ATTEMPTS)
            val T = timeLimitSeconds.coerceAtLeast(1)
            val A = defaultAlphabetSize(language)
            val spaceLog10 = L * log10(A)
            val entropy = L * (ln(A) / ln(2.0))
            val guessesPerLetter = G.toDouble() / L
            val secondsPerGuess = T.toDouble() / G

            val entropyMin = MIN_LEN * (ln(26.0) / ln(2.0))
            val entropyMax = MAX_LEN * (ln(27.0) / ln(2.0))
            val entropyNorm = ((entropy - entropyMin) / (entropyMax - entropyMin)).coerceIn(0.0, 1.0)

            // More guesses per letter => easier => lower contribution toward 100.
            val ratioMin = MIN_ATTEMPTS.toDouble() / MAX_LEN
            val ratioMax = MAX_ATTEMPTS.toDouble() / MIN_LEN
            val attemptTightness = (1.0 - ((guessesPerLetter - ratioMin) / (ratioMax - ratioMin)).coerceIn(0.0, 1.0))

            // Shorter overall time budget => harder (normalized against a generous cap).
            val timePressure = (1.0 - (T / REF_TIME_CAP_SECONDS).coerceIn(0.0, 1.0))

            val raw = 0.52 * entropyNorm + 0.28 * attemptTightness + 0.20 * timePressure
            val score = (raw * 100.0).roundToInt().coerceIn(0, 100)

            return WordleComplexity(
                wordLength = L,
                maxAttempts = G,
                timeLimitSeconds = T,
                effectiveAlphabetSize = A,
                searchSpaceLog10 = spaceLog10,
                entropyBits = entropy,
                guessesPerLetter = guessesPerLetter,
                secondsPerGuessBudget = secondsPerGuess,
                difficultyScore0To100 = score
            )
        }
    }
}

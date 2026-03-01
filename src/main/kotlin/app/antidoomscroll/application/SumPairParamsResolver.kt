package app.antidoomscroll.application

import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.SumPairParams
import kotlin.math.pow
import kotlin.random.Random

/**
 * Resolves SumPairParams from an Exercise. Supports:
 * - **Fixed statics**: `staticNumbers` from exercise_params (e.g. [5] or [2, 5, 10]).
 * - **Random statics**: `staticCount`, `staticMin`, `staticMax` — generates that many distinct
 *   values in [staticMin, staticMax], sorted ascending, so each play gets different statics.
 * - **Digit range**: optional `minDigits`, `maxDigits` (e.g. 1–2 = numbers 1–99). When both present,
 *   they define the numeric range for displayed pairs: minValue = 10^(minDigits-1), maxValue = 10^maxDigits - 1.
 *   If omitted, `minValue`/`maxValue` from params are used (default 1, 99).
 */
object SumPairParamsResolver {

    fun resolve(exercise: Exercise, random: Random = Random.Default): SumPairParams? = runCatching {
        if (exercise.type != ExerciseType.SUM_PAIR) return@runCatching null
        val p = exercise.exerciseParams ?: return@runCatching null
        val pairsPerRound = (p["pairsPerRound"] as? Number)?.toInt() ?: return@runCatching null
        val (minValue, maxValue) = resolveMinMaxValue(p)

        val staticNumbers = when {
            p["staticNumbers"] != null -> {
                @Suppress("UNCHECKED_CAST")
                (p["staticNumbers"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.takeIf { it.isNotEmpty() }
            }
            p["staticCount"] != null && p["staticMin"] != null && p["staticMax"] != null -> {
                val count = (p["staticCount"] as Number).toInt().coerceAtLeast(1)
                val lo = (p["staticMin"] as Number).toInt()
                val hi = (p["staticMax"] as Number).toInt()
                if (hi - lo + 1 < count) return@runCatching null
                (lo..hi).toList().shuffled(random).take(count).sorted()
            }
            else -> null
        } ?: return@runCatching null

        SumPairParams(
            staticNumbers = staticNumbers,
            pairsPerRound = pairsPerRound,
            minValue = minValue,
            maxValue = maxValue
        )
    }.getOrNull()

    /**
     * Resolves (minValue, maxValue) from params. If both minDigits and maxDigits are present,
     * derives range: minValue = 10^(minDigits-1), maxValue = 10^maxDigits - 1. Otherwise uses minValue/maxValue (default 1, 99).
     */
    private fun resolveMinMaxValue(p: Map<String, Any?>): Pair<Int, Int> {
        val minDigits = (p["minDigits"] as? Number)?.toInt()
        val maxDigits = (p["maxDigits"] as? Number)?.toInt()
        if (minDigits != null && maxDigits != null && minDigits in 1..9 && maxDigits in 1..9 && minDigits <= maxDigits) {
            val minVal = 10.0.pow((minDigits - 1).coerceAtLeast(0)).toInt()
            val maxVal = 10.0.pow(maxDigits).toInt() - 1
            return minVal to maxVal.coerceAtLeast(minVal)
        }
        val minVal = (p["minValue"] as? Number)?.toInt() ?: 1
        val maxVal = (p["maxValue"] as? Number)?.toInt() ?: 99
        return minVal to maxVal
    }
}

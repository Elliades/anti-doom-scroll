package app.antidoomscroll.application

import app.antidoomscroll.domain.ArithmeticComplexity
import app.antidoomscroll.domain.Difficulty
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * Generates parametric ESTIMATION exercises for variety in the estimation ladder.
 * Given a difficulty score (1-100) and seed, produces consistent problems.
 *
 * Types:
 * - Pure math (division e.g. 372/3, multiplication): uses [ArithmeticComplexity] bands; time matters more.
 * - Word: speed/distance/time ("Car 130 km/h, how long for 50 km?"); can ask in hours or minutes.
 * - Word: budget/daily rate ("Budget 72K, daily 612, how many days?"); can ask in months for harder.
 * - Conversion: h/min ("How many minutes are in 1.5 hours?").
 * - Curated: fact/math items from estimation-items.json (variables resolved per level).
 *
 * First level (levelIndex <= 4): greater tau (toleranceFactor from config, default 1.5–1.7) for easier scoring.
 */
@Component
class EstimationExerciseGenerator(
    private val curatedItems: EstimationCuratedItems,
    private val parametricConfig: EstimationParametricConfig
) {

    /**
     * One generated estimation problem (prompt + scoring params).
     * [timeWeightHigher] true for pure math so frontend can weight time more than precision.
     */
    data class GeneratedEstimation(
        val prompt: String,
        val correctAnswer: Double,
        val unit: String,
        val toleranceFactor: Double,
        val category: String,
        val hint: String? = null,
        val timeWeightHigher: Boolean = false
    )

    /**
     * Difficulty score 1-100 → complexity band (min, max) per [human_arithmetic_complexity_model].
     */
    fun complexityBandForScore(difficultyScore: Int): Pair<Double, Double> {
        val s = difficultyScore.coerceIn(1, 100)
        return when {
            s <= 25 -> 5.0 to 15.0   // elementary
            s <= 50 -> 15.0 to 30.0  // intermediate
            s <= 75 -> 30.0 to 60.0  // hard
            else -> 30.0 to 60.0     // very hard
        }
    }

    /**
     * Map ladder level index (0-29) to a difficulty score 1-100 for generation.
     */
    fun difficultyScoreFromLevel(levelIndex: Int): Int = (levelIndex * 3 + 10).coerceIn(1, 100)

    /** First level: use greater tau (toleranceFactor from config) for easier scoring. */
    private fun isFirstLevel(levelIndex: Int) = levelIndex <= 4

    /**
     * Generate a single estimation exercise. Same (levelIndex, difficulty, seed) → same problem.
     * At first level, bias toward easier kinds (car speed, conversion h/min).
     */
    fun generate(
        levelIndex: Int,
        difficulty: Difficulty,
        seed: Long
    ): GeneratedEstimation {
        val difficultyScore = difficultyScoreFromLevel(levelIndex)
        val rng = Random(seed)
        val firstLevel = isFirstLevel(levelIndex)
        val budgetMinLevel = parametricConfig.budgetDays().minLevelIndex
        val budgetAvailable = levelIndex >= budgetMinLevel
        val kind = when {
            firstLevel && rng.nextDouble() < 0.5 ->
                if (budgetAvailable) rng.nextInt(1, 4) else rng.nextInt(1, 3) // 1=speed, 2=budget or conversion
            budgetAvailable -> rng.nextInt(0, 4) // 0=math, 1=speed, 2=budget, 3=conversion
            else -> rng.nextInt(0, 3) // 0=math, 1=speed, 2=conversion (no budget)
        }
        return when {
            kind == 0 -> generatePureMath(difficultyScore, rng, firstLevel)
            kind == 1 -> generateSpeedTime(rng, firstLevel)
            kind == 2 && budgetAvailable -> generateBudgetDays(difficultyScore, rng, firstLevel)
            else -> generateConversionHMin(rng, firstLevel) // kind 2 when !budget, or kind 3
        }
    }

    /**
     * Generate multiple exercises to add to the ladder pool (different seeds).
     * Includes parametric generation plus curated items (fact/math from JSON) when available for the level.
     */
    fun generatePool(
        levelIndex: Int,
        difficulty: Difficulty,
        baseSeed: Long,
        count: Int = 8
    ): List<GeneratedEstimation> {
        val parametric = (0 until count).map { i -> generate(levelIndex, difficulty, baseSeed + i) }
        val curated = (0 until 4).mapNotNull { i -> curatedItems.resolve(levelIndex, baseSeed + 200 + i) }
        return parametric + curated
    }

    private fun toleranceFactor(firstLevel: Boolean, rng: Random, base: Double = 1.15, range: Double = 0.15): Double {
        if (firstLevel) {
            val cfg = parametricConfig.firstLevel()
            return cfg.toleranceFactorMin + rng.nextDouble() * (cfg.toleranceFactorMax - cfg.toleranceFactorMin)
        }
        return base + rng.nextDouble() * range
    }

    private fun generatePureMath(difficultyScore: Int, rng: Random, firstLevel: Boolean): GeneratedEstimation {
        val cfg = parametricConfig.pureMath()
        val (minC, maxC) = complexityBandForScore(difficultyScore)
        val tol = toleranceFactor(firstLevel, rng, cfg.toleranceBase, cfg.toleranceRange)
        val useDivision = rng.nextBoolean()
        if (useDivision) {
            repeat(50) {
                val divisor = rng.nextInt(2, 99)
                val quotient = rng.nextInt(10, 999)
                val dividend = divisor * quotient
                if (dividend <= 0 || dividend > 999_999) return@repeat
                val complexity = ArithmeticComplexity.complexityDivide(dividend, divisor)
                if (complexity in minC..maxC) {
                    return GeneratedEstimation(
                        prompt = "Estimate: $dividend ÷ $divisor = ?",
                        correctAnswer = quotient.toDouble(),
                        unit = "",
                        toleranceFactor = tol,
                        category = "math",
                        hint = null,
                        timeWeightHigher = true
                    )
                }
            }
        }
        repeat(50) {
            val a = rng.nextInt(10, 999)
            val b = rng.nextInt(2, 99)
            val product = a.toLong() * b
            if (product > Int.MAX_VALUE) return@repeat
            val complexity = ArithmeticComplexity.complexityMultiply(a, b)
            if (complexity in minC..maxC) {
                return GeneratedEstimation(
                    prompt = "Estimate: $a × $b = ?",
                    correctAnswer = product.toDouble(),
                    unit = "",
                    toleranceFactor = tol,
                    category = "math",
                    hint = null,
                    timeWeightHigher = true
                )
            }
        }
        // fallback: simple division
        val divisorLo = cfg.fallbackDivisorRange.getOrNull(0) ?: 2
        val divisorHi = cfg.fallbackDivisorRange.getOrNull(1) ?: 12
        val quotientLo = cfg.fallbackQuotientRange.getOrNull(0) ?: 10
        val quotientHi = cfg.fallbackQuotientRange.getOrNull(1) ?: 200
        val divisor = rng.nextInt(divisorLo, divisorHi + 1)
        val quotient = rng.nextInt(quotientLo, quotientHi + 1)
        val dividend = divisor * quotient
        return GeneratedEstimation(
            prompt = "Estimate: $dividend ÷ $divisor = ?",
            correctAnswer = quotient.toDouble(),
            unit = "",
            toleranceFactor = toleranceFactor(firstLevel, rng, cfg.fallbackToleranceBase, cfg.fallbackToleranceRange),
            category = "math",
            hint = null,
            timeWeightHigher = true
        )
    }

    private fun generateSpeedTime(rng: Random, firstLevel: Boolean): GeneratedEstimation {
        val cfg = parametricConfig.speedTime()
        val speedKmh = cfg.speeds[rng.nextInt(cfg.speeds.size)]
        val distanceKm = cfg.distances[rng.nextInt(cfg.distances.size)]
        val timeHours = distanceKm.toDouble() / speedKmh
        val askInMinutes = firstLevel && cfg.askInMinutesOnFirstLevel && rng.nextBoolean()
        val (prompt, correctAnswer, unit, hint) = if (askInMinutes) {
            val minutes = timeHours * 60
            Quadruple(
                cfg.promptMinutes.replace("{speed}", speedKmh.toString()).replace("{distance}", distanceKm.toString()),
                minutes,
                "minutes",
                cfg.hintMinutes
            )
        } else {
            Quadruple(
                cfg.promptHours.replace("{speed}", speedKmh.toString()).replace("{distance}", distanceKm.toString()),
                timeHours,
                "hours",
                cfg.hintHours
            )
        }
        val tol = toleranceFactor(firstLevel, rng, cfg.toleranceBase, cfg.toleranceRange)
        return GeneratedEstimation(
            prompt = prompt,
            correctAnswer = correctAnswer,
            unit = unit,
            toleranceFactor = tol,
            category = "math",
            hint = hint,
            timeWeightHigher = false
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun generateConversionHMin(rng: Random, firstLevel: Boolean): GeneratedEstimation {
        val cfg = parametricConfig.conversionHMin()
        val hours = cfg.hoursValues[rng.nextInt(cfg.hoursValues.size)]
        val minutes = hours * 60
        val tol = toleranceFactor(firstLevel, rng, cfg.toleranceBase, cfg.toleranceRange)
        return GeneratedEstimation(
            prompt = cfg.prompt.replace("{hours}", hours.toString()),
            correctAnswer = minutes,
            unit = "minutes",
            toleranceFactor = tol,
            category = "math",
            hint = cfg.hint,
            timeWeightHigher = false
        )
    }

    private fun generateBudgetDays(difficultyScore: Int, rng: Random, firstLevel: Boolean): GeneratedEstimation {
        val cfg = parametricConfig.budgetDays()
        val budget = cfg.budgets[rng.nextInt(cfg.budgets.size)]
        val dailyRate = cfg.dailyRates[rng.nextInt(cfg.dailyRates.size)]
        val days = budget.toDouble() / dailyRate
        val askInMonths = difficultyScore >= cfg.askInMonthsWhenDifficultyScoreMin && rng.nextBoolean()
        val (prompt, correctAnswer, unit, hint) = if (askInMonths) {
            val months = days / 30.0
            Quadruple(
                cfg.promptMonths.replace("{budget}", (budget / 1000).toString()).replace("{dailyRate}", dailyRate.toString()),
                months,
                "months",
                cfg.hintMonths
            )
        } else {
            Quadruple(
                cfg.promptDays.replace("{budget}", (budget / 1000).toString()).replace("{dailyRate}", dailyRate.toString()),
                days,
                "days",
                cfg.hintDays
            )
        }
        val tol = toleranceFactor(firstLevel, rng, cfg.toleranceBase, cfg.toleranceRange)
        return GeneratedEstimation(
            prompt = prompt,
            correctAnswer = correctAnswer,
            unit = unit,
            toleranceFactor = tol,
            category = "math",
            hint = hint,
            timeWeightHigher = false
        )
    }
}

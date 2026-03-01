package app.antidoomscroll.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

/**
 * Parametric estimation config loaded from estimation-parametric.json.
 * Makes budget_days, speed_time, conversion_h_min, pure_math tunable without code changes.
 */
@Component
class EstimationParametricConfig {

    private val mapper = jacksonObjectMapper()
    private val config: Map<String, Any?> by lazy {
        ClassPathResource("estimation/estimation-parametric.json").inputStream.use {
            @Suppress("UNCHECKED_CAST")
            mapper.readValue<Map<String, Any?>>(it)
        }
    }

    fun firstLevel(): FirstLevelConfig =
        (config["firstLevel"] as? Map<*, *>)?.let { m ->
            FirstLevelConfig(
                toleranceFactorMin = (m["toleranceFactorMin"] as? Number)?.toDouble() ?: defaultFirstLevel.toleranceFactorMin,
                toleranceFactorMax = (m["toleranceFactorMax"] as? Number)?.toDouble() ?: defaultFirstLevel.toleranceFactorMax
            )
        } ?: defaultFirstLevel

    fun budgetDays(): BudgetDaysConfig =
        (config["budget_days"] as? Map<*, *>)?.let { m ->
            BudgetDaysConfig(
                minLevelIndex = (m["minLevelIndex"] as? Number)?.toInt() ?: defaultBudgetDays.minLevelIndex,
                budgets = (m["budgets"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: defaultBudgetDays.budgets,
                dailyRates = (m["dailyRates"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: defaultBudgetDays.dailyRates,
                askInMonthsWhenDifficultyScoreMin = (m["askInMonthsWhenDifficultyScoreMin"] as? Number)?.toInt() ?: 50,
                promptDays = m["promptDays"]?.toString() ?: defaultBudgetDays.promptDays,
                promptMonths = m["promptMonths"]?.toString() ?: defaultBudgetDays.promptMonths,
                toleranceBase = (m["toleranceBase"] as? Number)?.toDouble() ?: 1.15,
                toleranceRange = (m["toleranceRange"] as? Number)?.toDouble() ?: 0.1,
                hintDays = m["hintDays"]?.toString() ?: defaultBudgetDays.hintDays,
                hintMonths = m["hintMonths"]?.toString() ?: defaultBudgetDays.hintMonths
            )
        } ?: defaultBudgetDays

    fun speedTime(): SpeedTimeConfig =
        (config["speed_time"] as? Map<*, *>)?.let { m ->
            SpeedTimeConfig(
                speeds = (m["speeds"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: defaultSpeedTime.speeds,
                distances = (m["distances"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: defaultSpeedTime.distances,
                askInMinutesOnFirstLevel = m["askInMinutesOnFirstLevel"] as? Boolean ?: true,
                promptHours = m["promptHours"]?.toString() ?: defaultSpeedTime.promptHours,
                promptMinutes = m["promptMinutes"]?.toString() ?: defaultSpeedTime.promptMinutes,
                toleranceBase = (m["toleranceBase"] as? Number)?.toDouble() ?: 1.2,
                toleranceRange = (m["toleranceRange"] as? Number)?.toDouble() ?: 0.1,
                hintHours = m["hintHours"]?.toString() ?: defaultSpeedTime.hintHours,
                hintMinutes = m["hintMinutes"]?.toString() ?: defaultSpeedTime.hintMinutes
            )
        } ?: defaultSpeedTime

    fun conversionHMin(): ConversionHMinConfig =
        (config["conversion_h_min"] as? Map<*, *>)?.let { m ->
            ConversionHMinConfig(
                hoursValues = (m["hoursValues"] as? List<*>)?.mapNotNull { (it as? Number)?.toDouble() } ?: defaultConversionHMin.hoursValues,
                prompt = m["prompt"]?.toString() ?: defaultConversionHMin.prompt,
                toleranceBase = (m["toleranceBase"] as? Number)?.toDouble() ?: 1.15,
                toleranceRange = (m["toleranceRange"] as? Number)?.toDouble() ?: 0.1,
                hint = m["hint"]?.toString() ?: defaultConversionHMin.hint
            )
        } ?: defaultConversionHMin

    fun pureMath(): PureMathConfig =
        (config["pure_math"] as? Map<*, *>)?.let { m ->
            PureMathConfig(
                toleranceBase = (m["toleranceBase"] as? Number)?.toDouble() ?: 1.05,
                toleranceRange = (m["toleranceRange"] as? Number)?.toDouble() ?: 0.15,
                fallbackToleranceBase = (m["fallbackToleranceBase"] as? Number)?.toDouble() ?: 1.15,
                fallbackToleranceRange = (m["fallbackToleranceRange"] as? Number)?.toDouble() ?: 0.1,
                fallbackDivisorRange = (m["fallbackDivisorRange"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.take(2) ?: listOf(2, 12),
                fallbackQuotientRange = (m["fallbackQuotientRange"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.take(2) ?: listOf(10, 200)
            )
        } ?: defaultPureMath

    data class FirstLevelConfig(
        val toleranceFactorMin: Double,
        val toleranceFactorMax: Double
    )

    data class BudgetDaysConfig(
        val minLevelIndex: Int,
        val budgets: List<Int>,
        val dailyRates: List<Int>,
        val askInMonthsWhenDifficultyScoreMin: Int,
        val promptDays: String,
        val promptMonths: String,
        val toleranceBase: Double,
        val toleranceRange: Double,
        val hintDays: String,
        val hintMonths: String
    )

    data class SpeedTimeConfig(
        val speeds: List<Int>,
        val distances: List<Int>,
        val askInMinutesOnFirstLevel: Boolean,
        val promptHours: String,
        val promptMinutes: String,
        val toleranceBase: Double,
        val toleranceRange: Double,
        val hintHours: String,
        val hintMinutes: String
    )

    data class ConversionHMinConfig(
        val hoursValues: List<Double>,
        val prompt: String,
        val toleranceBase: Double,
        val toleranceRange: Double,
        val hint: String
    )

    data class PureMathConfig(
        val toleranceBase: Double,
        val toleranceRange: Double,
        val fallbackToleranceBase: Double,
        val fallbackToleranceRange: Double,
        val fallbackDivisorRange: List<Int>,
        val fallbackQuotientRange: List<Int>
    )

    private companion object {
        val defaultFirstLevel = FirstLevelConfig(
            toleranceFactorMin = 1.5,
            toleranceFactorMax = 1.7
        )
        val defaultBudgetDays = BudgetDaysConfig(
            minLevelIndex = 10,
            budgets = listOf(12_000, 24_000, 36_000, 72_000, 100_000, 150_000),
            dailyRates = listOf(300, 450, 612, 800, 1200),
            askInMonthsWhenDifficultyScoreMin = 50,
            promptDays = "Budget is {budget}K, daily rate is {dailyRate}. How many days can you cover?",
            promptMonths = "Budget is {budget}K, daily rate is {dailyRate}. How many months can you cover?",
            toleranceBase = 1.15,
            toleranceRange = 0.1,
            hintDays = "days = budget ÷ daily rate",
            hintMonths = "days = budget ÷ daily rate; then ÷ 30 for months"
        )
        val defaultSpeedTime = SpeedTimeConfig(
            speeds = listOf(50, 80, 90, 100, 110, 120, 130, 140),
            distances = listOf(30, 50, 75, 100, 150, 200, 250),
            askInMinutesOnFirstLevel = true,
            promptHours = "A car travels at {speed} km/h. How long (in hours) to cover {distance} km?",
            promptMinutes = "A car travels at {speed} km/h. How long (in minutes) to cover {distance} km?",
            toleranceBase = 1.2,
            toleranceRange = 0.1,
            hintHours = "time = distance ÷ speed",
            hintMinutes = "time = distance ÷ speed; 1 h = 60 min"
        )
        val defaultConversionHMin = ConversionHMinConfig(
            hoursValues = listOf(0.5, 1.0, 1.5, 2.0, 2.5, 3.0),
            prompt = "How many minutes are in {hours} hours?",
            toleranceBase = 1.15,
            toleranceRange = 0.1,
            hint = "1 hour = 60 minutes"
        )
        val defaultPureMath = PureMathConfig(
            toleranceBase = 1.05,
            toleranceRange = 0.15,
            fallbackToleranceBase = 1.15,
            fallbackToleranceRange = 0.1,
            fallbackDivisorRange = listOf(2, 12),
            fallbackQuotientRange = listOf(10, 200)
        )
    }
}

package app.antidoomscroll.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * Curated estimation items (fact, math, problem) loaded from estimation-items.json.
 * Variables are resolved per level with a seed for reproducible draws.
 * JSON level 1–20 maps to ladder levelIndex 0–19.
 */
@Component
class EstimationCuratedItems(
    private val parametricConfig: EstimationParametricConfig
) {

    private val mapper = jacksonObjectMapper()
    private val items: List<EstimationItem> by lazy {
        @Suppress("UNCHECKED_CAST")
        val raw = ClassPathResource("estimation/estimation-items.json").inputStream.use {
            mapper.readValue<List<Map<String, Any?>>>(it)
        }
        raw.map { map ->
            when (map["type"]?.toString()) {
                "math" -> EstimationItem.Math(
                    level = (map["level"] as? Number)?.toInt() ?: 1,
                    item = map["item"]?.toString() ?: "",
                    variables = (map["variables"] as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value.toString() } ?: emptyMap(),
                    answerFormula = map["answer_formula"]?.toString() ?: ""
                )
                "fact" -> EstimationItem.Fact(
                    level = (map["level"] as? Number)?.toInt() ?: 1,
                    item = map["item"]?.toString() ?: "",
                    answer = (map["answer"] as? Number) ?: 0,
                    tolerancePercent = (map["tolerance_percent"] as? Number)?.toDouble() ?: 20.0
                )
                else -> null
            }
        }.filterNotNull()
    }

    /** Items for a given ladder level (levelIndex 0 → JSON level 1, etc.). */
    fun itemsForLevel(levelIndex: Int): List<EstimationItem> {
        val jsonLevel = (levelIndex + 1).coerceIn(1, 20)
        return items.filter { it.level == jsonLevel }
    }

    /**
     * Resolve one curated item for the level: sample variables, evaluate formula, build prompt and answer.
     * Returns null if no items for level or resolution fails.
     */
    fun resolve(
        levelIndex: Int,
        seed: Long
    ): EstimationExerciseGenerator.GeneratedEstimation? {
        val levelItems = itemsForLevel(levelIndex)
        if (levelItems.isEmpty()) return null
        val rng = Random(seed)
        val item = levelItems[rng.nextInt(levelItems.size)]
        return when (item) {
            is EstimationItem.Math -> resolveMath(item, rng, levelIndex <= 4)
            is EstimationItem.Fact -> resolveFact(item, levelIndex <= 4)
        }
    }

    private fun resolveMath(
        item: EstimationItem.Math,
        rng: Random,
        firstLevel: Boolean
    ): EstimationExerciseGenerator.GeneratedEstimation {
        val vars = item.variables.mapValues { (_, rangeStr) ->
            parseRange(rangeStr)?.let { rng.nextInt(it.first, it.last + 1) } ?: 0
        }
        val promptExpr = substituteVarsInPrompt(item.item, vars)
        val answer = evaluateFormula(item.answerFormula, vars)
        val firstCfg = parametricConfig.firstLevel()
        val tol = if (firstLevel) {
            firstCfg.toleranceFactorMin + rng.nextDouble() * (firstCfg.toleranceFactorMax - firstCfg.toleranceFactorMin)
        } else {
            1.1 + rng.nextDouble() * 0.15
        }
        return EstimationExerciseGenerator.GeneratedEstimation(
            prompt = "Estimate: $promptExpr = ?",
            correctAnswer = answer,
            unit = "",
            toleranceFactor = tol,
            category = "math",
            hint = null,
            timeWeightHigher = true
        )
    }

    private fun resolveFact(item: EstimationItem.Fact, firstLevel: Boolean): EstimationExerciseGenerator.GeneratedEstimation {
        val tol = 1.0 + (item.tolerancePercent / 100.0)
        val adjustedTol = if (firstLevel) maxOf(tol, parametricConfig.firstLevel().toleranceFactorMin) else tol
        val prompt = if (item.item.endsWith("?")) "Estimate: ${item.item}" else "Estimate: ${item.item}?"
        return EstimationExerciseGenerator.GeneratedEstimation(
            prompt = prompt,
            correctAnswer = item.answer.toDouble(),
            unit = "",
            toleranceFactor = adjustedTol,
            category = "math",
            hint = null,
            timeWeightHigher = false
        )
    }

    private fun parseRange(s: String): IntRange? {
        val regex = Regex("\\[(\\d+)-(\\d+)\\]")
        val m = regex.matchEntire(s.trim()) ?: return null
        val low = m.groupValues[1].toIntOrNull() ?: return null
        val high = m.groupValues[2].toIntOrNull() ?: return null
        return if (low <= high) low..high else null
    }

    private fun substituteVarsInPrompt(expr: String, vars: Map<String, Int>): String {
        var out = expr
        for ((name, value) in vars) {
            out = Regex("\\b$name\\b").replace(out, value.toString())
        }
        return out
    }

    private fun evaluateFormula(formula: String, vars: Map<String, Int>): Double {
        var expr = formula
        for ((name, value) in vars) {
            expr = Regex("\\b$name\\b").replace(expr, value.toString())
        }
        expr = expr.replace(" ", "")
        return evalExpr(expr)
    }

    private fun evalExpr(s: String): Double {
        val parser = object {
            var i = 0
            val str get() = s
            fun skipWs() { while (i < str.length && str[i] == ' ') i++ }
            fun parseExpr(): Double {
                var v = parseTerm()
                skipWs()
                while (i < str.length) {
                    when (str[i]) {
                        '+' -> { i++; v += parseTerm() }
                        '-' -> { i++; v -= parseTerm() }
                        else -> break
                    }
                    skipWs()
                }
                return v
            }
            fun parseTerm(): Double {
                var v = parseFactor()
                skipWs()
                while (i < str.length) {
                    when (str[i]) {
                        '*' -> { i++; v *= parseFactor() }
                        '/' -> { i++; v /= parseFactor() }
                        else -> break
                    }
                    skipWs()
                }
                return v
            }
            fun parseFactor(): Double {
                skipWs()
                if (i < str.length && str[i] == '(') {
                    i++
                    val v = parseExpr()
                    skipWs()
                    if (i < str.length && str[i] == ')') i++
                    return v
                }
                val start = i
                while (i < str.length && (str[i].isDigit() || str[i] == '.')) i++
                return str.substring(start, i).toDoubleOrNull() ?: 0.0
            }
        }
        return parser.parseExpr()
    }

    sealed class EstimationItem {
        abstract val level: Int
        data class Math(
            override val level: Int,
            val item: String,
            val variables: Map<String, String>,
            val answerFormula: String
        ) : EstimationItem()
        data class Fact(
            override val level: Int,
            val item: String,
            val answer: Number,
            val tolerancePercent: Double
        ) : EstimationItem()
    }
}

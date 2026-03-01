package app.antidoomscroll.domain

/**
 * Stimulus for dual grid N-back: position + color.
 */
data class GridStimulus(
    val position: Int,
    val color: String
)

/**
 * Params for dual N-back on a grid: position and color are independent match targets.
 * - matchPositionIndices: sequence[i].position == sequence[i-n].position
 * - matchColorIndices: sequence[i].color == sequence[i-n].color
 */
data class DualNBackGridParams(
    val n: Int,
    val sequence: List<GridStimulus>,
    val matchPositionIndices: List<Int>,
    val matchColorIndices: List<Int>,
    val colors: List<String>,
    val gridSize: Int = 3
) {
    init {
        require(n >= 1) { "n must be >= 1" }
        require(gridSize in 2..5) { "gridSize must be 2..5" }
        require(colors.size in 2..4) { "colors must be 2..4" }
        val maxPos = gridSize * gridSize - 1
        require(sequence.size >= n + 2) { "sequence must have at least n+2 items" }
        require(sequence.all { it.position in 0..maxPos && it.color in colors }) {
            "sequence positions must be 0..$maxPos and colors in palette"
        }
        val validRange = n until sequence.size
        require(matchPositionIndices.all { it in validRange }) { "matchPositionIndices must be valid" }
        require(matchColorIndices.all { it in validRange }) { "matchColorIndices must be valid" }
    }

    fun evaluate(userPositionResponses: Set<Int>, userColorResponses: Set<Int>): DualNBackScore {
        val posSet = matchPositionIndices.toSet()
        val colorSet = matchColorIndices.toSet()
        val posHits = userPositionResponses.intersect(posSet).size
        val posMisses = posSet.size - posHits
        val posFalseAlarms = userPositionResponses.minus(posSet).size
        val colorHits = userColorResponses.intersect(colorSet).size
        val colorMisses = colorSet.size - colorHits
        val colorFalseAlarms = userColorResponses.minus(colorSet).size
        return DualNBackScore(
            positionHits = posHits,
            positionMisses = posMisses,
            positionFalseAlarms = posFalseAlarms,
            positionTargets = posSet.size,
            colorHits = colorHits,
            colorMisses = colorMisses,
            colorFalseAlarms = colorFalseAlarms,
            colorTargets = colorSet.size
        )
    }
}

data class DualNBackScore(
    val positionHits: Int,
    val positionMisses: Int,
    val positionFalseAlarms: Int,
    val positionTargets: Int,
    val colorHits: Int,
    val colorMisses: Int,
    val colorFalseAlarms: Int,
    val colorTargets: Int
) {
    fun combinedNormalizedScore(): Double {
        val totalTargets = positionTargets + colorTargets
        if (totalTargets == 0) return 1.0
        val totalHits = positionHits + colorHits
        val totalFalseAlarms = positionFalseAlarms + colorFalseAlarms
        val hitBonus = totalHits.toDouble() / totalTargets
        val penalty = (totalFalseAlarms * 0.1).coerceAtMost(0.3)
        return (hitBonus - penalty).coerceIn(0.0, 1.0)
    }

    private fun modalityAccuracy(hits: Int, targets: Int) =
        if (targets == 0) 1.0 else (hits.toDouble() / targets).coerceIn(0.0, 1.0)

    /** Extensible subscore details. For grid dual: modality1=Position, modality2=Color. For card dual: modality1=Number, modality2=Color. */
    fun toSubscoreDetails(modality1Label: String = "Position", modality2Label: String = "Color"): List<Pair<String, String>> =
        buildList {
            add("$modality1Label hits" to "$positionHits/$positionTargets")
            if (positionMisses > 0) add("$modality1Label misses" to "$positionMisses")
            if (positionFalseAlarms > 0) add("$modality1Label false alarms" to "$positionFalseAlarms")
            add("$modality1Label accuracy" to "${(modalityAccuracy(positionHits, positionTargets) * 100).toInt()}%")
            add("$modality2Label hits" to "$colorHits/$colorTargets")
            if (colorMisses > 0) add("$modality2Label misses" to "$colorMisses")
            if (colorFalseAlarms > 0) add("$modality2Label false alarms" to "$colorFalseAlarms")
            add("$modality2Label accuracy" to "${(modalityAccuracy(colorHits, colorTargets) * 100).toInt()}%")
            val totalT = positionTargets + colorTargets
            val totalH = positionHits + colorHits
            add("Overall accuracy" to "${(modalityAccuracy(totalH, totalT) * 100).toInt()}%")
        }
}

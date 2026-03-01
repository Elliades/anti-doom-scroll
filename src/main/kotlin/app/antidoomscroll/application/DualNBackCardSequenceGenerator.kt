package app.antidoomscroll.application

import kotlin.random.Random

/**
 * Generates dual N-Back card sequences for DUAL_NBACK_CARD exercises.
 * Suit (color) and rank (number) are tracked as independent match channels.
 * ~35% chance of suit match and ~35% chance of rank match at each eligible position.
 * The two channels are sampled independently.
 *
 * Card codes: "<rank><suit>" e.g. "AC", "10H", "KS".
 * Suit chars: C=clubs, D=diamonds, H=hearts, S=spades.
 */
object DualNBackCardSequenceGenerator {

    private val SUITS = listOf('C', 'D', 'H', 'S')
    private val RANKS = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")

    data class DualCardResult(
        val sequence: List<String>,
        val matchColorIndices: List<Int>,
        val matchNumberIndices: List<Int>
    )

    /**
     * @param n N-back step count (≥ 1)
     * @param suitCount Number of suits to use (1–4)
     * @param sequenceLength Number of cards in the sequence (default 12)
     * @param seed Random seed
     */
    fun generate(
        n: Int,
        suitCount: Int = 4,
        sequenceLength: Int = 12,
        seed: Int = 0
    ): DualCardResult {
        require(n >= 1) { "n must be >= 1" }
        require(suitCount in 1..4) { "suitCount must be 1..4" }
        require(sequenceLength >= n + 2) { "sequence must have at least n+2 items" }

        val suits = SUITS.take(suitCount)
        val random = Random(seed)

        val rankSeq = mutableListOf<String>()
        val suitSeq = mutableListOf<Char>()
        val matchColorIndices = mutableListOf<Int>()
        val matchNumberIndices = mutableListOf<Int>()

        repeat(sequenceLength) { i ->
            if (i < n) {
                rankSeq.add(RANKS[random.nextInt(RANKS.size)])
                suitSeq.add(suits[random.nextInt(suits.size)])
            } else {
                val prevRank = rankSeq[i - n]
                val prevSuit = suitSeq[i - n]

                val suitMatch = random.nextFloat() < 0.35f
                val rankMatch = random.nextFloat() < 0.35f

                if (suitMatch) {
                    suitSeq.add(prevSuit)
                    matchColorIndices.add(i)
                } else {
                    val candidates = suits.filter { it != prevSuit }
                    suitSeq.add(
                        if (candidates.isNotEmpty()) candidates[random.nextInt(candidates.size)]
                        else suits[random.nextInt(suits.size)]
                    )
                }

                if (rankMatch) {
                    rankSeq.add(prevRank)
                    matchNumberIndices.add(i)
                } else {
                    val candidates = RANKS.filter { it != prevRank }
                    rankSeq.add(
                        if (candidates.isNotEmpty()) candidates[random.nextInt(candidates.size)]
                        else RANKS[random.nextInt(RANKS.size)]
                    )
                }
            }
        }

        val sequence = rankSeq.mapIndexed { i, rank -> "$rank${suitSeq[i]}" }

        return DualCardResult(
            sequence = sequence,
            matchColorIndices = matchColorIndices,
            matchNumberIndices = matchNumberIndices
        )
    }
}

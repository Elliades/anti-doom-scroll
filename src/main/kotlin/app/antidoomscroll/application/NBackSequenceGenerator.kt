package app.antidoomscroll.application

import kotlin.random.Random

/**
 * Generates N-Back card sequences from input parameters.
 * Input params: N (step count), suitCount (1-4 = number of suits).
 * Easy: N=1, suitCount=1 (clubs only).
 * Harder: N=2, suitCount=2; N=3, suitCount=4.
 */
object NBackSequenceGenerator {

    /** Suit chars: C=clubs, D=diamonds, H=hearts, S=spades (1..4) */
    private const val SUITS = "CDHS"

    /** Ranks for cards: A, 2-10, J, Q, K */
    private val RANKS = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")

    /**
     * Generates (sequence, matchIndices) from n, suitCount.
     * @param n N-back step count (>= 1)
     * @param suitCount Number of suits to use (1-4)
     * @param sequenceLength Desired sequence length (default 12)
     * @param seed For reproducibility (e.g. exercise ID hash)
     */
    fun generate(
        n: Int,
        suitCount: Int,
        sequenceLength: Int = 12,
        seed: Int = 0
    ): Pair<List<String>, List<Int>> {
        require(n >= 1) { "n must be >= 1" }
        require(suitCount in 1..4) { "suitCount must be 1..4" }
        require(sequenceLength >= n + 2) { "sequence must have at least n+2 items" }

        val random = Random(seed)
        val suitsToUse = SUITS.take(suitCount)
        val pool = buildList {
            for (rank in RANKS) {
                for (suit in suitsToUse) {
                    add("$rank$suit")
                }
            }
        }.shuffled(random)

        val sequence = mutableListOf<String>()
        val matchIndices = mutableListOf<Int>()

        repeat(sequenceLength) { i ->
            if (i < n) {
                sequence.add(pool.random(random))
            } else {
                val prevCard = sequence[i - n]
                if (random.nextFloat() < 0.35f && pool.size > 1) {
                    sequence.add(prevCard)
                    matchIndices.add(i)
                } else {
                    val other = pool.filter { it != prevCard }
                    sequence.add(if (other.isNotEmpty()) other.random(random) else pool.random(random))
                }
            }
        }

        return sequence to matchIndices
    }
}

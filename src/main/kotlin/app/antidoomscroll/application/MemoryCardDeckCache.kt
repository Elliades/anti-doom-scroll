package app.antidoomscroll.application

import app.antidoomscroll.domain.MemoryCardParams
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * Generates Memory Card deck order on each request. Content is randomly shuffled
 * so users get fresh layouts each time they play the same exercise.
 */
@Component
class MemoryCardDeckCache {

    @Cacheable(cacheNames = ["memoryCardDeck"], key = "#exerciseId", condition = "false")
    fun getOrGenerate(exerciseId: String, params: MemoryCardParams, random: Random = Random.Default): List<String> {
        val pairs = params.symbols.flatMap { s -> listOf(s, s) }
        return pairs.shuffled(random)
    }
}

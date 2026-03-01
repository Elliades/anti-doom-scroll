package app.antidoomscroll.application

import app.antidoomscroll.domain.SumPairParams
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

/**
 * Generates Sum Pair groups on each request. Content is randomly generated
 * so users get fresh pairs each time they play the same exercise.
 */
@Component
class SumPairRoundsCache(private val sumPairGenerator: SumPairGenerator) {

    @Cacheable(cacheNames = ["sumPairRounds"], key = "#exerciseId", condition = "false")
    fun getOrGenerate(exerciseId: String, params: SumPairParams): SumPairResult =
        sumPairGenerator.generateGroups(params)
}

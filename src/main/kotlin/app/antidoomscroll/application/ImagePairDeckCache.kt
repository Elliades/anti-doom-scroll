package app.antidoomscroll.application

import app.antidoomscroll.domain.ImagePairParams
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * Generates IMAGE_PAIR deck on each request. Content is randomly generated
 * so users get fresh layouts each time they play the same exercise.
 */
@Component
class ImagePairDeckCache(private val imagePairGenerator: ImagePairGenerator) {

    @Cacheable(cacheNames = ["imagePairDeck"], key = "#exerciseId", condition = "false")
    fun getOrGenerate(exerciseId: String, params: ImagePairParams, random: Random = Random.Default): ImagePairResult =
        imagePairGenerator.generate(params, random)
}

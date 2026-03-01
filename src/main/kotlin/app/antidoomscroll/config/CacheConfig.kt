package app.antidoomscroll.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Simple in-memory cache so ultra-easy exercise is served in <1s on reopen.
 * No Redis required for MVP.
 */
@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): ConcurrentMapCacheManager =
        ConcurrentMapCacheManager().apply {
            setCacheNames(listOf(
                "ultraEasyExercise",
                "ultraEasyExerciseByType",
                "nBackByLevel",
                "sumPairRounds",
                "memoryCardDeck",
                "imagePairDeck"
            ))
        }
}

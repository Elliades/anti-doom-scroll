package app.antidoomscroll.application

import app.antidoomscroll.domain.WordleParams
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.text.Normalizer
import kotlin.random.Random

/** Lowercase, strip combining marks, œ→oe æ→ae — matches frontend WordleExercise.normalizeForCompare. */
private fun normalizeWordleWord(raw: String): String {
    val nfd = Normalizer.normalize(raw, Normalizer.Form.NFD)
    val noMarks = nfd.replace(Regex("\\p{M}+"), "")
    val lower = noMarks.lowercase()
    return lower.replace("œ", "oe").replace("æ", "ae")
}

data class WordleResult(
    val answer: String,
    val wordLength: Int,
    val maxAttempts: Int
)

/**
 * Loads word lists from src/main/resources/words/wordle_{lang}.json and picks
 * a random word of exactly the required length.
 */
@Component
class WordleGenerator {

    private val mapper = jacksonObjectMapper()
    private val wordCache = mutableMapOf<String, Map<Int, List<String>>>()

    fun generate(params: WordleParams, random: Random = Random.Default): WordleResult? {
        val wordsByLength = loadWords(params.language) ?: return null
        val words = wordsByLength[params.wordLength]
        if (words.isNullOrEmpty()) return null
        val answer = normalizeWordleWord(words.random(random))
        return WordleResult(answer = answer, wordLength = params.wordLength, maxAttempts = params.maxAttempts)
    }

    private fun loadWords(lang: String): Map<Int, List<String>>? {
        return wordCache.getOrPut(lang) {
            runCatching {
                val resource = ClassPathResource("words/wordle_$lang.json")
                if (!resource.exists()) return@runCatching emptyMap()
                val words = mapper.readValue<List<String>>(resource.inputStream)
                words.map { normalizeWordleWord(it) }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .groupBy { it.length }
            }.getOrNull() ?: emptyMap()
        }.takeIf { it.isNotEmpty() }
    }
}

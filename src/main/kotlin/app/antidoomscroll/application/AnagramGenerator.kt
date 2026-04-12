package app.antidoomscroll.application

import app.antidoomscroll.domain.AnagramParams
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import kotlin.random.Random
import java.text.Normalizer

data class AnagramResult(
    val scrambledLetters: List<String>,
    val answer: String
)

/**
 * Loads word lists from src/main/resources/words/{lang}.json and generates
 * anagrams. Same component serves French and English; language comes from params.
 */
@Component
class AnagramGenerator {

    private val mapper = jacksonObjectMapper()
    private val wordCache = mutableMapOf<String, List<String>>()

    fun generate(params: AnagramParams, random: Random = Random.Default): AnagramResult? {
        val words = loadWords(params.language) ?: return null
        var filtered = words.filter { w ->
            val len = w.length
            len in params.minLetters..params.maxLetters
        }
        if (filtered.isEmpty()) {
            filtered = words.filter { it.length >= params.minLetters }
        }
        if (filtered.isEmpty()) return null
        val raw = filtered.random(random)
        val answer = Normalizer.normalize(raw, Normalizer.Form.NFC)
        val codePoints = buildList {
            var i = 0
            while (i < answer.length) {
                val cp = answer.codePointAt(i)
                add(cp)
                i += Character.charCount(cp)
            }
        }
        val scrambled = codePoints.shuffled(random).map { cp -> String(Character.toChars(cp)) }
        return AnagramResult(scrambledLetters = scrambled, answer = answer)
    }

    private fun loadWords(lang: String): List<String>? {
        return wordCache.getOrPut(lang) {
            runCatching {
                val resource = ClassPathResource("words/$lang.json")
                if (!resource.exists()) return@runCatching emptyList()
                mapper.readValue<List<String>>(resource.inputStream)
                    .map { Normalizer.normalize(it, Normalizer.Form.NFC) }
            }.getOrNull() ?: emptyList()
        }.takeIf { it.isNotEmpty() }
    }
}

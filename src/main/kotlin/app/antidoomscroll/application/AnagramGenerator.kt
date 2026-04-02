package app.antidoomscroll.application

import app.antidoomscroll.domain.AnagramParams
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import kotlin.random.Random
import java.text.BreakIterator
import java.text.Normalizer
import java.util.Locale

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
            val len = graphemeCount(w)
            len in params.minLetters..params.maxLetters
        }
        if (filtered.isEmpty()) {
            // No exact [min,max] hits: prefer the shortest word ≥ minLetters (user-perceived length).
            val atLeastMin = words.filter { graphemeCount(it) >= params.minLetters }
            if (atLeastMin.isNotEmpty()) {
                val lenCap = atLeastMin.minOf { graphemeCount(it) }
                filtered = atLeastMin.filter { graphemeCount(it) == lenCap }
            }
        }
        if (filtered.isEmpty()) return null
        val raw = filtered.random(random)
        val answer = Normalizer.normalize(raw, Normalizer.Form.NFC)
        val graphemes = graphemeList(answer)
        val scrambled = graphemes.shuffled(random)
        return AnagramResult(scrambledLetters = scrambled, answer = answer)
    }

    private fun graphemeCount(s: String): Int = graphemeList(s).size

    /** One string per user-perceived character (handles combining marks, e.g. é as e + acute). */
    private fun graphemeList(s: String): List<String> {
        val bi = BreakIterator.getCharacterInstance(Locale.ROOT)
        bi.setText(s)
        val out = mutableListOf<String>()
        var start = bi.first()
        var end = bi.next()
        while (end != BreakIterator.DONE) {
            out.add(s.substring(start, end))
            start = end
            end = bi.next()
        }
        return out
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

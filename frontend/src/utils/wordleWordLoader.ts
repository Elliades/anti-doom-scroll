/**
 * Chunked word lists (regenerate: npm run build:wordle-words).
 * Dynamic import loads only the length in use per exercise.
 */
export async function loadWordListWords(lang: string, wordLength: number): Promise<string[]> {
  if (wordLength < 3 || wordLength > 7) return []
  const L = wordLength as 3 | 4 | 5 | 6 | 7
  if (lang === 'en') {
    switch (L) {
      case 3:
        return (await import('../data/wordle_words/en/3.json')).default
      case 4:
        return (await import('../data/wordle_words/en/4.json')).default
      case 5:
        return (await import('../data/wordle_words/en/5.json')).default
      case 6:
        return (await import('../data/wordle_words/en/6.json')).default
      case 7:
        return (await import('../data/wordle_words/en/7.json')).default
      default:
        return []
    }
  }
  switch (L) {
    case 3:
      return (await import('../data/wordle_words/fr/3.json')).default
    case 4:
      return (await import('../data/wordle_words/fr/4.json')).default
    case 5:
      return (await import('../data/wordle_words/fr/5.json')).default
    case 6:
      return (await import('../data/wordle_words/fr/6.json')).default
    case 7:
      return (await import('../data/wordle_words/fr/7.json')).default
    default:
      return []
  }
}

# Exercise: Anagram

## Overview

Anagram is a word puzzle: letters are shuffled and the player must find the correct word.  
Design reference: **Wordle** (clean grid, keyboard, clear feedback).

## Word Databases

| Language | Source | License | Format | Size |
|----------|--------|---------|--------|------|
| **French** | [words/an-array-of-french-words](https://github.com/words/an-array-of-french-words) | MIT | `index.json` (array of lowercase strings) | ~336k |
| **English** | [words/an-array-of-english-words](https://github.com/words/an-array-of-english-words) | MIT | `index.json` | ~275k |

**Alternative English**: [dwyl/english-words](https://github.com/dwyl/english-words) — Unlicense, 479k words, `words_dictionary.json` (object keys).

**Integration**: Bundled JSON in `src/main/resources/words/{lang}.json`. Same exercise component for both languages; backend picks list by subject (`anagram-fr` vs `anagram-en`).

To expand the word list (e.g. full ~336k French words):

```bash
npm install an-array-of-french-words an-array-of-english-words
node scripts/fetch-anagram-words.js
```

This overwrites `fr.json` and `en.json` with filtered 2–10 letter words.

## Difficulty ↔ Letter Count

| Difficulty   | Letter range |
|--------------|--------------|
| ULTRA_EASY   | 2–3 letters  |
| EASY         | 3–4 letters  |
| MEDIUM       | 4–5 letters  |
| HARD         | 6–7 letters  |
| VERY_HARD    | 8+ letters   |

## Gameplay

1. **Display**: Shuffled letters (e.g. `RTA` → answer `ART`).
2. **Input**: Virtual keyboard with **only the letters of the anagram** (no full QWERTY).
3. **Hints**: Every 5 seconds until solved, reveal one letter in the correct position.
4. **Success**: Correct word → score, completion.

## API Params

```json
{
  "minLetters": 2,
  "maxLetters": 5,
  "language": "fr"
}
```

Backend generates at request time: `{ scrambledLetters: string[], answer: string }`.

## Reusability

- **Single component** `AnagramExercise.tsx` — language-agnostic.
- **Two subjects**: `anagram-fr` (French), `anagram-en` (English later).
- Words loaded by `language` in `exerciseParams` or inferred from subject code.

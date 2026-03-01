# Exercises index

**Goal:** Access exercises via URL **without knowing any exercise ID**. Base URL: `http://localhost:5173`.

---

## Subjects and exercises (by subject)

| Subject | Code | Exercises |
|---------|------|-----------|
| **Default** | `default` | FLASHCARD_QA ADD (4: ULTRA_EASY, EASY, MEDIUM, HARD), FLASHCARD_QA SUBTRACT (4), FLASHCARD_QA MULTIPLY (4), FLASHCARD_QA DIVIDE (4) |
| **N-back** | `B1` | N_BACK (3: 1-back, 2-back, 3-back), N_BACK_GRID (2), DUAL_NBACK_GRID (1), DUAL_NBACK_CARD (1) |
| **Memory** | `MEMORY` | MEMORY_CARD_PAIRS (3), SUM_PAIR (5), IMAGE_PAIR (2) |
| **Anagrammes (FR)** | `ANAGRAM_FR` | ANAGRAM (4: ULTRA_EASY, EASY, MEDIUM, HARD) |
| **Wordle (FR)** | `WORDLE_FR` | WORDLE (4: EASY=3 letters, MEDIUM=5, HARD=6, VERY_HARD=7) |
| **Wordle (EN)** | `WORDLE_EN` | WORDLE (4: EASY=3 letters, MEDIUM=5, HARD=6, VERY_HARD=7) |
| **Estimation** | `ESTIMATION` | ESTIMATION (20: ULTRA_EASY=5, EASY=5, MEDIUM=5, HARD=3, VERY_HARD=3) |

**Default** — Sum, Subtraction, Multiplication, Division (math flashcard) by difficulty.  
**B1** — N-back card, N-back grid, dual N-back grid, dual N-back card.  
**MEMORY** — Memory card pairs and sum-pair exercises.  
**ANAGRAM_FR** — French anagrams by difficulty.  
**WORDLE_FR** — French Wordle: guess the hidden word. EASY=3 letters, MEDIUM=5, HARD=6, VERY_HARD=7.  
**WORDLE_EN** — English Wordle: guess the hidden word. EASY=3 letters, MEDIUM=5, HARD=6, VERY_HARD=7.  
**ESTIMATION** — Approximate numerical answers (geography, science, mental math). Scored on accuracy + speed.
---

## If /api/subjects and /api/exercises are empty

- **Using in-memory (no PostgreSQL):** Start the backend with the **local** profile so seed data is loaded:
  - `./gradlew bootRun --args='--spring.profiles.active=local'`  
  - Then open [http://localhost:5173/api/subjects](http://localhost:5173/api/subjects) and [http://localhost:5173/api/exercises](http://localhost:5173/api/exercises).
- **Using PostgreSQL:** Ensure the database exists and the app can connect; Flyway runs on first boot and seeds subjects and exercises. If the DB was created empty or migrations failed, check logs and DB connection.

---

## Main URLs (no ID needed)

| What you want | URL (GET) |
|---------------|-----------|
| **All subjects** | [http://localhost:5173/api/subjects](http://localhost:5173/api/subjects) |
| **All exercises** | [http://localhost:5173/api/exercises](http://localhost:5173/api/exercises) |

---

## More URLs

| What you want | URL (GET) |
|---------------|-----------|
| **N-Back 1** | [http://localhost:5173/api/nback/1](http://localhost:5173/api/nback/1) |
| **N-Back 2** | [http://localhost:5173/api/nback/2](http://localhost:5173/api/nback/2) |
| **N-Back 3** | [http://localhost:5173/api/nback/3](http://localhost:5173/api/nback/3) |
| **Exercises in subject “default”** | [http://localhost:5173/api/subjects/default/exercises](http://localhost:5173/api/subjects/default/exercises) |
| **Exercises in subject “B1”** | [http://localhost:5173/api/subjects/B1/exercises](http://localhost:5173/api/subjects/B1/exercises) |
| **Start a session** (returns steps with exercises) | [http://localhost:5173/api/session/start](http://localhost:5173/api/session/start) |
| **Start ladder session** (score-based level progression) | [http://localhost:5173/api/session/start?mode=ladder](http://localhost:5173/api/session/start?mode=ladder) |
| **Start sum ladder** (math: ADD→SUBTRACT→MULT/DIV, levels 0–25) | [http://localhost:5173/api/session/start?mode=ladder&ladderCode=sum](http://localhost:5173/api/session/start?mode=ladder&ladderCode=sum) |
| **Start anagram ladder** (French words: 2-3 → 8-15 letters, levels 0–29) | [http://localhost:5173/api/session/start?mode=ladder&ladderCode=anagram](http://localhost:5173/api/session/start?mode=ladder&ladderCode=anagram) |
| **Start estimation ladder** (approximate numbers, 30 levels) | [http://localhost:5173/api/session/start?mode=ladder&ladderCode=estimation](http://localhost:5173/api/session/start?mode=ladder&ladderCode=estimation) |
| **Start session, prefer one type** | [http://localhost:5173/api/session/start?preferType=SUM_PAIR](http://localhost:5173/api/session/start?preferType=SUM_PAIR) |
| **Open-app session** | [http://localhost:5173/api/session/start?mode=openapp](http://localhost:5173/api/session/start?mode=openapp) |
| **Journey definition** | [http://localhost:5173/api/journey?code=default](http://localhost:5173/api/journey?code=default) |
| **Journey step 0 content** (e.g. open-app session) | [http://localhost:5173/api/journey/steps/0/content?journeyCode=default](http://localhost:5173/api/journey/steps/0/content?journeyCode=default) |
| **Journey step 1 content** (e.g. reflection) | [http://localhost:5173/api/journey/steps/1/content?journeyCode=default](http://localhost:5173/api/journey/steps/1/content?journeyCode=default) |
| **Journey step 2 content** (chapter exercises) | [http://localhost:5173/api/journey/steps/2/content?journeyCode=default&chapterIndex=0](http://localhost:5173/api/journey/steps/2/content?journeyCode=default&chapterIndex=0) |

**If you already have an exercise ID** (e.g. from a list response):  
[http://localhost:5173/api/exercises/{id}](http://localhost:5173/api/exercises/{id})

---

## Exercise types and params

### 1. Sum (FLASHCARD_QA, operation: ADD)

- **Display name:** Sum (API returns `mathOperation: "ADD"` for UI label).
- **Params:** `operation: ADD`, `firstMax`, `secondMax` — digit ranges per difficulty:
  - ULTRA_EASY (opening): 1 digit + 1–2 digits (max 3 digits total)
  - EASY: 1–2 digits + 1–2 digits
  - MEDIUM: 2–3 digits + 2–3 digits
  - HARD: 3–4 digits + 3–4 digits
- **Access (no ID):** [All exercises](http://localhost:5173/api/exercises) or [subjects](http://localhost:5173/api/subjects) then subject’s exercises, or session/journey.

---

### 1b. Subtraction (FLASHCARD_QA, operation: SUBTRACT)

- **Display name:** Subtraction (API returns `mathOperation: "SUBTRACT"` for UI label).
- **Params:** `operation: SUBTRACT`, `firstMax`, `secondMax`. Generator ensures subtrahend ≤ minuend (non-negative results). Same difficulty progression as Sum:
  - ULTRA_EASY: 1–2 digit − 1 digit (e.g. 12 − 5)
  - EASY: 1–2 digits − 1–2 digits
  - MEDIUM: 2–3 digits − 2–3 digits
  - HARD: 3–4 digits − 3–4 digits
- **Access (no ID):** Same as Sum (default subject); list shows "Subtraction" when `mathOperation` is SUBTRACT.

---

### 2. CLOZE

- **Params:** None (generic).
- **Access (no ID):** Same as above (subject list, session, journey).

---

### 3. QCM

- **Params:** None (generic).
- **Access (no ID):** Same as above.

---

### 4. KEYWORD_BLURTING

- **Params:** None (generic).
- **Access (no ID):** Same as above.

---

### 5. MINI_PROBLEM

- **Params:** None (generic).
- **Access (no ID):** Same as above.

---

### 6. N_BACK (Card)

- **Input params** (stored in `exercise_params`): `n`, `suitCount` (1–4). Easy: n=1, suitCount=1. Harder: n=2 suitCount=2; n=3 suitCount=4.
- **Params** (response as `nBackParams`): `sequence` and `matchIndices` are generated at runtime from (n, suitCount). Legacy: stored `sequence`+`matchIndices` still supported.

  | Field | Type | Description |
  |-------|------|-------------|
  | `n` | `Int` | Match current item with item N positions back (1 = previous). |
  | `sequence` | `List<String>` | Stimuli to display one-by-one. **Card codes** (e.g. `AC`, `2D`, `QH`) or letters. |
  | `matchIndices` | `List<Int>` | 0-based positions where current == item at (current - n). |

- **Access (no ID):** Use level URLs: [1-back](http://localhost:5173/api/nback/1), [2-back](http://localhost:5173/api/nback/2), [3-back](http://localhost:5173/api/nback/3).
- **Details:** See [exercise-nback.md](exercise-nback.md).

---

### 6b. N_BACK_GRID

- **Params** (response as `nBackGridParams`):

  | Field | Type | Description |
  |-------|------|-------------|
  | `n` | `Int` | Match current grid position with position N steps back. |
  | `sequence` | `List<Int>` | Grid cell indices 0–8 (3×3, row-major). |
  | `matchIndices` | `List<Int>` | Positions where current cell == cell at (current - n). |
  | `gridSize` | `Int` | Grid dimension (default 3). |

- **Details:** See [exercise-nback-architecture.md](exercise-nback-architecture.md).

---

### 6c. DUAL_NBACK_GRID

- **Params** (response as `dualNBackGridParams`):

  | Field | Type | Description |
  |-------|------|-------------|
  | `n` | `Int` | Same as single N-back. |
  | `sequence` | `List<{position, color}>` | Position (0–8) and color per stimulus. |
  | `matchPositionIndices` | `List<Int>` | Positions where current position == position N back. |
  | `matchColorIndices` | `List<Int>` | Positions where current color == color N back. |
  | `colors` | `List<String>` | Up to 4 hex colors for stimuli. |

- **User action:** "Match Position" or "Match Color" buttons.
- **Details:** See [exercise-nback-architecture.md](exercise-nback-architecture.md).

---

### 6d. DUAL_NBACK_CARD

- **Params** (response as `dualNBackCardParams`):

  | Field | Type | Description |
  |-------|------|-------------|
  | `n` | `Int` | Same as single N-back. |
  | `sequence` | `List<String>` | Card codes. |
  | `matchColorIndices` | `List<Int>` | Suit match (e.g. both Clubs). |
  | `matchNumberIndices` | `List<Int>` | Rank match (e.g. both 2). |

- **User action:** "Match Color" (suit) or "Match Number" (rank) buttons.
- **Details:** See [exercise-nback-architecture.md](exercise-nback-architecture.md).

---

### 7. MEMORY_CARD_PAIRS

- **Params** (response as `memoryCardParams`):

  | Field | Type | Description |
  |-------|------|-------------|
  | `pairCount` | `Int` | Number of pairs (total cards = 2 × pairCount). Must be ≥ 2. |
  | `symbols` | `List<String>` | One symbol per pair (e.g. emoji or letter); size must equal `pairCount`. |

- **Access (no ID):** [All exercises](http://localhost:5173/api/exercises) or [default](http://localhost:5173/api/subjects/default/exercises) / [B1](http://localhost:5173/api/subjects/B1/exercises) — list; each item has `id` if needed.

---

### 8. ANAGRAM

- **Params** (response as `anagramParams`); **generated at response time:**

  | Field | Type | Description |
  |-------|------|--------------|
  | `scrambledLetters` | `List<String>` | Shuffled letters to recompose. |
  | `answer` | `String` | Correct word. |
  | `hintIntervalSeconds` | `Int` | Hint every N seconds of inactivity (default 10). From exercise `exerciseParams.hintIntervalSeconds`. |
  | `letterColorHint` | `Boolean` | When true, filled slots show green (correct) or red (wrong) — another kind of hint (default true). From exercise `exerciseParams.letterColorHint`. |

- **Letter range per difficulty:**

  | Difficulty | Letters |
  |------------|---------|
  | ULTRA_EASY | 2–3 |
  | EASY | 3–4 |
  | MEDIUM | 4–5 |
  | HARD | 6–7 |
  | VERY_HARD | 8+ |

- **Subject**: `WORD`.
- **Access**: [Exercises in WORD](http://localhost:5173/api/subjects/WORD/exercises).
- **Details**: See [exercise-anagram.md](exercise-anagram.md).

---

### 1c. Multiplication (FLASHCARD_QA, operation: MULTIPLY)

- **Display name:** Multiplication (API returns `mathOperation: "MULTIPLY"`).
- **Params:** `operation: MULTIPLY`; optional `firstValues` (e.g. `[2, 5, 10]`) for fixed multipliers; else `firstMin`/`firstMax`, `secondMin`/`secondMax`. Difficulty progression (own logic, not digit-based like Sum):
  - **ULTRA_EASY:** 2, 5, or 10 × single digit (e.g. 2×7, 10×4) — `firstValues: [2,5,10]`, `secondMax: 9`.
  - **EASY:** Times tables — 1–9 × 1–12 (e.g. 7×8).
  - **MEDIUM:** Two-digit × one-digit (e.g. 24×7).
  - **HARD:** Two-digit × two-digit (e.g. 24×17).
- **Access (no ID):** [All exercises](http://localhost:5173/api/exercises) or default subject; session/journey.

---

### 1d. Division (FLASHCARD_QA, operation: DIVIDE)

- **Display name:** Division (API returns `mathOperation: "DIVIDE"`). All divisions are clean (dividend = divisor × quotient).
- **Params:** `operation: DIVIDE`; optional `secondValues` (e.g. `[2, 5, 10]`) for fixed divisors; else `firstMin`/`firstMax` (quotient range), `secondMin`/`secondMax` (divisor range). Difficulty progression:
  - **ULTRA_EASY:** ÷ 2, 5, or 10 with quotient 1–9 (e.g. 20÷2, 45÷5).
  - **EASY:** Divisor 2–9, quotient 1–12 (times-table divisions).
  - **MEDIUM:** Divisor 1–9, quotient 1–99 (e.g. 432÷6).
  - **HARD:** Two-digit ÷ two-digit (quotient and divisor 10–99).
- **Access (no ID):** Same as Multiplication.

---

### 9. SUM_PAIR

- **Params** (response as `sumPairParams`); **generated at response time:** `sumPairRounds`.

  | Field | Type | Description |
  |-------|------|-------------|
  | `staticNumbers` | `List<Int>` | One or more static values K; valid pair (a,b) iff a + K = b. Or use `staticCount`+`staticMin`+`staticMax` for random statics per play. |
  | `pairsPerRound` | `Int` | Number of sum pairs per round (2 × pairsPerRound cards per round). ≥ 2. |
  | `minValue` | `Int` | Optional min for generated numbers (default 1). |
  | `maxValue` | `Int` | Optional max for generated numbers (default 99). |
  | `minDigits` | `Int?` | Optional: min digits for displayed numbers (1–9). When set with `maxDigits`, overrides min/max value (e.g. 1–2 → 1–99). |
  | `maxDigits` | `Int?` | Optional: max digits for displayed numbers (1–9). |

  **Response-only (generated):** `sumPairRounds` / `sumPairGroups` + `sumPairDeck` — groups by static with colors; deck is shuffled cards.

- **Access (no ID):** [All exercises](http://localhost:5173/api/exercises) or [MEMORY subject](http://localhost:5173/api/subjects/MEMORY/exercises); or [session prefer SUM_PAIR](http://localhost:5173/api/session/start?preferType=SUM_PAIR).
- **Details:** See [exercise-sum-pair.md](exercise-sum-pair.md).

---

### 10. IMAGE_PAIR

- **Params** (response as `imagePairParams`); **generated at response time:** `imagePairDeck`.

  | Field | Type | Description |
  |-------|------|-------------|
  | `pairCount` | `Int` | Number of pairs (total cards = 2 × pairCount). ≥ 2. |
  | `maxPairsPerBackground` | `Int` | At most this many pairs share the same background (duplicate cap). Default 2. |
  | `colorCount` | `Int` | 0 = no background color; 1 = no color + 1 color (2 backgrounds); etc. Number of background types = colorCount + 1. |

  **Matching rule:** Two cards match iff they have the **same background and the same image**. Only animals (images) with the same background can match.

  **Response-only (generated):** `imagePairDeck`: `List<{ backgroundId, imageId, backgroundColorHex? }>` — shuffled deck.

- **Access (no ID):** [All exercises](http://localhost:5173/api/exercises) or [MEMORY subject](http://localhost:5173/api/subjects/MEMORY/exercises).
- **Details:** See [exercise-pair-architecture.md](exercise-pair-architecture.md).

---

## Quick reference: URLs (base `http://localhost:5173`)

| What | URL |
|------|-----|
| **All subjects** | [http://localhost:5173/api/subjects](http://localhost:5173/api/subjects) |
| **All exercises** | [http://localhost:5173/api/exercises](http://localhost:5173/api/exercises) |
| N-Back 1, 2, 3 (card) | [api/nback/1](http://localhost:5173/api/nback/1) · [api/nback/2](http://localhost:5173/api/nback/2) · [api/nback/3](http://localhost:5173/api/nback/3) |
| N-Back Grid, Dual Grid, Dual Card | Via [api/exercises](http://localhost:5173/api/exercises) by type |
| Exercises in subject | [api/subjects/default/exercises](http://localhost:5173/api/subjects/default/exercises) · [api/subjects/B1/exercises](http://localhost:5173/api/subjects/B1/exercises) · [api/subjects/WORD/exercises](http://localhost:5173/api/subjects/WORD/exercises) · [api/subjects/WORDLE_FR/exercises](http://localhost:5173/api/subjects/WORDLE_FR/exercises) · [api/subjects/WORDLE_EN/exercises](http://localhost:5173/api/subjects/WORDLE_EN/exercises) · [api/subjects/ESTIMATION/exercises](http://localhost:5173/api/subjects/ESTIMATION/exercises) |
| Start session | [http://localhost:5173/api/session/start](http://localhost:5173/api/session/start) |
| Journey | [http://localhost:5173/api/journey?code=default](http://localhost:5173/api/journey?code=default) |
| Journey step content | [api/journey/steps/0/content?journeyCode=default](http://localhost:5173/api/journey/steps/0/content?journeyCode=default) (step 0, 1, 2, …) |
| Health | [http://localhost:5173/api/health](http://localhost:5173/api/health) |
| **If you have an ID** | `http://localhost:5173/api/exercises/{id}` |

---

---

## Estimation Ladder (`estimation`) — 30 levels

**Start:** `GET /api/session/start?mode=ladder&ladderCode=estimation`  
**Next:** `POST /api/session/ladder/next` (send `ladderState` + `lastScore` back each time)

### Level map

| Levels | Difficulties | Phase |
|--------|-------------|-------|
| 0–4 | ULTRA_EASY | Everyday warmup — days/year, Eiffel Tower, π×10, sound speed, hours/week |
| 5–6 | ULTRA_EASY + EASY | Bridge — mixed everyday + school |
| 7–11 | EASY | School knowledge — Everest, Paris→NYC, France population, 17×23, √200 |
| 12–13 | EASY + MEDIUM | Bridge — mixed school + cultural |
| 14–18 | MEDIUM | Cultural + harder math — Earth population, Earth→Moon, France area, e³, 2^10 |
| 19–20 | MEDIUM + HARD | Bridge — mixed cultural + expert |
| 21–24 | HARD | Expert knowledge — speed of light, Earth→Sun distance, 7^5 |
| 25–26 | HARD + VERY_HARD | Bridge — mixed expert + specialist |
| 27–29 | VERY_HARD | Specialist peak — seconds/year, age of universe, 2^20 |

**Thresholds:** advance ≥ 75%, stay ≥ 40%, demote < 40% (evaluated after every 5 answers).  
**Scoring on each answer:** `score = max(0, 1 − |ln(answer/correct)| / ln(toleranceFactor))`, scaled by speed bonus.

---

## 15. Estimation (ESTIMATION)

- **Subject code:** `ESTIMATION`
- **Goal:** Approximate a numerical answer as quickly and accurately as possible.
- **Categories:** `geography` (monuments, distances, areas, populations), `science` (physical constants, astronomical distances), `math` (mental arithmetic: π×N, √N, powers, factorials).
- **Scoring (logarithmic):** `score = max(0, 1 − |ln(answer / correctAnswer)| / ln(toleranceFactor))`
  - `toleranceFactor = 1.03–1.05` → very tight (exact math: ±3–5% = 0)
  - `toleranceFactor = 1.1–1.15` → tight (mental math: ±10–15% = 0)
  - `toleranceFactor = 1.3–1.5` → moderate (school knowledge)
  - `toleranceFactor = 2.0–10.0` → wide (orders-of-magnitude geography/science)
- **API params returned (`estimationParams`):**
  - `correctAnswer: Double` — the true value
  - `unit: String` — unit label (e.g. `"m"`, `"km"`, `"million people"`, `""`)
  - `toleranceFactor: Double` — controls score decay (see formula above)
  - `category: String` — `"geography"` | `"science"` | `"math"` | `"history"`
  - `hint: String?` — optional contextual hint (shown after a few seconds or on request)
- **Prompt:** stored in DB, returned as-is (no generation at response time).
- **Expected answers:** indicative string list (e.g. `["330"]`); actual grading uses the logarithmic formula.
- **Difficulty mapping:**

| Difficulty | Tolerance range | Examples |
|---|---|---|
| ULTRA_EASY | 1.03–1.5 | Days/year, Eiffel Tower height, π×10, hours/week |
| EASY | 1.1–2.0 | Everest height, Paris→NYC, √200, 17×23 |
| MEDIUM | 1.05–2.0 | Earth population, Earth→Moon distance, area of France, e³, 2^10 |
| HARD | 1.1–1.5 | Speed of light, Earth→Sun distance, 7^5 |
| VERY_HARD | 1.05–1.3 | Seconds/year, age of universe, 2^20 |

- **Access:** [api/subjects/ESTIMATION/exercises](http://localhost:5173/api/subjects/ESTIMATION/exercises)

---

*When adding or changing exercise types or params, update this index and any type-specific doc (e.g. `exercise-sum-pair.md`).*

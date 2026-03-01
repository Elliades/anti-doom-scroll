# Exercise: Sum Pair (Memory Card Game)

> **All exercises:** See [exercises-index.md](exercises-index.md) for types, params, and URLs.

## Overview

A **sum pair** exercise displays cards with **numbers**. A **static number** \( K \) is shown. The user must find pairs of cards \( (a, b) \) such that:

\[
a + K = b
\]

So the "second" card must equal the "first" card plus the static value. Cards are shown face-down (or face-up with numbers); the user selects two cards; if they form a valid sum pair, they are removed (or marked solved). The game ends when all pairs for the current static are found.

**Harder mode**: Multiple static numbers \( K_1, K_2, \ldots, K_m \). Only **one static is displayed at a time**. The user solves all pairs for \( K_1 \); then the game switches to \( K_2 \) with a **new board**. This continues until all statics are done. A critical constraint ensures the game stays solvable across rounds (see § Disjoint number pools).

---

## Definitions

- **Static number** \( K \): A positive integer shown to the user. Defines the rule: valid pair \( (a, b) \) iff \( a + K = b \) (with \( a < b \) by convention).
- **Sum pair**: An unordered pair of distinct numbers \( \{a, b\} \) with \( a + K = b \). We store it as \( (a, b) \) with \( a < b \), so \( b = a + K \).
- **Round / generation**: For a single static \( K \), one "round" is the board of cards for that \( K \). When multiple statics exist, each static has its own round; the user completes round 1 (all pairs for \( K_1 \)), then round 2 (all pairs for \( K_2 \)), etc.
- **Pool (number pool)**: The set of numbers that appear on the cards in a given round. All cards in that round show exactly the numbers in the pool; each number appears on exactly one card.

---

## Mechanics

### Single static (easy)

- One static \( K \), one round.
- **Generation**: Choose \( n \) sum pairs \( (a_1, a_1+K), (a_2, a_2+K), \ldots, (a_n, a_n+K) \) such that all \( 2n \) numbers are **distinct**.
- **Board**: \( 2n \) cards showing these numbers, shuffled (positions random, values fixed).
- **Play**: User selects two cards. If the two numbers form a sum pair for \( K \) (i.e. smaller + \( K \) = larger), the pair is removed (or marked). When all \( n \) pairs are removed, the exercise is complete.

### Multiple statics (harder)

- Statics \( K_1, K_2, \ldots, K_m \) (\( m \ge 2 \)).
- **Display**: Only the current static \( K_i \) is shown. The board shows only the cards for the current round \( i \).
- **Flow**: User solves all pairs for \( K_1 \). When the board for \( K_1 \) is empty (all pairs found), the game shows \( K_2 \) and a **new** board with numbers for \( K_2 \) only. Repeat until all \( K_m \) rounds are done.
- **Critical constraint — disjoint number pools**:  
  For every two different rounds \( i \ne j \), the **number pool of round \( i \) must not intersect the number pool of round \( j \)**.  
  So: no number that appeared on a card in round \( i \) may appear on any card in round \( j \).  
  **Why**: If round 1 uses numbers \( \{2, 3, 4, 5\} \) (e.g. pairs (2,3) and (4,5) for \( K_1 = 1 \)) and round 2 uses static \( K_2 = 2 \) and reuses some of those numbers, then a combination valid in round 1 could also be valid in round 2 (e.g. \( 2 + 2 = 4 \)). The user could then match (2,4) in round 2, leaving 3 and 5 unmatched and making the round unsolvable, or get confused about which static applies. By keeping pools disjoint, no number from a previous round can form a valid sum pair in a later round; each round uses only its own numbers and its own static.

### Order of selection

- User clicks card A then card B. The pair is valid iff \( \min(A, B) + K = \max(A, B) \) (so the smaller value is the "first" in the sum-pair relation).

---

## Generation rules (formal)

### Single static \( K \), \( n \) pairs

1. Choose \( n \) distinct values \( a_1, \ldots, a_n \) such that:
   - For all \( i \), \( a_i \ge 1 \) (or a configured minimum).
   - For all \( i \ne j \): \( a_i \ne a_j \), \( a_i \ne a_j + K \), and \( a_i + K \ne a_j \) (so all \( 2n \) numbers \( a_i, a_i + K \) are distinct).
2. Pairs: \( (a_1, a_1+K), \ldots, (a_n, a_n+K) \).
3. Card values (shuffled): \( [a_1, a_1+K, a_2, a_2+K, \ldots, a_n, a_n+K] \).

### Multiple statics \( K_1, \ldots, K_m \), \( n \) pairs per round

1. **Allocate disjoint pools**.  
   For each round \( i \in \{1,\ldots,m\} \), assign an interval \( [L_i, R_i] \) such that \( L_i, R_i \) are integers and the intervals do not overlap. Example: \( [1, 50], [51, 100], [101, 150] \) for \( m = 3 \). Size \( R_i - L_i + 1 \ge 2n \) and large enough that we can place \( n \) pairs \( (a, a+K_i) \) inside \( [L_i, R_i] \).

2. **Per round \( i \)**:
   - Static \( K_i \).
   - Choose \( n \) values \( a_1, \ldots, a_n \) in \( [L_i, R_i] \) such that:
     - For all \( j \), \( a_j \) and \( a_j + K_i \) are in \( [L_i, R_i] \).
     - All \( 2n \) numbers \( a_1, a_1+K_i, \ldots, a_n, a_n+K_i \) are distinct.
   - Pairs for round \( i \): \( (a_1, a_1+K_i), \ldots, (a_n, a_n+K_i) \).
   - Card values for round \( i \) (shuffled): only these \( 2n \) numbers.

3. **Disjointness**: By construction, round \( i \) uses only numbers in \( [L_i, R_i] \), and intervals do not overlap, so no number from round \( i \) appears in round \( j \ne i \). So no combination from a previous round can be a valid sum pair for a later static.

---

## Exercise parameters (API / persistence)

Stored in `exercise_params` (e.g. JSON) for type `SUM_PAIR`:

| Field | Type | Description |
|-------|------|--------------|
| `staticNumbers` | `List<Int>` | One or more static values \( K_1, \ldots, K_m \). Length 1 = single round; length > 1 = multi-round. |
| `staticCount` | `Int` | (Alternative to staticNumbers) Number of statics to generate randomly per play. |
| `staticMin` | `Int` | With staticCount: minimum value for each generated static. |
| `staticMax` | `Int` | With staticCount: maximum value for each generated static. Generated values are distinct and **sorted ascending**. |
| `pairsPerRound` | `Int` | Number of sum pairs per round (\( n \)). Total cards per round = \( 2n \). |
| `minValue` | `Int?` | Optional minimum for the smallest number in a pair (default e.g. 1). |
| `maxValue` | `Int?` | Optional maximum for the largest number in a pair (for single round; for multi-round, backend uses disjoint ranges). |
| `minDigits` | `Int?` | Optional (1–9): when set with `maxDigits`, defines digit range for displayed numbers; backend derives minValue = 10^(minDigits-1), maxValue = 10^maxDigits - 1. |
| `maxDigits` | `Int?` | Optional (1–9): used with `minDigits` to constrain displayed numbers to that many digits. |

**Generated at runtime (or at exercise creation)** and sent to the client:

- **Single round**: `rounds: [{ static: K, cards: [number, number, ...] }]` — one element, `cards` length \( 2n \), shuffled.
- **Multi-round**: `rounds: [{ static: K1, cards: [...] }, { static: K2, cards: [...] }, ...]` — each `cards` is the shuffled list of \( 2n \) numbers for that round; pools disjoint.

So the backend (or a generator) produces the actual card values; the frontend only displays the current static and current round’s cards and checks pairs against the current static.

---

## Scoring (MVP)

- **Success**: All pairs found for all rounds. Call `onComplete(1)` (or a score derived from moves/time).
- **Moves**: Count of "two cards selected" actions. Optional: score = f(moves) e.g. \( \max(0, 1 - (moves - n) \cdot 0.05) \) per round or overall.

---

## Edge cases

- **No duplicate cards in a round**: Each number appears at most once in the current round’s card list.
- **Clear round transition**: When the last pair of round \( i \) is found, immediately show the next static and the next round’s board (or a short "Round 2" message then the board).
- **Backend generation**: Either (a) generate on each GET exercise (different game each time) or (b) store a seed and generate deterministically. For MVP, (a) is acceptable.

---

## Summary

| Aspect | Rule |
|--------|------|
| Valid pair | \( (a, b) \) with \( a + K = b \), \( a < b \). |
| Single static | One \( K \), one board of \( 2n \) cards. |
| Multiple statics | One static and one board at a time; after all pairs for \( K_i \) are found, switch to \( K_{i+1} \) and a new board. |
| Disjoint pools | Numbers used in round \( i \) must not appear in round \( j \ne i \); ensures no cross-round valid pair and prevents getting stuck. |
| Generation | Per round: pick \( n \) pairs \( (a, a+K) \) with all \( 2n \) values distinct; for multi-round use disjoint ranges so pools stay disjoint. |

---

## Difficulty scaling

| Difficulty   | Suggested config | Rationale |
|-------------|------------------|-----------|
| **ULTRA_EASY** | 1 static, 3 pairs, small range (e.g. 1–30) | Quick game, few cards (6), low cognitive load. |
| **EASY**      | 1 static, 4 pairs (8 cards) | One round, more pairs. |
| **MEDIUM**    | 2 statics, 3 pairs each | Two rounds; user must complete round 1 then round 2 (disjoint pools). |
| **HARD**      | 3+ statics, 4+ pairs per round | More rounds and/or more cards per round; ensure `minValue`/`maxValue` and `rangeSize >= maxStatic + pairsPerRound` for multi-round. |

When adding exercises, keep **multi-round constraint**: for `staticNumbers.size > 1`, the generator splits `[minValue, maxValue]` into disjoint ranges. Each range must fit `pairsPerRound` pairs for that round’s static, so `rangeSize >= max(staticNumbers) + pairsPerRound`. Use a wide enough range (e.g. 1–99) and avoid very large statics when you have many rounds.

---

## Implementation plan

### Backend (Kotlin)

1. **Domain**
   - Add `ExerciseType.SUM_PAIR`.
   - Add `SumPairParams`: `staticNumbers: List<Int>`, `pairsPerRound: Int`, optional `minValue`/`maxValue`.
   - Add `Exercise.memoryCardParams()`-style parser `sumPairParams()` (or use a single param blob; generation can be server-side when exercise is fetched).

2. **Generation**
   - Service or use-case: given `SumPairParams`, produce a list of **rounds**. Each round: `static: Int`, `cards: List<Int>` (length `2 * pairsPerRound`), shuffled. For single static, one round. For multiple statics, disjoint ranges per round (e.g. round 1 → [1, 50], round 2 → [51, 100], …), generate pairs inside each range so all card values in that round are distinct and form valid sum pairs.

3. **API**
   - No new endpoint: `GET /api/exercises/{id}` returns exercise with type `SUM_PAIR` and a payload (e.g. `sumPairRounds` or embed in `exerciseParams`). Either:
     - **Option A**: Store only params in DB; backend **generates** rounds on each GET (new game each time), or  
     - **Option B**: Store pre-generated rounds in `exercise_params` for reproducibility.  
   - Prefer **Option A** for variety; DTO includes `sumPairRounds: List<{ static: Int, cards: List<Int> }>` generated at response time.

4. **DTO**
   - `SumPairParamsDto`: same as domain (staticNumbers, pairsPerRound, minValue?, maxValue?).  
   - `SumPairRoundDto`: `static: Int`, `cards: List<Int>`.  
   - `ExerciseDto`: add `sumPairParams: SumPairParamsDto?`, `sumPairRounds: List<SumPairRoundDto>?` (generated when type is SUM_PAIR).

5. **Tests**
   - Unit: generator produces correct number of rounds; each round has `2 * pairsPerRound` distinct cards; each round’s cards form valid sum pairs for that round’s static; multi-round: no number in round i appears in round j.
   - Integration: GET exercise by id with type SUM_PAIR returns valid sumPairRounds.

### Frontend (React/TypeScript)

1. **Types**
   - `SumPairParamsDto`, `SumPairRoundDto`; extend `ExerciseDto` with `sumPairParams?`, `sumPairRounds?`.

2. **Component**
   - `SumPairExercise.tsx`: intro (prompt, Start); playing: show current static K and grid of cards (numbers); two cards selected → if valid sum pair (smaller + K = larger), mark matched and remove; when all pairs of current round matched, if more rounds, show “Next round” and next static + new board; when all rounds done, call `onComplete(score)`.

3. **Registry**
   - Register `SUM_PAIR` in `ExercisePlayer` type registry.

4. **Styling**
   - Reuse or extend memory-card styles (grid, cards); show static number prominently.

### Data

- Migration: add SUM_PAIR exercises to Memory subject (or a new “Math” subject): e.g. one with `staticNumbers: [5]`, `pairsPerRound: 4` (easy); one with `staticNumbers: [3, 7]`, `pairsPerRound: 3` (harder, two rounds).

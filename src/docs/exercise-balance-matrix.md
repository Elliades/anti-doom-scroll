# Exercise Balance Matrix

Goal: compare difficulty across implemented exercises and detect imbalance between `exercise type x difficulty`.

Scope: includes all implemented types except `CLOZE`, `QCM`, `KEYWORD_BLURTING` (currently not fully implemented as dedicated playable flows).

## 1) Static design matrix (what we ship)

Use this matrix to reason about expected difficulty before telemetry.

| Exercise type | Typical difficulty bands | Main characteristics | Example(s) | Current scoring hardness |
|---|---|---|---|---|
| `FLASHCARD_QA` (ADD/SUB/MUL/DIV) | `ULTRA_EASY` -> `HARD` | Arithmetic prompt + text answer; math generator targets complexity bands (`mathComplexityScore`) | `24 + 37`, `432 / 6`, `24 x 17` | Hard binary (`1` if correct, else `0`) |
| `MATH_CHAIN` | usually `ULTRA_EASY` -> `HARD` by seed | Sequential mental operations; hidden intermediate states; final typed answer | Start `8`, then `+4`, `x3`, `-5` | Hard binary (`1`/`0`), with subscore showing chain complexity |
| `N_BACK` | `n=1` -> `n=3` | Working memory over temporal sequence; match current with `N` steps back | Card sequence with match taps | Medium-hard (hits + false alarm penalty) |
| `N_BACK_GRID` | low `n` -> higher `n` | Spatial working memory (3x3 position sequence) | Highlighted cell sequence | Medium-hard (hits + false alarm penalty) |
| `DUAL_NBACK_GRID` | base dual -> harder overlaps | Two simultaneous channels (position + color), two response buttons | Match Position / Match Color | Hard (dual targets + false alarm penalty) |
| `DUAL_NBACK_CARD` | base dual -> harder overlaps | Two channels on cards (suit + rank), two response buttons | Match Color / Match Number | Hard (dual targets + false alarm penalty) |
| `MEMORY_CARD_PAIRS` | small board -> larger board | Flip 2 cards, find identical symbols, move efficiency matters | 3 pairs (6 cards) to larger decks | Medium (score degrades with extra moves) |
| `SUM_PAIR` | 1 static few pairs -> multi-static/more pairs | Match numbers by rule `min(a,b)+K=max(a,b)`; can include several statics/groups | `+5` group with shuffled cards | Medium-hard (move-based; extra cognitive rule load) |
| `IMAGE_PAIR` | fewer pairs/bg -> more pair/bg combos | Match requires both same image and same background | Same animal + same color background | Medium (move-based; conjunctive matching) |
| `ANAGRAM` | letters: `2-3` -> `8+` | Scrambled letters, hint timer, wrong placement pressure | `RTA -> ART`, longer words at high level | Medium (penalty by hints + wrong placements, floor at 0.3) |
| `WORDLE` | letters: `3` -> `7` | Classic guess-feedback loop, dictionary validation, limited attempts | 6 max attempts by default | Medium (attempt-based with score floor on win/loss) |
| `DIGIT_SPAN` | start short -> longer + transformations | Memorize digits, then challenge transforms (ascending, parity split, etc.) | span 3 then + challenge | Medium-hard (progressive span/challenge success) |
| `ESTIMATION` | broad tolerance -> strict tolerance | Numeric estimate scored by precision + response time | Eiffel tower height, `sqrt(200)`, Earth-Sun distance | Variable/continuous (weighted precision-time model) |

## 2) Per-type difficulty levers (what to tune first)

Use these as primary tuning knobs when one type is too easy/hard for its label.

- `FLASHCARD_QA` / `MATH_CHAIN`
  - Operand ranges, number of operations, operation mix, target `mathComplexityScore`.
- `N_BACK` family
  - `n`, sequence length, target density, stimulus speed.
- `MEMORY_CARD_PAIRS`
  - `pairCount` (board size), symbol similarity.
- `SUM_PAIR`
  - `pairsPerRound`, number of statics, value/digit range, static magnitude.
- `IMAGE_PAIR`
  - `pairCount`, `colorCount`, `maxPairsPerBackground` (distractor density).
- `ANAGRAM`
  - Letter count, `hintIntervalSeconds`, letter-color hint behavior.
- `WORDLE`
  - `wordLength`, `maxAttempts`, dictionary strictness.
- `DIGIT_SPAN`
  - `startLength`, growth pace, challenge mode distribution.
- `ESTIMATION`
  - `toleranceFactor`, prompt domain mix, `timeWeightHigher`.

## 3) Common difficulty index (cross-exercise normalization)

This is the shared scale to compare very different exercises.

### 3.1 Rubric per attempt bucket

Rate each `(exercise type, difficulty)` cell on:

- `cognitive_load` (1..5): rule + abstraction complexity.
- `working_memory` (1..5): memory burden over time.
- `time_pressure` (1..5): urgency/speed demand.
- `error_penalty` (1..5): score drop severity after mistakes.
- `ambiguity` (1..5): uncertainty/randomness/noise in solving path.

### 3.2 Static index

`static_index = 0.30*cognitive_load + 0.25*working_memory + 0.20*time_pressure + 0.20*error_penalty + 0.05*ambiguity`

Normalize to 0..100:

`static_index_100 = ((static_index - 1) / 4) * 100`

### 3.3 Telemetry-adjusted index

Add production behavior from attempts:

- `completion_rate` (0..1)
- `median_score` (0..1)
- `median_reaction_ms` (ms)
- `quit_rate` (0..1) if available from session events

Suggested adjustment:

`balance_index = 0.55*static_index_100 + 0.20*(100*(1-completion_rate)) + 0.15*(100*(1-median_score)) + 0.10*time_pressure_proxy`

Where `time_pressure_proxy` can be percentile rank of median reaction time within the same mode (session/ladder), mapped to 0..100.

## 4) Balance scorecard (what to measure weekly)

Minimum columns:

- `exercise_type`
- `difficulty`
- `attempts`
- `completion_rate`
- `median_score`
- `p50_reaction_ms`
- `p90_reaction_ms`
- `balance_index`

Recommended SQL starter (PostgreSQL):

```sql
SELECT
  e.type AS exercise_type,
  e.difficulty,
  COUNT(*) AS attempts,
  AVG(CASE WHEN a.correct THEN 1.0 ELSE 0.0 END) AS completion_rate,
  PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY a.score) AS median_score,
  PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY a.reaction_time_ms) AS p50_reaction_ms,
  PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY a.reaction_time_ms) AS p90_reaction_ms
FROM attempt a
JOIN exercise e ON e.id = a.exercise_id
GROUP BY e.type, e.difficulty
ORDER BY e.type, e.difficulty;
```

Notes:

- `attempt.correct` is less useful for partial-scored games (`ESTIMATION`, memory move-based) than `attempt.score`; use both.
- For N-back and dual N-back, retain subscore signals (false alarms, misses) in analytics if available.

## 5) Imbalance detection rules (automatic flags)

Flag a cell as suspicious when one of these is true:

- **Adjacent jump too steep:** within same type, completion drops by more than 20 percentage points between two adjacent difficulties.
- **Same-label mismatch across types:** for the same difficulty label, median score differs by more than 0.20 between two core types with similar target audience.
- **Time-pressure spike:** `p90_reaction_ms` jumps > 40% vs previous difficulty while score also drops.
- **Penalty cliff:** median score drops > 0.25 with little change in completion (often means scoring is too harsh, not task too hard).

## 6) Calibration loop (safe balancing workflow)

1. Pick one flagged `(type, difficulty)` cell.
2. Change one lever only (example: `ANAGRAM` hint interval from 10s to 8s).
3. Run A/B or staged rollout (if possible) for enough attempts.
4. Recompute scorecard and compare:
   - completion delta
   - median score delta
   - reaction-time delta
   - spillover on neighboring difficulties
5. Keep/revert; then move to next flagged cell.

Avoid changing multiple exercise families at once; it makes attribution impossible.

## 7) Practical target bands (initial proposal)

For healthy progression, aim per difficulty band:

- `ULTRA_EASY`: completion `>= 0.85`, median score `>= 0.75`
- `EASY`: completion `0.70-0.85`, median score `0.60-0.78`
- `MEDIUM`: completion `0.55-0.75`, median score `0.45-0.70`
- `HARD`: completion `0.40-0.65`, median score `0.30-0.60`
- `VERY_HARD`: completion `0.25-0.50`, median score `0.20-0.45`

If a cell sits far outside the band, tune with section 2 levers first.

## 8) Next implementation step

Create a weekly generated artifact (CSV or markdown) from `attempt` + `exercise` and append:

- computed `static_index_100`
- computed `balance_index`
- imbalance flags

This document is the reference for scoring/balance decisions across all implemented exercise types.

## 9) Score-driven params generation (frontend-first)

The frontend now supports a unified generation contract:

- `generateParamsFromScore(targetScore: number): ExerciseParams`

Current implementation details:

- A central registry lives in `frontend/src/api/exerciseParamGenerators.ts`.
- `offlineLadderEngine` computes a `targetScore` from level difficulty bands, then blends it with recent user scores to avoid abrupt jumps.
- Synthetic pool builders consume that target score when generating:
  - `SUM_PAIR` (pilot implementation with calibrated candidates),
  - `MEMORY_CARD_PAIRS`,
  - `FLASHCARD_QA`.

Calibration policy:

- `targetScore` is clamped to `0..1`.
- Generation aims for `abs(estimatedScore - targetScore) <= 0.05` when discrete parameter combinations allow it.
- For exercises with coarse discrete knobs, the closest calibrated candidate is selected.

Migration path:

1. Keep backend thresholds and ladder rules unchanged.
2. Move each synthetic generator to score-driven mapping.
3. Extend beyond synthetic exercises as new per-type parameter mappings are provided.

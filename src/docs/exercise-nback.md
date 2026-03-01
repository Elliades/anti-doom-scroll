# Exercise: N-Back (Working Memory)

> **All exercises:** See [exercises-index.md](exercises-index.md) for types, params, and URLs.  
> **Architecture (Card, Grid, Dual variants):** See [exercise-nback-architecture.md](exercise-nback-architecture.md).

## Overview

The **n-back task** is a continuous performance task used to measure working memory and working memory capacity ([Kirchner, 1958](https://en.wikipedia.org/wiki/N-back)). The subject is presented with a sequence of stimuli and must indicate when the **current stimulus matches** the one from **n steps earlier** in the sequence. The load factor \( n \) (1-back, 2-back, 3-back) controls difficulty.

**Task**: Tap **Match** when the current item equals the item **n positions back**.

## Variants

| Variant | Stimulus | Match target | Documented in |
|---------|----------|--------------|----------------|
| **Card N-Back** | Playing cards | Card identity | This doc |
| **Grid N-Back** | 3×3 grid position | Same cell position | [exercise-nback-architecture.md](exercise-nback-architecture.md) |
| **Grid Dual N-Back** | 3×3 grid + color | Position or color | [exercise-nback-architecture.md](exercise-nback-architecture.md) |
| **Card Dual N-Back** | Playing cards | Suit or rank | [exercise-nback-architecture.md](exercise-nback-architecture.md) |

- **1-back**: Match when current = previous item.
- **2-back**: Match when current = item 2 steps back.
- **3-back**: Match when current = item 3 steps back.

## Visual vs. Auditory N-Back

- **Auditory**: Letters read aloud — user matches letter identity (e.g. T, L, H, **C**, H, O, **C** …).
- **Visual (this app)**: Items displayed one at a time — user matches item identity. We use **playing cards** instead of letters. One card appears per turn; user taps Match when it equals the card **n** positions back.

## Stimuli: Playing Cards

We use [CSS Playing Cards](https://selfthinker.github.io/CSS-Playing-Cards/) for visual display.

- **Card codes**: `rank` + `suit` (e.g. `AC` = Ace of Clubs, `2D` = 2 Diamonds, `QH` = Queen Hearts, `KS` = King Spades).
- **Sequence**: List of card codes, e.g. `["AC","2D","QH","AC","3S",…]`. Match at index 3 because `AC` == `AC` (1-back).
- **Cards cycle one way**: One card at a time, left-to-right (or top-to-bottom) through the sequence. No backtracking.

## Definitions

- **n**: Match current item with item **n** positions back (\( n \ge 1 \)).
- **Sequence**: Stimuli to display one-by-one (card codes).
- **Match indices**: 0-based positions where `sequence[i] == sequence[i - n]` (user must tap Match at these indices).
- **Length**: `sequence.size >= n + 2` (enough items for at least one valid match).

## Mechanics

1. **Intro**: Show instruction and n value. User taps Start.
2. **Play**: Cards appear one at a time at fixed intervals (e.g. 2.5s). User taps **Match** when the current card equals the one **n** steps back.
3. **Done**: Show score (hits vs. targets, false alarms).

### Scoring (MVP)

- **Hit**: User tapped Match at a correct match index.
- **Miss**: Match index where user did not tap.
- **False alarm**: User tapped Match at a non-match index.
- **Normalized score**: `hitBonus - falseAlarmPenalty`, clamped to [0, 1].

## API Parameters

| Field          | Type           | Description                                          |
|----------------|----------------|------------------------------------------------------|
| `n`            | `Int`          | 1, 2, or 3 (1-back, 2-back, 3-back).                 |
| `sequence`     | `List<String>` | Card codes, e.g. `["AC","2D","QH","AC","3S",…]`.     |
| `matchIndices` | `List<Int>`    | 0-based positions where user must tap Match.        |

## Access

- **By level**: [1-back](http://localhost:5174/api/nback/1), [2-back](http://localhost:5174/api/nback/2), [3-back](http://localhost:5174/api/nback/3)
- **By ID**: `GET /api/exercises/{id}` when `type == N_BACK`
- **Journey/Session**: N-Back exercises appear in OPEN_APP and CHAPTER_EXERCISES (subject B1)

## Card Code Format

| Suit   | Code | CSS class |
|--------|------|-----------|
| Clubs  | C    | clubs     |
| Diamonds | D  | diams     |
| Hearts | H    | hearts    |
| Spades | S    | spades    |

**Ranks**: 2–10, J, Q, K, A → `rank-2`, `rank-10`, `rank-j`, `rank-q`, `rank-k`, `rank-a`.

Example: `AC` → `<span class="card rank-a clubs">`, `2D` → `<span class="card rank-2 diams">`.

---

## Grid N-Back and Dual N-Back

For **Grid N-Back**, **Grid Dual N-Back**, and **Card Dual N-Back**, see [exercise-nback-architecture.md](exercise-nback-architecture.md). Those variants use:

- **Grid N-Back**: 3×3 grid; one cell highlighted per stimulus; match when the same position appears N steps back.
- **Grid Dual N-Back**: Same grid with cells colored temporarily (up to 4 colors); user taps "Match Position" or "Match Color".
- **Card Dual N-Back**: Cards shown one-by-one; user taps "Match Color" (suit) or "Match Number" (rank) when that attribute matches N steps back.

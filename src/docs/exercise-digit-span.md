# Digit Span Exercise

## Overview

A progressive working memory exercise where users memorize and recall sequences of digits, with increasing difficulty and challenge modes.

## Game Flow

1. **Display phase**: X digits shown for a configurable time (default 3s), with a blinking animation on the last second
2. **Hide phase**: Digits fade out (600ms transition)
3. **Forward recall**: User types the digits back in order
4. **Challenge mode** (on success): User must recall the digits in a transformed order:
   - **Ascending**: smallest to largest
   - **Descending**: largest to smallest
   - **Even/Odd**: even numbers first, then odd
   - **Odd/Even**: odd numbers first, then even
   - **Every other**: 1st, 3rd, 5th... digits only
5. **Progression**: On challenge success, add one more digit and repeat from step 1
6. **Termination**: On any failure, exercise ends

## Scoring

- `score = (maxSpan - startLength) / (maxLength - startLength)`, clamped to [0, 1]
- Subscores: Max span reached, total rounds played, challenges passed

## Backend

### Exercise params (`exerciseParams` JSONB)

| Key            | Type | Default | Description                      |
|----------------|------|---------|----------------------------------|
| `startLength`  | int  | 3       | Initial digit count (2–15)       |
| `displayTimeMs`| int  | 3000    | How long digits are shown (ms)   |
| `maxLength`    | int  | 15      | Upper cap for digit count        |

### Domain

- `ExerciseType.DIGIT_SPAN`
- `DigitSpanParams` data class with validation
- `Exercise.digitSpanParams()` accessor

### DTO

- `DigitSpanParamsDto(startLength, displayTimeMs, maxLength)`
- Mapped in `ExerciseDtoMapper.toExerciseDto()`

### Seeded exercises

| Difficulty  | Start length | Display time | ID prefix      |
|-------------|-------------|--------------|----------------|
| ULTRA_EASY  | 3           | 3000ms       | `f2000000-…01` |
| EASY        | 4           | 3000ms       | `f2000000-…02` |
| MEDIUM      | 5           | 2500ms       | `f2000000-…03` |
| HARD        | 6           | 2000ms       | `f2000000-…04` |
| VERY_HARD   | 7           | 1500ms       | `f2000000-…05` |

### Subject

- Code: `DIGIT_SPAN`
- ID: `b0000000-0000-0000-0000-000000000014`

## Frontend

### Component

`frontend/src/components/exercises/DigitSpanExercise.tsx`

### Input parsing

Users can enter digits in any format:
- `"472"` → [4, 7, 2]
- `"4 7 2"` → [4, 7, 2]
- `"4,7,2"` → [4, 7, 2]

### Phases

`intro → showing → hiding → forward_input → forward_correct → challenge_prompt → challenge_input → challenge_correct → (next round or done)`

On failure at any input phase → `round_fail → done`

### Registration

- `ExercisePlayer.tsx`: `DIGIT_SPAN: DigitSpanExercise`
- `exerciseLabels.ts`: `DIGIT_SPAN: 'Digit Span'`

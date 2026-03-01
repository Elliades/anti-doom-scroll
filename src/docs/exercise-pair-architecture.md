# Exercise pair architecture – analysis and plan

## Current architecture

### SUM_PAIR flow
- **DB**: `exercise_params`: `staticNumbers` (fixed) or `staticCount`+`staticMin`+`staticMax` (random), `pairsPerRound`, `minValue`, `maxValue`.
- **Resolver**: `SumPairParamsResolver` → `SumPairParams` (staticNumbers, pairsPerRound, minValue, maxValue).
- **Generator**: `SumPairGenerator.generateGroups()` → `SumPairResult` (groups with static+color+cards, flat deck).
- **Cache**: `SumPairRoundsCache` per exercise ID (session-stable).
- **DTO**: `SumPairParamsDto`, `SumPairGroupDto`, `SumPairCardDto`; mapper fills `sumPairParams`, `sumPairGroups`, `sumPairDeck`.

### MEMORY_CARD_PAIRS flow
- **DB**: `pairCount`, `symbols` (one per pair).
- **Resolver**: `Exercise.memoryCardParams()` → `MemoryCardParams`.
- **Generator**: `MemoryCardDeckCache` duplicates each symbol and shuffles.
- **DTO**: `memoryCardParams`, `shuffledDeck`.

### Shared pattern for “pair” exercises
1. Exercise type + `exercise_params` in DB.
2. Params resolved to domain object (Resolver or `Exercise.xxxParams()`).
3. Content generated at response time (optional cache by exercise ID).
4. DTO carries both config and generated content (deck/groups).

---

## User requirements (parsed)

### SUM_PAIR (sample exercises)
- **Number of pairs displayed**: `pairsPerRound` (already present).
- **Number of static numbers**: optional — either fixed `staticNumbers` or random `staticCount` (already present).
- **Range of number of digits for the displayed pair**: e.g. 1-digit (1–9), 2-digit (10–99). New params: `minDigits`, `maxDigits` (optional). When set, derive `minValue` = 10^(minDigits-1), `maxValue` = 10^maxDigits - 1. So displayed numbers stay within that digit range.

### New “image/animal pair” exercise (same architecture)
- **Number of pairs**: e.g. 4 pairs → 8 cards.
- **Amount of duplicates**: e.g. “at most 2 pairs of dogs with same background” → same background can appear on at most **maxPairsPerBackground** pairs (so we need multiple backgrounds when we have many pairs).
- **Number of colors**: **colorCount** 0..x. 0 = no background color; 1 = no background + 1 color (2 “backgrounds”); 2 = no background + 2 colors (3 backgrounds); etc. So number of background types = colorCount + 1.
- **Matching rule**: only cards with the **same background and same image** match. So each pair is (backgroundId, imageId). Two cards match iff (backgroundId, imageId) are equal.

---

## Implementation plan

### 1. SUM_PAIR digit range
- **Domain**: `SumPairParams`: add optional `minDigits: Int?`, `maxDigits: Int?`. When present, effective min/max for generation = derived from digits (minValue = 10^(minDigits-1), maxValue = 10^maxDigits - 1). Keep existing minValue/maxValue; if digits set, they override for the generator (or we derive in resolver and pass only min/max).
- **Resolver**: Read `minDigits`, `maxDigits` from `exercise_params`. If both set, compute `minValue` = 10^(minDigits-1), `maxValue` = 10^maxDigits - 1 (and pass to SumPairParams). Otherwise use existing minValue/maxValue.
- **Tests**: Resolver with minDigits/maxDigits; generator uses correct numeric range.
- **Seed/docs**: Optional: add one SUM_PAIR exercise that uses digit range in params.

### 2. IMAGE_PAIR exercise type
- **Domain**: New `ExerciseType.IMAGE_PAIR`. `ImagePairParams(pairCount, maxPairsPerBackground, colorCount)`. `ImagePairCard(backgroundId, imageId, backgroundColorHex?)`. `ImagePairResult(deck)`.
- **Generator**: Fixed pool of image codes (e.g. animal emojis). Backgrounds: 0 = “no color”, 1..colorCount = colors from palette. Generate `pairCount` pairs: each pair = (backgroundId, imageId). Constraint: each backgroundId used by at most `maxPairsPerBackground` pairs. Assign backgrounds to pairs (round-robin or random respecting cap), then assign images (distinct per pair), produce 2 cards per pair, shuffle deck.
- **Resolver**: `ImagePairParamsResolver` from `exercise_params`. **Cache**: `ImagePairDeckCache` by exercise ID. **Exercise.kt**: add `imagePairParams()`. **DTO**: `ImagePairParamsDto`, `ImagePairCardDto`, mapper and API.
- **Frontend**: New `ImagePairExercise` component: cards show image + background color; match when same background + same image.

### 3. Tests and verification
- SUM_PAIR: unit tests for digit range in resolver and generator range.
- IMAGE_PAIR: unit tests for generator (pair count, max duplicates per background, color count, valid matches only).
- Integration: mapper returns correct DTOs for both types.

---

## Summary

| Area              | Change |
|-------------------|--------|
| SUM_PAIR params   | Optional `minDigits`, `maxDigits`; resolver derives min/max value for generation. |
| IMAGE_PAIR        | New type, params (pairCount, maxPairsPerBackground, colorCount), generator (background+image), same pipeline as SUM_PAIR. |
| Matching (IMAGE)  | Same background + same image = one pair; generator ensures valid pairs only. |

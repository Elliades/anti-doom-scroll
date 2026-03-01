# Scoring Extensibility

## Analysis (Before Implementation)

**Problem:** The scoring system was not extensible. Each exercise computed a single `score` (0–1) and called `onComplete(score)`. Subscore details (e.g. "Hits: 5/6", "Moves: 12") were either:
- Hardcoded in each exercise’s own done view (and lost when replaced by the generic ScoreAnimation), or
- Not exposed at all.

**Goal:** Allow any exercise to customize scoring and provide subscore details that appear in the shared score animation.

---

## Design

### 1. `ExerciseResult` type (`frontend/src/types/exercise.ts`)

```typescript
interface SubscoreDetail {
  label: string   // e.g. "Hits", "Moves"
  value: string | number
}

interface ExerciseResult {
  score: number        // 0–1 (displayed as 0–100%)
  subscores?: SubscoreDetail[]
}
```

### 2. Backward compatibility

- `onComplete` accepts `ExerciseResult | number`; `toExerciseResult()` normalizes to `ExerciseResult`
- Exercises that only pass a number (e.g. GenericTextExercise) still work

### 3. Flow

1. Exercise finishes → calls `onComplete({ score, subscores })` or `onComplete(score)`
2. ExercisePlayer normalizes to `ExerciseResult`, captures `elapsedMs`
3. ScoreAnimation receives `score`, `elapsedMs`, `subscores` and shows them with the existing animation

---

## Adding Custom Scoring to a New Exercise

1. Compute the overall score (0–1)
2. Build a `subscores` array with labels and values
3. Call `onComplete({ score, subscores })`

Example:

```typescript
onComplete?.({
  score: 0.85,
  subscores: [
    { label: 'Hits', value: '8/10' },
    { label: 'Accuracy', value: '80%' },
  ],
})
```

---

## Current Exercise Subscore Details

| Exercise            | Subscores                                      |
|---------------------|-----------------------------------------------|
| N-Back              | Hits: X/Y, False alarms (if any)              |
| N-Back Grid         | Hits: X/Y, False alarms (if any)              |
| Dual N-Back Grid    | Hits, Position, Color                         |
| Dual N-Back Card    | Hits, Color, Number                           |
| Memory Card         | Moves, Perfect                                |
| Sum Pair            | Moves, Perfect                                |
| Generic Text        | (score only)                                  |

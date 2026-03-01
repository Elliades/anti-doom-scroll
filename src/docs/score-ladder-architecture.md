# Score Ladder Architecture

## Overview

The **Score Ladder** is a progression system applicable **only in Ladder Mode**. Players climb or descend levels based on performance. Each level contains exercises of predefined difficulties. Advancement/demotion is driven by configurable score thresholds evaluated over a sliding window of recent answers.

---

## Requirements Summary

| Requirement | Specification |
|-------------|---------------|
| **Mode** | Ladder mode only (not openapp, chapter, or standalone play) |
| **Stay in level** | Score ≥ 40% |
| **Advance** | Score ≥ 75% |
| **Demote** | Score < 40% (if a lower level exists) |
| **Evaluation window** | Configurable (e.g. last 5 answers) for advancement |
| **Level structure** | 1+ exercises per level, mix of predefined difficulties |
| **Scoring** | Current score (level) + Overall score (journey) |

---

## Domain Model

### LadderConfig

Configuration for a ladder (e.g. per subject or global). Loaded from `application.yml` or DB.

```kotlin
data class LadderConfig(
    val code: String,
    val name: String?,
    val levels: List<LadderLevel>,
    val thresholds: LadderThresholds
)

data class LadderLevel(
    val levelIndex: Int,
    val allowedDifficulties: List<Difficulty>,  // e.g. [ULTRA_EASY] or [ULTRA_EASY, EASY]
    val subjectCode: String? = null,            // if null, use default subject
    val exerciseIds: List<UUID>? = null        // if set, use these specific exercises only
)

data class LadderThresholds(
    val minScoreToStay: Double = 0.40,      // 40%: stay or demote
    val minScoreToAdvance: Double = 0.75,   // 75%: advance
    val answersNeededToAdvance: Int = 5    // evaluate over last N answers
)
```

### LadderState (Session State)

Mutable state carried by the client and sent back on each "next exercise" request. Server is stateless; client holds the truth for the session.

```kotlin
data class LadderState(
    val ladderCode: String,
    val currentLevelIndex: Int,
    val recentScores: List<Double>,     // last N scores (0-1)
    val overallScoreSum: Double,         // sum of all scores (for journey-wide average)
    val overallTotal: Int
) {
    fun currentLevelScorePercent(): Double? =  // null if no answers yet
        if (recentScores.isEmpty()) null else recentScores.average()
    
    fun overallScorePercent(): Double =
        if (overallTotal == 0) 0.0 else overallScoreSum / overallTotal
}
```

### Advancement Logic

Advancement is based **only on the last 5 answers at the current level**:

1. After each answer, append `score` to `recentScores` (keep last N, default 5).
2. When `recentScores.size >= answersNeededToAdvance` (5), compute average:
   - **Advance** if average ≥ `minScoreToAdvance` (75%) and not at max level.
   - **Demote** if average < `minScoreToStay` (40%) and `currentLevelIndex > 0`.
   - **Stay** otherwise (40% ≤ average < 75%).
3. **Reset on level change:** When advancing or demoting, `recentScores` is cleared. The next level starts with a fresh evaluation window of 5 answers.

### Score Display

When the user completes an exercise, the score is displayed prominently (ScoreAnimation). The user must click **Continue** to proceed to the next exercise, ensuring they see the result before continuing level by level.

---

## Level Definition Examples

| Level | allowedDifficulties | Description |
|-------|---------------------|-------------|
| 0 | `[ULTRA_EASY]` | Ultra easy only |
| 1 | `[ULTRA_EASY, EASY]` | Mix of ultra easy and easy |
| 2 | `[EASY]` | Easy only |
| 3 | `[EASY, MEDIUM]` | Mix easy and medium |
| 4 | `[MEDIUM]` | Medium only |
| 5 | `[MEDIUM, HARD]` | Mix medium and hard |
| 6 | `[HARD]` | Hard only |

Levels can optionally use `exerciseIds` for a fixed set of exercises, or `subjectCode` + `allowedDifficulties` for dynamic selection from a subject.

---

## API Design

### Start Ladder Session

```
GET /api/session/start?mode=ladder&ladderCode=default
```

**Response:**
```json
{
  "profileId": "uuid",
  "mode": "ladder",
  "exercise": { /* ExerciseDto */ },
  "ladderState": {
    "ladderCode": "default",
    "currentLevelIndex": 0,
    "recentScores": [],
    "overallScoreSum": 0,
    "overallTotal": 0
  },
  "sessionDefaultSeconds": 180,
  "lowBatteryModeSeconds": 45
}
```

### Request Next Exercise (After Completing One)

```
POST /api/session/ladder/next
Content-Type: application/json

{
  "profileId": "uuid",
  "ladderState": {
    "ladderCode": "default",
    "currentLevelIndex": 0,
    "recentScores": [0.8, 0.9, 1.0, 0.7, 0.85],
    "overallScoreSum": 4.25,
    "overallTotal": 5
  },
  "lastScore": 0.85
}
```

**Response:**
```json
{
  "exercise": { /* ExerciseDto */ },
  "ladderState": { /* updated state */ },
  "levelChanged": { "from": 0, "to": 1, "direction": "up" }
}
```

When advancing: `levelChanged.direction = "up"`. When demoting: `"down"`. When staying: `levelChanged` can be null or `direction: "none"`.

---

## Data Flow

1. **Start**: User starts ladder session → server returns first exercise from level 0 and initial `LadderState`.
2. **Play**: User completes exercise → frontend gets `ExerciseResult.score` (0–1).
3. **Next**: Frontend calls `POST /ladder/next` with `ladderState` + `lastScore`.
4. **Evaluate**: Server appends score to recentScores, updates overall, applies advancement logic.
5. **Return**: Server returns next exercise (from current level after eval) and updated `LadderState`.
6. **Repeat** until user exits or configurable end condition.

---

## Configuration (application.yml)

```yaml
app:
  ladder:
    defaultCode: default
    ladders:
      default:
        name: "Default Ladder"
        thresholds:
          minScoreToStay: 0.40
          minScoreToAdvance: 0.75
          answersNeededToAdvance: 5
        levels:
          - levelIndex: 0
            allowedDifficulties: [ULTRA_EASY]
            subjectCode: default
          - levelIndex: 1
            allowedDifficulties: [ULTRA_EASY, EASY]
            subjectCode: default
          - levelIndex: 2
            allowedDifficulties: [EASY]
            subjectCode: default
          # ...
```

---

## Frontend Integration

- **Session start**: When `mode=ladder`, `SessionResponseDto` (or equivalent) includes `ladderState` and `exercise` (single exercise for ladder; steps can be omitted or used differently).
- **ExercisePlayer**: Same component; `onComplete(result)` provides `result.score`.
- **LadderSessionBlock**: New component that:
  - Shows current level, current score %, overall score %
  - Renders `ExercisePlayer` with the current exercise
  - On complete, calls `POST /ladder/next` with state + score
  - Displays level-up / level-down feedback when `levelChanged` is present
  - Loops: get next exercise → play → submit → repeat until user clicks "Exit" or similar

---

## Out of Scope (For Initial Implementation)

- Persisting ladder progress across sessions (can be added later via `profile_subject_level` or similar)
- Multiple ladder configurations (single default ladder first)
- Journey step type `LADDER` (can add later to embed ladder in journey)

---

## Implementation Plan

### Phase 1: Domain & Config

1. Create `LadderThresholds`, `LadderLevel`, `LadderConfig`, `LadderState` in `domain/`
2. Create `LadderConfigProperties` with `@ConfigurationProperties`
3. Create `LadderPort` interface and `LadderConfigAdapter` implementation
4. Add ladder config to `application.yml`

### Phase 2: Backend Use Cases & API

5. `StartLadderSessionUseCase`: load ladder config, return first exercise from level 0 + initial state
6. `GetNextLadderExerciseUseCase`: accept state + lastScore, evaluate advancement, return next exercise + updated state
7. Extend `SessionController` for `mode=ladder` → delegate to `StartLadderSessionUseCase`
8. Add `POST /api/session/ladder/next` endpoint (or extend session controller)

### Phase 3: Frontend

9. Add `LadderStateDto`, `LadderNextRequestDto`, `LadderNextResponseDto` types and API client
10. Create `LadderSessionBlock` component (similar to `SessionExerciseBlock` but ladder flow)
11. Add route/page for ladder mode (e.g. `/ladder` or via session start with mode=ladder)

### Phase 4: Tests & Verification

12. Unit tests: `StartLadderSessionUseCase`, `GetNextLadderExerciseUseCase`, advancement logic
13. Integration test: `LadderControllerIntegrationTest` or session integration test for ladder
14. Manual verification: start ladder, complete exercises, verify level changes

---

## Files to Create/Modify

| Area | Files |
|------|-------|
| Domain | `LadderConfig.kt`, `LadderLevel.kt`, `LadderThresholds.kt`, `LadderState.kt` |
| Config | `LadderConfigProperties.kt`, adapter for `LadderPort` |
| Use cases | `StartLadderSessionUseCase.kt`, `GetNextLadderExerciseUseCase.kt` |
| Ports | `LadderPort.kt` |
| Web | `SessionController.kt` (extend), `LadderController.kt` or extend SessionController |
| DTOs | `LadderStateDto`, `LadderNextRequestDto`, `LadderNextResponseDto` |
| Frontend | `LadderSessionBlock.tsx`, `api/ladder.ts`, types |
| Tests | `StartLadderSessionUseCaseTest.kt`, `GetNextLadderExerciseUseCaseTest.kt`, `LadderControllerIntegrationTest.kt` |

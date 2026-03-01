# Exercise Result Display: Decoupled Architecture

## Problem Analysis

### Current Architecture (Coupled)

```
ExercisePlayer
├── Renders exercise (e.g. GenericTextExercise)
├── When complete: sets internal `completed` state
└── Renders ScoreAnimation (coupled inside ExercisePlayer)
```

**Issues:**
1. **ExercisePlayer owns result display** — The score is shown by ExercisePlayer, not by the orchestrator (Ladder, Session, Play page).
2. **Inflexible chaining** — To chain exercises (ladder, session steps) with custom flow (Continue, Next, level stats), the parent must work around ExercisePlayer by replacing it when done (LadderSessionBlock) or loses control (SessionExerciseBlock).
3. **Duplicate responsibilities** — PlayExercisePage already has its own result display; LadderSessionBlock duplicates it. SessionExerciseBlock relies on ExercisePlayer's built-in display.
4. **Parent cannot control** — The orchestrator cannot add Continue/Next, level feedback, or custom layout around the result.

### Desired Architecture (Decoupled)

```
Orchestrator (LadderSessionBlock | SessionExerciseBlock | PlayExercisePage)
├── Renders ExercisePlayer when playing
├── Receives onComplete(result, elapsedMs)
├── Owns result display (ScoreAnimation + Continue/Next/Back)
└── Controls flow (fetch next, advance step, etc.)
```

**Benefits:**
- ExercisePlayer = **pure exercise runner** — only renders the exercise, reports result via callback.
- Orchestrator = **owns result and flow** — displays score, provides Continue/Next, tracks level/session state.
- Reusable — Same ExercisePlayer in ladder, session, standalone; each context customizes the done view.

## Solution

1. **ExercisePlayer**: Remove built-in ScoreAnimation. On complete, call `onComplete(result, elapsedMs)` and keep rendering the exercise (parent will replace on next render).
2. **Orchestrators**: Must handle `onComplete` and render the result view (ScoreAnimation + action button) when done.
3. **SessionExerciseBlock**: Capture result, show ScoreAnimation + Next/Continue.
4. **LadderSessionBlock**: Already decoupled (shows ScoreAnimation + Continue).
5. **PlayExercisePage**: Already decoupled (shows ScoreAnimation + links).

## Implementation (Done)

| Component | Before | After |
|-----------|--------|-------|
| ExercisePlayer | Rendered ScoreAnimation when complete | Only calls onComplete, never renders result. Pure exercise runner. |
| SessionExerciseBlock | onComplete→setStepDone only, relied on ExercisePlayer for score | onComplete→setLastResult+setStepDone, renders ScoreAnimation+Next when done |
| LadderSessionBlock | Already renders own ScoreAnimation+Continue | No change |
| PlayExercisePage | Already renders own ScoreAnimation | No change |

## Verification

- Backend: LadderSessionApiIntegrationTest, SessionApiIntegrationTest pass.
- Frontend: Build succeeds. Exercise flow: exercise → onComplete → parent renders ScoreAnimation.

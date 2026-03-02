# Sum Ladder

Math ladder progression from addition (ultra easy) through subtraction, multiplication, and division.

## Level Structure

| Levels | Operations | Difficulty | Description |
|--------|------------|------------|-------------|
| 0 | ADD | ULTRA_EASY | Ultra easy sum only |
| 1–5 | ADD | ULTRA_EASY→EASY | Sum only, increasing difficulty |
| 6–10 | ADD, SUBTRACT | ULTRA_EASY→EASY | Add subtraction, same progression |
| 11–15 | ADD, SUBTRACT | EASY→MEDIUM | Mix to medium sum-subtraction |
| 16–20 | ADD, SUBTRACT, MULTIPLY, DIVIDE | ULTRA_EASY→EASY | All four operations |
| 21–25 | MULTIPLY, DIVIDE | EASY→HARD | Increasing mult/div difficulty |

## Scoring Rules (Ladder mode)

- **Stay:** ≥ 40% over last 5 answers at current level
- **Advance:** ≥ 75% over last 5 answers → go to next level
- **Demote:** < 40% (if level > 0) → go back one level
- **Reset:** When level changes (up or down), the 5-answer window is cleared. The new level starts fresh.

## Flow

1. Complete an exercise → **Score is displayed** (result %, time, subscores).
2. Click **Continue** → Next exercise loads (level may advance, stay, or demote).
3. Header shows **Level · X/5 answers · Current · Overall** so you see progress toward the 5-answer evaluation.

## Access

- **URL:** `/ladder/sum` or [http://localhost:5173/ladder/sum](http://localhost:5173/ladder/sum)
- **API:** `GET /api/session/start?mode=ladder&ladderCode=sum`
- **End:** Click "End" to stop the session and return to the ladder menu (or home)

## Configuration

Defined in `application.yml` under `app.ladder.ladders.sum`. Levels use `exerciseParamFilter.operation` to filter FLASHCARD_QA exercises by operation (ADD, SUBTRACT, MULTIPLY, DIVIDE). Each level's `allowedDifficulties` (e.g. ULTRA_EASY, EASY) drive which exercises are selected; when a math problem is generated, its human arithmetic complexity is targeted to fall within that difficulty's band (see [human_arithmetic_complexity_model.md](human_arithmetic_complexity_model.md)).

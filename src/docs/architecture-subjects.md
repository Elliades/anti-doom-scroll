# Architecture: Subjects and Exercises

## Analysis (from request + .cursorrules + exercises.md)

### Requirements
- **Easily add subjects** and **exercises to subjects** without code churn.
- **Subjects** carry **scoring and other properties** defined in .cursorrules:
  - Scoring: accuracy, speed, confidence calibration, streak bonus (small); never punish harshly.
  - Per-subject variants (see exercises.md): e.g. Flashcards (Acc: 1/0, Speed: min(1, Ttarget/Tactual)), Blurting (Acc: matched/expected, weighted), N-back (F1 or hits/total), etc.
- **Scale**: many subjects, many exercises; session picks by subject + difficulty.

### Current state
- Exercise has free-form `axis`; no first-class Subject.
- Scoring is not modeled per subject; Attempt has score, reactionTimeMs, confidencePercent.

### Design decisions
1. **Subject** is a first-class entity: code (e.g. `A1`, `B1`), name, optional parent (for hierarchy A → A1, A2), and **scoring config**.
2. **Scoring config** is a structured value object (stored as JSONB per subject) so new subjects add data, not code: formula identifiers + params (e.g. `accuracy: "binary" | "partial" | "weighted"`, `speedTargetMs`, `streakBonusCap`).
3. **Exercise** belongs to exactly one **Subject** (`subject_id` FK). "Axis" in session/daily plan becomes **subject code**.
4. **Daily plan / profile**: "daily axes" → **daily subject codes** (list of subject codes the user wants today). Session picks ultra-easy from any subject, then easy/medium from first chosen subject.

---

## Plan

| Step | What |
|------|------|
| 1 | Domain: `Subject`, `SubjectScoringConfig`; `Exercise.subjectId`; keep `Attempt` as-is. |
| 2 | DB: `subject` table, `exercise.subject_id` FK, migration; index (subject_id, difficulty). |
| 3 | Ports: `SubjectPort` (findByCode, listAll); `ExercisePort`: findBySubjectAndDifficulty, findOneUltraEasy(subjectCode?) for cache. |
| 4 | Use case: start session by subject (from profile daily subject codes or default). |
| 5 | Seed: default subject + link existing exercise; optional A1/B1 from exercises.md. |
| 6 | Verify: integration test; API to list subjects (optional). |

---

## Domain model (summary)

- **Subject**: id, code (unique), name, description, parentSubjectId?, scoringConfig (SubjectScoringConfig), createdAt.
- **SubjectScoringConfig**: accuracyType (binary | partial | weighted), speedTargetMs?, confidenceWeight?, streakBonusCap?, partialMatchThreshold? (for cloze). Stored as JSONB; interpreter in domain or small service.
- **Exercise**: id, **subjectId**, type, difficulty, prompt, expectedAnswers, timeLimitSeconds, createdAt. (axis removed; subject implies it.)
- **UserProfile**: dailyAxes → **dailySubjectCodes** (list of subject codes).
- **Attempt**: unchanged (score, reactionTimeMs, confidencePercent).

Scoring computation can stay in domain (e.g. `SubjectScoringConfig.compute(correct, reactionTimeMs, confidencePercent, streakCount)`) so it remains transparent and testable.

---

## How to add subjects and exercises (scalable)

1. **Add a subject**  
   Insert into `subject` (id, code, name, description, parent_subject_id, scoring_config).  
   Use a new UUID and a unique `code` (e.g. `B3`, `C2`).  
   Set `scoring_config` JSON from .cursorrules (accuracyType, speedTargetMs, confidenceWeight, streakBonusCap, optional partialMatchThreshold).

2. **Add exercises to a subject**  
   Insert into `exercise` (id, **subject_id**, type, difficulty, prompt, expected_answers, time_limit_seconds).  
   No code change: link to existing subject by `subject_id`.

3. **Profile “daily axes”**  
   Stored as `user_profile` / `daily_plan` axes: use **subject codes** (e.g. `["A1","B1"]`).  
   Session step 1 uses first subject’s ultra-easy; step 2 uses first subject’s easy/medium.

**API**
- `GET /api/subjects` — list all subjects (code, name, scoringConfig).
- `GET /api/subjects/{code}` — get one subject by code.
- `GET /api/session/start` — returns steps with `exercise.subjectId` and `exercise.subjectCode`.

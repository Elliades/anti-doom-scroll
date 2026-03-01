# Architecture: Scalable Subjects and Exercises

## Goals

- **Dedicated URLs** for each subject and each exercise so they can be linked, bookmarked, and opened directly.
- **Reusable exercise UI**: the same component that plays an exercise is used in (1) the main journey flow and (2) a standalone “play” page. No duplication; behavior is identical in both contexts.
- **Easy to add** new subjects and new exercise types without breaking existing flows.

---

## URL and routing design

| Route | Purpose |
|-------|---------|
| `/` | Main app: journey flow (steps 0, 1, 2…) with `?step=` and `?chapterIndex=` |
| `/subjects` | List all subjects (codes, names). Entry point to pick a subject. |
| `/subjects/:subjectCode` | Subject detail: list exercises for that subject; user picks one to play. |
| `/play/:exerciseId` | Standalone page: layout + single exercise player. Same player component as in journey. |

- **Subject code** is a stable identifier (e.g. `default`, `B1`, `nback`). **Exercise ID** is the backend UUID so each exercise has a unique URL.
- Adding a new subject or exercise only requires data (DB/config); no new routes.

---

## Backend API (REST)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/subjects` | List all subjects. |
| GET | `/api/subjects/{code}` | Get one subject by code. |
| GET | `/api/subjects/{code}/exercises` | List exercises for that subject (for subject detail page). |
| GET | `/api/exercises/{id}` | Get one exercise by ID (for dedicated play page). |
| GET | `/api/nback/{level}` | N-back by level (1, 2, 3); existing. |
| GET | `/api/session/start` | Start session (journey use). |
| GET | `/api/journey?...` | Journey definition and step content (journey use). |

- **Exercise by ID** and **exercises by subject** support direct linking and standalone play.
- New exercise types are added in domain + persistence; existing endpoints return `type` and type-specific params (e.g. `nBackParams`). No new endpoint per type.

---

## Frontend: exercise player reuse

- **ExercisePlayer** (or equivalent) is the single component that:
  - Receives one **exercise** (DTO) and an **onComplete** (and optional **onScore**) callback.
  - Renders the correct UI by **exercise type** (e.g. `N_BACK`, `FLASHCARD_QA`) using a **registry**: `type → component`.
- **Journey flow**: `SessionExerciseBlock` receives a session (list of steps); for each step it renders **ExercisePlayer** with that step’s exercise. No inlining of N-back vs generic logic in the block; the block only iterates steps and delegates to the player.
- **Standalone play page** (`/play/:exerciseId`): a minimal layout (e.g. header + main area) that fetches the exercise by ID and renders **ExercisePlayer** with that exercise. Same component, same props contract.
- **Adding a new exercise type**:
  1. Implement a small presentational component (e.g. `XxxExercise.tsx`) that takes `exercise: ExerciseDto` and `onComplete?: (score?: number) => void`.
  2. Register it in the type registry (e.g. `EXERCISE_TYPE_COMPONENTS['NEW_TYPE'] = NewTypeExercise`).
  3. No changes to `SessionExerciseBlock` or the play page layout.

---

## Frontend: type registry pattern

- Central map: `ExerciseType → React component`.
- Default/unknown type: render a generic fallback (e.g. text prompt + optional input) so missing types don’t crash the app.
- Keeps `SessionExerciseBlock` and the play page layout stable when new types are added.

---

## Data flow

- **Journey**: App loads journey → step content (session or reflection or chapter series) → for session steps, `SessionExerciseBlock` renders **ExercisePlayer** per step.
- **Subject list**: Page fetches `GET /api/subjects` → links to `/subjects/:code`.
- **Subject detail**: Page fetches `GET /api/subjects/:code/exercises` → links to `/play/:exerciseId` for each exercise.
- **Play page**: Page fetches `GET /api/exercises/:id` → renders **ExercisePlayer** with that exercise in a simple layout.

---

## Adding a new subject

1. **Backend**: Insert subject (and optionally exercises) via migrations or seed data. No code change if APIs already support list/get by code and list exercises by subject.
2. **Frontend**: No code change; subject list and subject detail pages are data-driven from API.

---

## Adding a new exercise type

1. **Backend**: Add enum value in `ExerciseType`; add persistence for type-specific params if needed; ensure `ExerciseDto` (or equivalent) can carry those params; existing endpoints already return `type` and params.
2. **Frontend**: Add one component for the type; register it in the exercise type registry. Journey and play page automatically use it.

---

## Summary

- **Dedicated URLs**: `/subjects`, `/subjects/:subjectCode`, `/play/:exerciseId`.
- **One player component** used in journey and on the play page; behavior identical.
- **Type registry** for exercise UI so new types are additive (new component + one registration).
- **APIs**: subject list/get, exercises by subject, exercise by ID; new subjects/exercises are data-only.

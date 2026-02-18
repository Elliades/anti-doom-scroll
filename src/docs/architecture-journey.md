# Architecture: Journey

## Overview

The **journey** is a scalable, ordered sequence of steps the user goes through. It supports:
- **On opening**: ultra-easy exercises (OpenApp step).
- **Reflection**: e.g. "Why am I doom scrolling?" (content step).
- **By chapters**: exercise series per subject/chapter (CHAPTER_EXERCISES step).

The journey is **easily scalable**: add or reorder steps in config; **navigate to any step** by index (e.g. go back).

## Domain

- **Journey**: `code`, `name`, `steps: List<JourneyStepDef>`.
- **JourneyStepDef**: `stepIndex`, `type: JourneyStepType`, `config: Map`.
- **JourneyStepType**: `OPEN_APP`, `REFLECTION`, `CHAPTER_EXERCISES`.

Step config (type-specific):
- **OPEN_APP**: `exerciseCount` (default 3).
- **REFLECTION**: `contentKey` (e.g. `why-doom-scrolling`).
- **CHAPTER_EXERCISES**: `subjectCodes` (ordered list of subject/chapter codes).

## Configuration

Journey is defined in `application.yml` under `app.journey`:

```yaml
app:
  journey:
    code: default
    name: "Default journey"
    steps:
      - stepIndex: 0
        type: OPEN_APP
        config:
          exerciseCount: 3
      - stepIndex: 1
        type: REFLECTION
        config:
          contentKey: why-doom-scrolling
      - stepIndex: 2
        type: CHAPTER_EXERCISES
        config:
          subjectCodes: [default, B1]
    content:
      why-doom-scrolling:
        title: "Why am I doom scrolling?"
        body: "..."
```

To **add a step**: append to `steps` with a new `stepIndex` and set `type` + `config`.  
To **go back to a precise step**: frontend (or API) uses `stepIndex` in URL or state.

## Ports

- **JourneyPort**: `getByCode(code: String): Journey?`
- **ReflectionContentPort**: `getByKey(contentKey: String): ReflectionContent?`

Implemented by **JourneyConfigAdapter** (config-driven). Can be replaced by DB or CMS later.

## Use cases

- **GetJourneyStepContentUseCase**: given `journeyCode`, `stepIndex`, optional `profileId` and `chapterIndex`, returns content for that step:
  - OPEN_APP → openapp session (3 ultra-easy/easy exercises).
  - REFLECTION → title + body from content key.
  - CHAPTER_EXERCISES → list of chapter codes + session for one chapter (by `chapterIndex`).

- **StartSessionUseCase** (existing): `startOpenAppSession`, `startSessionForSubject` (used by journey for OPEN_APP and CHAPTER_EXERCISES).

## API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/journey?code=default` | Journey definition (code, name, steps with type + config). |
| GET | `/api/journey/steps/{stepIndex}/content?journeyCode=default&profileId=...&chapterIndex=0` | Content for step: `type` + `session` \| `reflection` \| `chapterSeries`. |

Response for step content: **JourneyStepContentDto** with `stepIndex`, `type`, and one of:
- `session`: SessionResponseDto (OPEN_APP or CHAPTER_EXERCISES current chapter).
- `reflection`: `{ title, body }`.
- `chapterSeries`: `{ chapters, currentChapterIndex, session }`.

## Frontend flow

1. Load journey: `GET /api/journey`.
2. Current step index in state (or URL `?step=0`); optional `chapterIndex` for CHAPTER_EXERCISES.
3. Load step content: `GET /api/journey/steps/{stepIndex}/content&chapterIndex=...`.
4. Render by `type`: OpenApp session UI, Reflection screen, or Chapter series (session + chapter list).
5. On "Continue" / "Next": advance step (or chapter within step); optionally allow "Go to step N" to jump back.

## Scalability

- **Add steps**: edit YAML `app.journey.steps`; no code change.
- **Add step types**: add enum value, handle in GetJourneyStepContentUseCase and frontend.
- **Go to precise step**: use `stepIndex` (and `chapterIndex`) in API and UI.

## Testing

- **Integration tests**: `JourneyApiIntegrationTest` (MockMvc, `@SpringBootTest`, `@Sql("/data.sql")`).
  - `GET /api/journey?code=default` → 200, code, name, steps (OPEN_APP, REFLECTION, CHAPTER_EXERCISES).
  - `GET /api/journey?code=unknown` → 404.
  - `GET /api/journey/steps/0/content` → OPEN_APP, session with profileId and steps.
  - `GET /api/journey/steps/1/content` → REFLECTION, title and body.
  - `GET /api/journey/steps/2/content` and `chapterIndex` → CHAPTER_EXERCISES, chapters and session.
  - Out-of-range step → 404.
- Test config: `application-test.yml` includes `app.journey` and `app.journey.content` so journey beans load.
- See `src/docs/TESTING.md` for full test inventory and how to run tests.

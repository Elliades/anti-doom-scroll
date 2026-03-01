# N-Back Architecture: Card, Grid, and Dual Variants

> **Related:** [exercise-nback.md](exercise-nback.md) (single N-back), [exercises-index.md](exercises-index.md).

## Executive Summary

This document defines the architecture for extending N-back exercises to support:
1. **Grid N-Back** – position-based stimuli on a 3×3 grid
2. **Grid Dual N-Back** – grid positions + temporary colors (up to 4); user selects "Match Position" or "Match Color"
3. **Card Dual N-Back** – playing cards; user selects "Match Color" (suit) or "Match Number" (rank)

The existing **Card N-Back** (`N_BACK` with card sequence) remains unchanged.

---

## Current Architecture (Card N-Back)

### What Exists

| Layer | Component | Responsibility |
|-------|------------|-----------------|
| **Domain** | `NBackParams` | `n`, `sequence: List<String>`, `matchIndices`; `evaluate(userMatchPositions)` → `NBackScore` |
| **Domain** | `Exercise.nBackParams()` | Parse `exerciseParams` for type `N_BACK` |
| **Backend** | `NBackController` | `GET /api/nback/{level}` → 1/2/3-back by level |
| **Backend** | `ExerciseDtoMapper` | Maps `NBackParams` → `NBackParamsDto` |
| **Frontend** | `NBackExercise` | Phase flow (intro → playing → done); card/letter display; Match button |
| **Frontend** | `NBackCardDisplay` | Renders card codes (AC, 2D, QH) via CSS Playing Cards |
| **Frontend** | `ExercisePlayer` | Type registry: `N_BACK` → `NBackExercise` |

### Reusable Building Blocks

| Reusable | Usage for Grid / Dual |
|----------|------------------------|
| **Phase flow** | `intro → playing → done`; interval-based stimulus advance; progress display |
| **Scoring logic** | `NBackScore`, `evaluate()`, `normalizedScore()` – same hits/misses/false alarms |
| **Feedback** | "Correct!" / "Wrong" – can be extended for dual (position vs color feedback) |
| **ExercisePlayer** | Add new types to registry; same `ExerciseDto` + `onComplete` contract |
| **ExerciseDtoMapper** | Add new param DTOs and mapping; `resolveNBackParamsIfNeeded` pattern for new types |
| **Session/Journey** | Same session step model; exercises fetched by type or ID |

### Not Reusable As-Is

| Component | Reason |
|-----------|--------|
| `NBackParams.sequence: List<String>` | Grid uses positions (0–8) or `{position, color}` |
| `NBackCardDisplay` | Grid needs cell highlighting; cards need suit/rank extraction for dual |
| Single "Match" button | Dual needs "Match Position" + "Match Color" (or "Match Number") |
| Card code parsing | Card dual needs `parseCardCode` → suit + rank for match detection |

---

## New Exercise Types

| Type | Stimulus | Match Target | User Action |
|------|----------|--------------|-------------|
| `N_BACK` | Cards (existing) | Card identity | Tap "Match" |
| `N_BACK_GRID` | Grid position | Same position N back | Tap "Match" |
| `DUAL_NBACK_GRID` | Grid position + color | Position N back, or color N back | Tap "Match Position" / "Match Color" |
| `DUAL_NBACK_CARD` | Cards | Suit N back, or rank N back | Tap "Match Color" / "Match Number" |

---

## Param Schemas

### N_BACK_GRID

```json
{
  "n": 1,
  "sequence": [0, 4, 2, 4, 8, 1, 8],
  "matchIndices": [3, 6],
  "gridSize": 3
}
```

- **sequence**: Grid cell indices 0–8 (row-major: 0=top-left, 8=bottom-right).
- **matchIndices**: Indices where `sequence[i] == sequence[i - n]`.
- **gridSize**: 3 for 3×3 (default).

### DUAL_NBACK_GRID

```json
{
  "n": 1,
  "sequence": [
    { "position": 0, "color": "#4285F4" },
    { "position": 4, "color": "#EA4335" },
    { "position": 0, "color": "#FBBC04" }
  ],
  "matchPositionIndices": [2],
  "matchColorIndices": [],
  "colors": ["#4285F4", "#EA4335", "#FBBC04", "#34A853"],
  "gridSize": 3
}
```

- **sequence**: `{ position: 0-8, color: hex }` per step.
- **matchPositionIndices**: `sequence[i].position == sequence[i-n].position`.
- **matchColorIndices**: `sequence[i].color == sequence[i-n].color`.
- **colors**: Up to 4 distinct colors for stimuli.

### DUAL_NBACK_CARD

```json
{
  "n": 1,
  "sequence": ["AC", "2D", "AH", "2S", "3C"],
  "matchColorIndices": [2],
  "matchNumberIndices": [3]
}
```

- **sequence**: Card codes (AC, 2D, …).
- **matchColorIndices**: Suit match (e.g. AC and AH both Clubs/Hearts? No – C vs H; so "color" = suit, so AC vs 2C would match).
- **matchNumberIndices**: Rank match (e.g. 2D and 2S both rank 2).
- **Convention**: "Color" = suit; "Number" = rank.

---

## Scoring (Dual N-Back)

- **Position (or card identity)**: Same as single N-back: hits, misses, false alarms.
- **Color/Number**: Separate sets; each modality scored independently.
- **Combined score**: `(positionScore + colorScore) / 2` or `accuracy = (hitsPos + hitsColor) / (targetsPos + targetsColor)` with false alarm penalty.

---

## API and URLs

| Exercise | Access |
|----------|--------|
| N_BACK (card) | `GET /api/nback/1`, `/2`, `/3` |
| N_BACK_GRID | `GET /api/nback/grid/{level}` or via `GET /api/exercises/{id}` |
| DUAL_NBACK_GRID | `GET /api/nback/dual/grid/{level}` or via exercises |
| DUAL_NBACK_CARD | `GET /api/nback/dual/card/{level}` or via exercises |

**Recommendation**: Use `GET /api/exercises/{id}` as the primary access; add level endpoints only if needed for deep-linking (like existing `/api/nback/1`).

---

## Implementation Plan

### Phase 1: Domain and Backend

1. **ExerciseType**
   - Add `N_BACK_GRID`, `DUAL_NBACK_GRID`, `DUAL_NBACK_CARD`.

2. **Domain params**
   - `NBackGridParams(n, sequence: List<Int>, matchIndices, gridSize)`
   - `DualNBackGridParams(n, sequence: List<GridStimulus>, matchPositionIndices, matchColorIndices, colors, gridSize)`
   - `DualNBackCardParams(n, sequence, matchColorIndices, matchNumberIndices)`
   - Each has `evaluate()` returning extended score (hits per modality).

3. **Exercise parsers**
   - `nBackGridParams()`, `dualNBackGridParams()`, `dualNBackCardParams()` on `Exercise`.

4. **DTOs**
   - `NBackGridParamsDto`, `DualNBackGridParamsDto`, `DualNBackCardParamsDto` in `ExerciseDto`.

5. **Generation**
   - `NBackGridGenerator`: random positions 0–8, ensure `matchIndices` valid.
   - `DualNBackGridGenerator`: random (position, color) pairs; compute both match sets.
   - `DualNBackCardGenerator`: random card codes; compute suit-match and rank-match indices.

6. **API**
   - Extend `ExerciseDtoMapper` for new types; optionally add `/api/nback/grid/{level}`, etc.
   - Seed exercises via migrations.

### Phase 2: Frontend

1. **Types**
   - Add new param DTOs to `api.ts`; extend `ExerciseDto`.

2. **Components**
   - `NBackGridExercise`: 3×3 grid, one highlighted cell, "Match" button, reuse phase flow.
   - `DualNBackGridExercise`: 3×3 grid, colored cell, "Match Position" + "Match Color", dual scoring.
   - `DualNBackCardExercise`: Reuse `NBackCardDisplay`, add "Match Color" + "Match Number".

3. **Registry**
   - Register in `ExercisePlayer`: `N_BACK_GRID`, `DUAL_NBACK_GRID`, `DUAL_NBACK_CARD`.

4. **Styling**
   - Grid: `.nback-grid`, `.nback-grid-cell`, `.nback-grid-cell--active`, `.nback-grid-cell--color-*`.
   - Reuse `.nback-match-btn` for dual (two buttons).

### Phase 3: Tests and Verification

1. **Unit**
   - Param validation, `evaluate()` for each new params class.
   - Generators produce valid sequences and correct `match*Indices`.

2. **Integration**
   - GET exercise by ID returns valid DTO for new types.
   - Session includes new types when configured.

3. **E2E**
   - Play grid N-back, dual grid, dual card; verify feedback and scoring.

---

## File Checklist

| Layer | New/Modified Files |
|-------|--------------------|
| Domain | `NBackGridParams.kt`, `DualNBackGridParams.kt`, `DualNBackCardParams.kt`; `ExerciseType.kt`; `Exercise.kt` (parsers) |
| Application | `NBackGridGenerator`, `DualNBackGridGenerator`, `DualNBackCardGenerator` (or single generator service) |
| Web | `ExerciseDto.kt` (new DTOs); `ExerciseDtoMapper.kt`; `NBackController.kt` (optional new routes) |
| DB | Migration to seed `N_BACK_GRID`, `DUAL_NBACK_GRID`, `DUAL_NBACK_CARD` exercises |
| Frontend | `NBackGridExercise.tsx`, `DualNBackGridExercise.tsx`, `DualNBackCardExercise.tsx`; `ExercisePlayer.tsx`; `api.ts`; `App.css` |

---

## Summary

| Variant | Stimulus | Match Type | Buttons |
|---------|----------|------------|---------|
| Card N-Back | Cards | Identity | Match |
| Grid N-Back | Grid position | Position | Match |
| Grid Dual N-Back | Grid + color | Position, Color | Match Position, Match Color |
| Card Dual N-Back | Cards | Suit, Rank | Match Color, Match Number |

Reuse: phase flow, scoring model, ExercisePlayer, DTO mapper pattern, session/journey integration.
New: grid UI, dual response handling, generators for grid/dual params.

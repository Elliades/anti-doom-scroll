# Remember Number Exercise

## Overview
A working memory exercise that tests the user's ability to hold a number in mind
while performing a distracting math task. The user is shown a number, solves a math
problem (distraction), then must recall the original number.

## Exercise Flow
1. **Intro** — Brief instructions + Start button
2. **Memorize** — A random number is displayed for a limited time (countdown)
3. **Math distraction** — A math problem appears; the user must solve it (same as FLASHCARD_QA)
4. **Recall** — The user types the memorized number
5. **Result** — Both answers are revealed (math correctness + recall correctness)

## Params (`exerciseParams` stored in DB)
| Key | Type | Description |
|-----|------|-------------|
| `numberDigits` | int | Number of digits in the number to memorize (2–7) |
| `displayTimeMs` | int | How long the number is shown before it disappears |
| `mathOperation` | string | Distraction operation: ADD, SUBTRACT, MULTIPLY, DIVIDE |
| `mathFirstMax` | int | Max first operand for the generated math problem |
| `mathSecondMax` | int | Max second operand for the generated math problem |

## Backend-generated DTO (`RememberNumberParamsDto`)
| Field | Type | Description |
|-------|------|-------------|
| `numberToRemember` | int | The randomly generated number |
| `displayTimeMs` | int | Display duration in ms |
| `mathPrompt` | string | Generated math problem (e.g. "What is 12 + 7?") |
| `mathExpectedAnswer` | string | Expected answer for the math problem |
| `mathComplexityScore` | double? | Arithmetic complexity score of the math problem |

## Difficulty scaling
| Difficulty | Digits | Display time | Math operation | Math range |
|-----------|--------|-------------|---------------|-----------|
| ULTRA_EASY | 2 | 3000 ms | ADD | 1-digit + 1-digit |
| EASY | 3 | 2500 ms | ADD/SUBTRACT | 1–2 digit |
| MEDIUM | 4 | 2000 ms | ADD/SUBTRACT/MULTIPLY | 1–2 digit |
| HARD | 5 | 1500 ms | All four | 1–2 digit |
| VERY_HARD | 6 | 1200 ms | All four | 1–2 digit |

## Scoring (client-side)
Both parts must be correct for a pass — failing either one means failing the exercise:
- **Recall correct + Math correct** → 1.0 (pass)
- **Any part wrong** → 0.0 (fail)

Subscores:
- `Number recall` — "Correct" or "Wrong"
- `Math answer` — "Correct" or "Wrong"
- `Problem complexity` — arithmetic complexity score

## Subject
Belongs to **MEMORY** (`b0000000-0000-0000-0000-000000000008`).

## ID prefix
Uses `f3000000-…` to avoid collision with other exercise types.

## Registered in
| Code | Name | Exercise types |
|------|------|----------------|
| `MEMORY` | Memory | MEMORY_CARD_PAIRS, SUM_PAIR, IMAGE_PAIR, **REMEMBER_NUMBER** |

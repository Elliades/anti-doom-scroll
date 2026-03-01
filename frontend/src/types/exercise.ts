/**
 * Extensible exercise result for completion.
 * Any exercise can customize scoring and provide subscore details.
 */
export interface SubscoreDetail {
  /** Display label (e.g. "Hits", "Moves") */
  label: string
  /** Display value (e.g. "5/6", "12") */
  value: string | number
}

export interface ExerciseResult {
  /** Overall score 0–1 (displayed as 0–100%) */
  score: number
  /** Optional exercise-specific subscore details shown in the score animation */
  subscores?: SubscoreDetail[]
}

/** Normalize score (number) or full result to ExerciseResult */
export function toExerciseResult(input: ExerciseResult | number | undefined): ExerciseResult {
  if (input == null) return { score: 0 }
  if (typeof input === 'number') return { score: input }
  return { score: input.score, subscores: input.subscores }
}

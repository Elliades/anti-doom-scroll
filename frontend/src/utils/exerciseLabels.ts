import type { ExerciseDto } from '../types/api'

/**
 * Display label for exercise type. For FLASHCARD_QA uses mathOperation (Sum, Subtraction, etc.).
 */
export function getExerciseTypeLabel(ex: ExerciseDto): string {
  if (ex.type === 'FLASHCARD_QA' && ex.mathOperation) {
    const mathLabels: Record<string, string> = {
      ADD: 'Sum',
      SUBTRACT: 'Subtraction',
      MULTIPLY: 'Multiplication',
      DIVIDE: 'Division',
    }
    return mathLabels[ex.mathOperation] ?? 'Sum'
  }
  const labels: Record<string, string> = {
    FLASHCARD_QA: 'Sum',
    N_BACK: 'N-Back (Card)',
    N_BACK_GRID: 'N-Back (Grid)',
    DUAL_NBACK_GRID: 'Dual N-Back (Grid)',
    DUAL_NBACK_CARD: 'Dual N-Back (Card)',
    MEMORY_CARD_PAIRS: 'Memory Pairs',
    SUM_PAIR: 'Sum Pair',
    ANAGRAM: 'Anagram',
    DIGIT_SPAN: 'Digit Span',
  }
  return labels[ex.type] ?? ex.type
}

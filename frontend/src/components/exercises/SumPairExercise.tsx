import { useState, useCallback, useEffect, useRef, useMemo } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'
import { estimateSumPairComplexityScore } from '../../api/exerciseParamGenerators'

export interface SumPairExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
  showInstruction?: boolean
}

type CardState = 'hidden' | 'revealed' | 'matched'

interface CardItem {
  value: number
  staticK: number
  color: string
  state: CardState
}

/**
 * Sum-pair game: all cards on one board. Statics are colored; each static's pairs
 * have the same color. Match (a, b) only when a + static = b within the same group.
 */
export function SumPairExercise({ exercise, onComplete, showInstruction = true }: SumPairExerciseProps) {
  const groups = exercise.sumPairGroups ?? exercise.sumPairRounds?.map((r) => ({
    static: r.static,
    color: '#3b82f6',
    cards: r.cards,
  }))
  const deck = exercise.sumPairDeck
  if (!groups?.length && !deck?.length) {
    return <p className="error">Invalid sum-pair exercise: missing groups or deck.</p>
  }

  const initialCards: CardItem[] = useMemo(() => {
    if (deck?.length) {
      return deck.map((c) => ({
        value: c.value,
        staticK: c.static,
        color: c.color,
        state: 'hidden' as CardState,
      }))
    }
    const items: CardItem[] = []
    for (const g of groups!) {
      for (const value of g.cards) {
        items.push({ value, staticK: g.static, color: g.color, state: 'hidden' })
      }
    }
    return shuffleArray([...items])
  }, [deck, groups])

  const [phase, setPhase] = useState<'intro' | 'playing' | 'done'>('intro')
  const [cards, setCards] = useState<CardItem[]>([])
  const [moves, setMoves] = useState(0)
  const completedRef = useRef(false)
  const complexityScore = useMemo(() => {
    const values = initialCards.map((c) => c.value)
    const minValue = values.length > 0 ? Math.min(...values) : 1
    const maxValue = values.length > 0 ? Math.max(...values) : 99
    const params = exercise.sumPairParams ?? {
      staticNumbers: (groups ?? []).map((g) => g.static),
      pairsPerRound: groups?.[0] ? Math.floor(groups[0].cards.length / 2) : 3,
      minValue,
      maxValue,
    }
    return Math.round(estimateSumPairComplexityScore(params))
  }, [exercise.sumPairParams, groups, initialCards])

  const isMultiStatic = (groups ?? []).length > 1

  const startGame = useCallback(() => {
    setCards(initialCards.map((c) => ({ ...c, state: 'hidden' as CardState })))
    setMoves(0)
    setPhase('playing')
    completedRef.current = false
  }, [initialCards])

  const flippedIndices = cards
    .map((c, i) => (c.state === 'revealed' ? i : -1))
    .filter((i) => i >= 0)

  const isSumPair = useCallback((a: number, b: number, staticK: number) => {
    const lo = Math.min(a, b)
    const hi = Math.max(a, b)
    return lo + staticK === hi
  }, [])

  const handleCardClick = useCallback(
    (index: number) => {
      if (phase !== 'playing') return
      const card = cards[index]
      if (card.state !== 'hidden') return
      if (flippedIndices.length >= 2) return

      setCards((prev) => {
        const next = prev.map((c, i) =>
          i === index ? { ...c, state: 'revealed' as CardState } : c
        )
        const revealed = next
          .map((c, i) => (c.state === 'revealed' ? i : -1))
          .filter((i) => i >= 0)
        if (revealed.length === 2) {
          setMoves((m) => m + 1)
          const [i, j] = revealed
          const ca = next[i]
          const cb = next[j]
          if (ca.staticK === cb.staticK && isSumPair(ca.value, cb.value, ca.staticK)) {
            return next.map((c, idx) =>
              idx === i || idx === j ? { ...c, state: 'matched' as CardState } : c
            )
          }
          return next
        }
        return next
      })
    },
    [phase, cards, flippedIndices.length, isSumPair]
  )

  const twoRevealedNoMatch =
    flippedIndices.length === 2 && (() => {
      const [i, j] = flippedIndices
      const ca = cards[i]
      const cb = cards[j]
      return ca.staticK !== cb.staticK || !isSumPair(ca.value, cb.value, ca.staticK)
    })()

  useEffect(() => {
    if (!twoRevealedNoMatch) return
    const t = setTimeout(() => {
      setCards((prev) =>
        prev.map((c) =>
          c.state === 'revealed' ? { ...c, state: 'hidden' as CardState } : c
        )
      )
    }, 800)
    return () => clearTimeout(t)
  }, [twoRevealedNoMatch])

  const allMatched = cards.length > 0 && cards.every((c) => c.state === 'matched')

  useEffect(() => {
    if (phase !== 'playing' || !allMatched || completedRef.current) return
    completedRef.current = true
    setPhase('done')
    const totalPairs = (groups ?? []).reduce((s, g) => s + g.cards.length / 2, 0)
    const score = Math.max(0, Math.min(1, 1 - (moves - totalPairs) * 0.05))
    onComplete?.({
      score,
      subscores: [
        { label: 'Moves', value: moves },
        { label: 'Perfect', value: totalPairs },
      ],
    })
  }, [phase, allMatched, moves, groups, onComplete])

  const gridCols = (n: number) => {
    if (n <= 4) return 2
    if (n <= 8) return 4
    if (n <= 12) return 4
    return 4
  }

  if (phase === 'intro') {
    return (
      <div className="sumpair-intro">
        <p className="prompt">{exercise.prompt}</p>
        {showInstruction && (
          <p className="sumpair-instruction">
            Cards show numbers. Find pairs where <strong>first + static = second</strong>.
            {isMultiStatic &&
              ' Each static is colored — match only cards of the same color.'}
          </p>
        )}
        <button type="button" onClick={startGame} className="sumpair-start-btn" autoFocus>
          Start
        </button>
      </div>
    )
  }

  if (phase === 'done') {
    return (
      <div className="sumpair-done">
        <p className="sumpair-result">All pairs matched!</p>
        <p className="sumpair-stats">Total moves: {moves}</p>
      </div>
    )
  }

  return (
    <div className="sumpair-playing">
      <p className="exercise-complexity-watermark" aria-hidden="true">
        C {complexityScore}/100
      </p>
      <div className="sumpair-statics">
        {(groups ?? []).map((g) => (
          <span
            key={g.static}
            className="sumpair-static-badge"
            style={{ backgroundColor: g.color }}
          >
            +{g.static}
          </span>
        ))}
      </div>
      <div
        className="sumpair-grid"
        style={{ gridTemplateColumns: `repeat(${gridCols(cards.length)}, 1fr)` }}
      >
        {cards.map((card, index) => (
          <button
            key={`${index}-${card.value}-${card.staticK}`}
            type="button"
            className={`sumpair-card ${card.state}`}
            style={
              card.state !== 'hidden'
                ? { borderColor: card.color, color: card.color }
                : undefined
            }
            data-color={card.color}
            onClick={() => handleCardClick(index)}
            disabled={
              card.state === 'matched' ||
              (flippedIndices.length >= 2 && card.state === 'hidden')
            }
          >
            {card.state === 'hidden' ? '?' : card.value}
          </button>
        ))}
      </div>
      <p className="sumpair-moves">Moves: {moves}</p>
    </div>
  )
}

function shuffleArray<T>(arr: T[]): T[] {
  const out = [...arr]
  for (let i = out.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[out[i], out[j]] = [out[j], out[i]]
  }
  return out
}

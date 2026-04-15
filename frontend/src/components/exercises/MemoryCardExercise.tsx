import { useState, useMemo, useCallback, useEffect, useRef } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'

export interface MemoryCardExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
  showInstruction?: boolean
}

type CardState = 'hidden' | 'revealed' | 'matched'

interface Card {
  id: number
  symbol: string
  state: CardState
}

/**
 * Memory card game: display n pairs of cards face-down; user flips two at a time to find pairs.
 * Deck is built from exercise.memoryCardParams (pairCount + symbols), shuffled once.
 */
export function MemoryCardExercise({ exercise, onComplete, showInstruction = true }: MemoryCardExerciseProps) {
  const params = exercise.memoryCardParams
  if (!params) {
    return <p className="error">Invalid memory card exercise: missing params.</p>
  }

  const [phase, setPhase] = useState<'intro' | 'playing' | 'done'>('intro')
  const [cards, setCards] = useState<Card[]>([])
  const [moves, setMoves] = useState(0)
  const completedRef = useRef(false)

  const deck = useMemo(() => {
    if (params.shuffledDeck?.length === params.pairCount * 2) {
      return params.shuffledDeck.map((symbol, i) => ({
        id: i,
        symbol,
        state: 'hidden' as CardState,
      }))
    }
    const pairs = params.symbols.flatMap((s, i) => [
      { id: i * 2, symbol: s, state: 'hidden' as CardState },
      { id: i * 2 + 1, symbol: s, state: 'hidden' as CardState },
    ])
    return shuffle([...pairs])
  }, [params.symbols, params.shuffledDeck, params.pairCount])

  const startGame = useCallback(() => {
    setCards(deck)
    setPhase('playing')
    setMoves(0)
    completedRef.current = false
  }, [deck])

  const flippedIndices = useMemo(
    () => cards.map((c, i) => (c.state === 'revealed' ? i : -1)).filter((i) => i >= 0),
    [cards]
  )

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
        const nowRevealed = next
          .map((c, i) => (c.state === 'revealed' ? i : -1))
          .filter((i) => i >= 0)
        if (nowRevealed.length === 2) {
          setMoves((m) => m + 1)
          const [a, b] = nowRevealed
          const match = next[a].symbol === next[b].symbol
          if (match) {
            return next.map((c, i) =>
              i === a || i === b ? { ...c, state: 'matched' as CardState } : c
            )
          }
          return next
        }
        return next
      })
    },
    [phase, cards, flippedIndices.length]
  )

  const twoRevealedNoMatch =
    flippedIndices.length === 2 &&
    cards[flippedIndices[0]]?.symbol !== cards[flippedIndices[1]]?.symbol

  useEffect(() => {
    if (!twoRevealedNoMatch) return
    const t = setTimeout(() => {
      setCards((prev) =>
        prev.map((c) => (c.state === 'revealed' ? { ...c, state: 'hidden' as CardState } : c))
      )
    }, 800)
    return () => clearTimeout(t)
  }, [twoRevealedNoMatch])

  const allMatched = cards.length > 0 && cards.every((c) => c.state === 'matched')

  useEffect(() => {
    if (phase !== 'playing' || !allMatched || completedRef.current) return
    completedRef.current = true
    setPhase('done')
    const pairCount = params.pairCount
    const perfectMoves = pairCount
    const score = Math.max(0, Math.min(1, 1 - (moves - perfectMoves) * 0.05))
    onComplete?.({
      score,
      subscores: [
        { label: 'Moves', value: moves },
        { label: 'Perfect', value: perfectMoves },
      ],
    })
  }, [phase, allMatched, moves, params.pairCount, onComplete])

  if (phase === 'intro') {
    return (
      <div className="memory-intro">
        <p className="prompt">{exercise.prompt}</p>
        {showInstruction && (
          <p className="memory-instruction">
            You will see {params.pairCount * 2} cards. Find the {params.pairCount} pairs by flipping
            two cards at a time.
          </p>
        )}
        <button type="button" onClick={startGame} className="memory-start-btn" autoFocus>
          Start
        </button>
      </div>
    )
  }

  if (phase === 'done') {
    return (
      <div className="memory-done">
        <p className="memory-result">All pairs found!</p>
        <p className="memory-stats">Moves: {moves}</p>
      </div>
    )
  }

  return (
    <div className="memory-playing">
      <div
        className="memory-grid"
        style={{
          gridTemplateColumns: `repeat(${gridCols(cards.length)}, 1fr)`,
        }}
      >
        {cards.map((card, index) => (
          <button
            key={`${card.id}-${index}`}
            type="button"
            className={`memory-card ${card.state}`}
            onClick={() => handleCardClick(index)}
            disabled={
              card.state === 'matched' ||
              (flippedIndices.length >= 2 && card.state === 'hidden')
            }
          >
            {card.state === 'hidden' ? '?' : card.symbol}
          </button>
        ))}
      </div>
      <p className="memory-moves">Moves: {moves}</p>
    </div>
  )
}

function shuffle<T>(arr: T[]): T[] {
  const out = [...arr]
  for (let i = out.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [out[i], out[j]] = [out[j], out[i]]
  }
  return out
}

function gridCols(totalCards: number): number {
  if (totalCards <= 4) return 2
  if (totalCards <= 6) return 3
  if (totalCards <= 12) return 4
  return 4
}

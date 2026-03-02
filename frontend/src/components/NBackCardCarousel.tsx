/**
 * N-Back card carousel: 2n+1 slots, center spaced.
 * Flow:
 * 0. All backs, center spaced out
 * 1. After 0.5s: center flips to face (shows current card)
 * 2. After 1s: center flips to back, moves right; right shift; rightmost exits
 * 3. Left-neighbor of center (end of left queue) flips to face, moves to center with next card;
 *    new card enters at start of left queue (furthest left)
 */
import { useEffect, useState, useRef, useCallback } from 'react'
import { NBackCardDisplay } from './NBackCardDisplay'

const SLOT_WIDTH = 90
const INITIAL_DELAY_MS = 500
const FACE_DISPLAY_MS = 1000
const MOVE_MS = 450
const FLIP_MS = 320

function CardBack() {
  return <div className="nback-card-back" />
}

function CardFace({ code }: { code: string }) {
  return (
    <div className="nback-card-face-wrapper">
      <NBackCardDisplay code={code} size="medium" />
    </div>
  )
}

type Phase =
  | 'init'      // step 0: all backs
  | 'flipFace'  // step 1: center flips to face
  | 'showFace'  // face visible
  | 'moveOut'   // step 2: center→back, move right, shift, exit
  | 'moveIn'    // step 3: leftmost→center (flip+move), left shift, new enter

interface CardItem {
  id: string
  code: string | null
  pos: number // visual position 0..2n (or -1 for entering, 2n+1 for exiting)
  showFace: boolean
  exiting?: boolean
  entering?: boolean
}

export interface NBackCardCarouselProps {
  n: number
  sequence: string[]
  onCardShow?: (index: number) => void
  onComplete?: () => void
}

export function NBackCardCarousel({
  n,
  sequence,
  onCardShow,
  onComplete,
}: NBackCardCarouselProps) {
  const slotCount = 2 * n + 1
  const centerIdx = n
  const [, setPhase] = useState<Phase>('init')
  const [cards, setCards] = useState<CardItem[]>([])
  const seqIdxRef = useRef(0)
  const idRef = useRef(0)
  const sequenceRef = useRef(sequence)
  const onCardShowRef = useRef(onCardShow)
  const onCompleteRef = useRef(onComplete)
  sequenceRef.current = sequence
  onCardShowRef.current = onCardShow
  onCompleteRef.current = onComplete

  const advance = useCallback(() => {
    if (seqIdxRef.current >= sequenceRef.current.length) {
      onCompleteRef.current?.()
      return
    }
    // Notify only for the first card here; subsequent cards are notified in t3 when they appear in center
    if (seqIdxRef.current === 0) {
      onCardShowRef.current?.(seqIdxRef.current)
    }

    setPhase('flipFace')
    setCards((prev) =>
      prev.map((c, i) => (i === centerIdx ? { ...c, showFace: true } : c))
    )

    // After flip finishes → showFace
    const t1 = setTimeout(() => setPhase('showFace'), FLIP_MS)

    // After showFace duration → moveOut
    const t2 = setTimeout(() => {
      setPhase('moveOut')
      setCards((prev) => {
        const centerCard = prev[centerIdx]
        return prev.map((c, i) => {
          if (i < centerIdx) return c
          if (i === centerIdx) return { ...centerCard, pos: centerIdx + 1, showFace: false }
          if (i < slotCount - 1) return { ...c, pos: c.pos + 1 }
          return { ...c, pos: slotCount + 2, exiting: true }
        })
      })
    }, FLIP_MS + FACE_DISPLAY_MS)

    // After moveOut → moveIn: left-neighbor-of-center flips & moves to center; new card enters at left
    const t3 = setTimeout(() => {
      seqIdxRef.current += 1
      if (seqIdxRef.current >= sequenceRef.current.length) {
        setPhase('init')
        onCompleteRef.current?.()
        return
      }
      const nextCode = sequenceRef.current[seqIdxRef.current]
      // Notify parent as soon as the new card is shown in center so button/scoring stays in sync
      onCardShowRef.current?.(seqIdxRef.current)
      setCards((prev) => {
        const withoutExit = prev.filter((c) => !c.exiting)
        // Card nearest to center on the LEFT (end of left queue) flips and moves to center
        const leftNeighborOfCenter = withoutExit[centerIdx - 1]
        const leftOfThat = withoutExit.slice(0, centerIdx - 1)
        const rightGroup = withoutExit.slice(centerIdx)
        const newCard: CardItem = {
          id: `new-${++idRef.current}`,
          code: null,
          pos: -1,
          showFace: false,
          entering: true,
        }
        const updated: CardItem[] = [
          newCard,
          ...leftOfThat.map((c, i) => ({ ...c, pos: i + 1 })),
          { ...leftNeighborOfCenter, pos: centerIdx, showFace: true, code: nextCode },
          ...rightGroup.map((c, i) => ({ ...c, pos: centerIdx + 1 + i })),
        ]
        return updated
      })
      setPhase('moveIn')
    }, FLIP_MS + FACE_DISPLAY_MS + MOVE_MS)

    // After moveIn → settle; wait FACE_DISPLAY_MS then next cycle
    const t4 = setTimeout(() => {
      setCards((prev) =>
        prev.map((c) => ({
          ...c,
          pos: c.entering ? 0 : c.pos,
          entering: false,
        }))
      )
      setPhase('init')
    }, FLIP_MS + FACE_DISPLAY_MS + MOVE_MS * 2)

    const t5 = setTimeout(() => {
      if (seqIdxRef.current >= sequenceRef.current.length) {
        onCompleteRef.current?.()
        return
      }
      advance()
    }, FLIP_MS + FACE_DISPLAY_MS + MOVE_MS * 2 + FACE_DISPLAY_MS)

    return () => {
      clearTimeout(t1)
      clearTimeout(t2)
      clearTimeout(t3)
      clearTimeout(t4)
      clearTimeout(t5)
    }
  }, [slotCount, centerIdx])

  useEffect(() => {
    if (sequence.length === 0) return
    const init: CardItem[] = Array.from({ length: slotCount }, (_, i) => ({
      id: `card-${++idRef.current}`,
      code: i === centerIdx ? sequence[0] : null,
      pos: i,
      showFace: false,
    }))
    setCards(init)
    seqIdxRef.current = 0
    setPhase('init')

    const t = setTimeout(() => advance(), INITIAL_DELAY_MS)
    return () => clearTimeout(t)
  }, [sequence, slotCount, centerIdx, advance])

  if (cards.length === 0) return null

  const centerSpacing = 28

  return (
    <div
      className="nback-carousel-v2"
      style={{ width: slotCount * SLOT_WIDTH + centerSpacing * 2 }}
    >
      <div className="nback-carousel-track-v2">
        {cards.map((card) => {
          const off =
            card.pos === centerIdx
              ? centerSpacing
              : card.pos > centerIdx
                ? centerSpacing * 2
                : 0
          const left = card.pos * SLOT_WIDTH + off
          return (
            <div
              key={card.id}
              className={`nback-carousel-card-v2 ${
                card.exiting ? 'nback-card-exit' : ''
              } ${card.entering ? 'nback-card-enter' : ''}`}
              style={{
                left: `${left}px`,
                transition: `left ${MOVE_MS}ms cubic-bezier(0.25, 0.46, 0.45, 0.94), opacity 0.4s ease-out`,
                opacity: card.entering && card.pos < 0 ? 0 : 1,
              }}
            >
              <div
                className={`nback-flip-card-v2 ${
                  card.showFace ? 'nback-flip-face' : 'nback-flip-back'
                }`}
                style={{ transitionDuration: `${FLIP_MS}ms` }}
              >
                <div className="nback-flip-inner-v2">
                  <div className="nback-flip-front-v2">
                    {card.code ? (
                      <CardFace code={card.code} />
                    ) : (
                      <CardBack />
                    )}
                  </div>
                  <div className="nback-flip-back-side-v2">
                    <CardBack />
                  </div>
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

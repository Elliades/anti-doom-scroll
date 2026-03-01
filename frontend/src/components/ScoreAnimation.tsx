import { useEffect, useState } from 'react'
import type { SubscoreDetail } from '../types/exercise'

export interface ScoreAnimationProps {
  /** Score from 0 to 1 (displayed as 0–100%) */
  score: number
  /** Elapsed time in milliseconds */
  elapsedMs?: number
  /** Exercise-specific subscore details (e.g. Hits: 5/6, Moves: 12) */
  subscores?: SubscoreDetail[]
  /** @deprecated Use subscores instead */
  detail?: string
  className?: string
}

function formatElapsed(ms: number): string {
  const totalSeconds = Math.floor(ms / 1000)
  const m = Math.floor(totalSeconds / 60)
  const s = totalSeconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

function getResultMessage(score: number): string {
  if (score >= 0.9) return 'Excellent!'
  if (score >= 0.7) return 'Well done!'
  if (score >= 0.5) return 'Good effort!'
  return 'Keep practicing!'
}

/**
 * Animated score display at the end of an exercise.
 * Counts up from 0 to the score with a scale-in animation.
 */
export function ScoreAnimation({
  score,
  elapsedMs,
  subscores,
  detail,
  className,
}: ScoreAnimationProps) {
  const [displayScore, setDisplayScore] = useState(0)
  const scorePercent = Math.round(score * 100)

  useEffect(() => {
    setDisplayScore(0)
    const duration = 800
    const start = performance.now()
    const step = (now: number) => {
      const elapsed = now - start
      const t = Math.min(elapsed / duration, 1)
      const eased = 1 - (1 - t) ** 2
      setDisplayScore(Math.round(eased * scorePercent))
      if (t < 1) requestAnimationFrame(step)
    }
    const id = requestAnimationFrame(step)
    return () => cancelAnimationFrame(id)
  }, [scorePercent])

  const details = subscores?.length
    ? subscores
    : detail
      ? [{ label: '', value: detail }]
      : []

  return (
    <div className={`score-animation ${className ?? ''}`.trim()}>
      <p className="score-animation-result">{getResultMessage(score)}</p>
      <div className="score-animation-circle">
        <span className="score-animation-value">{displayScore}%</span>
      </div>
      {elapsedMs != null && (
        <p className="score-animation-time">Time: {formatElapsed(elapsedMs)}</p>
      )}
      {details.length > 0 && (
        <ul className="score-animation-subscores">
          {details.map((s, i) => (
            <li key={i}>
              {s.label && <span className="score-subscore-label">{s.label}: </span>}
              <span className="score-subscore-value">{s.value}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

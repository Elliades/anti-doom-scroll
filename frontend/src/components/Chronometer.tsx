export interface ChronometerProps {
  /** Elapsed time in milliseconds to display */
  elapsedMs: number
  className?: string
}

/**
 * Generic chronometer displaying elapsed time as MM:SS.
 * Parent controls the value (e.g. via setInterval) for accurate capture on complete.
 */
export function Chronometer({ elapsedMs, className }: ChronometerProps) {
  const totalSeconds = Math.floor(elapsedMs / 1000)
  const m = Math.floor(totalSeconds / 60)
  const s = totalSeconds % 60
  const display = `${m}:${s.toString().padStart(2, '0')}`

  return (
    <span className={className} aria-label="Elapsed time">
      <span className="chronometer-icon" aria-hidden>⏱</span>
      {display}
    </span>
  )
}

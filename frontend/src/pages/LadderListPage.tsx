import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listLadders, listLadderMixes } from '../api/ladder'
import type { LadderSummaryDto, LadderMixSummaryDto } from '../api/ladder'

const LADDER_ICONS: Record<string, string> = {
  default: '🎯',
  sum: '🔢',
  anagram: '🔤',
  pair: '🃏',
  nback: '🧠',
  estimation: '📐',
}

/**
 * Lists all available ladders and ladder mixes fetched from API.
 * Each ladder links to /ladder/:code; each mix links to /ladder/mix/:mixCode.
 */
export function LadderListPage() {
  const [ladders, setLadders] = useState<LadderSummaryDto[]>([])
  const [mixes, setMixes] = useState<LadderMixSummaryDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    Promise.all([listLadders(), listLadderMixes()])
      .then(([ladderData, mixData]) => {
        if (!cancelled) {
          setLadders(ladderData)
          setMixes(mixData)
        }
      })
      .catch((e) => !cancelled && setError(e.message))
      .finally(() => !cancelled && setLoading(false))
    return () => { cancelled = true }
  }, [])

  if (loading) return <div className="screen center"><p className="pulse">Loading…</p></div>
  if (error) {
    return (
      <div className="screen center">
        <p className="error">{error}</p>
        <Link to="/">← Back</Link>
      </div>
    )
  }

  return (
    <div className="screen">
      <header className="header">
        <Link to="/" className="back-link">← App</Link>
        <h1>Ladders</h1>
      </header>
      <main className="main">
        <p className="muted" style={{ marginBottom: '1rem' }}>
          Climb levels by scoring well. Advance at ≥75%, stay at 40–75%, demote below 40%.
        </p>

        {mixes.length > 0 && (
          <ul className="subject-list" style={{ marginBottom: '1.5rem' }}>
            {mixes.map((m) => (
              <li key={m.code}>
                <Link to={`/ladder/mix/${m.code}`}>
                  🔀 {m.name ?? 'Ladder Mix'}
                </Link>
                <span className="muted"> — all ladders, pass each to advance</span>
              </li>
            ))}
          </ul>
        )}

        <h2 className="ladder-section-title">Single Ladders</h2>
        <ul className="subject-list">
          {ladders.map((l) => (
            <li key={l.code}>
              <Link to={`/ladder/${l.code}`}>
                {LADDER_ICONS[l.code] ?? '🪜'} {l.name ?? l.code}
              </Link>
              <span className="muted"> — {l.levelCount} levels</span>
            </li>
          ))}
        </ul>
        {ladders.length === 0 && mixes.length === 0 && <p className="muted">No ladders configured.</p>}
      </main>
    </div>
  )
}

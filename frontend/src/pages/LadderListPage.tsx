import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listLadders } from '../api/ladder'
import type { LadderSummaryDto } from '../api/ladder'

const LADDER_ICONS: Record<string, string> = {
  default: '🎯',
  sum: '🔢',
  anagram: '🔤',
  pair: '🃏',
  nback: '🧠',
}

/**
 * Lists all available ladders fetched from GET /api/ladders.
 * Each ladder links to /ladder/:code to start a session.
 */
export function LadderListPage() {
  const [ladders, setLadders] = useState<LadderSummaryDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    listLadders()
      .then((data) => !cancelled && setLadders(data))
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
        {ladders.length === 0 && <p className="muted">No ladders configured.</p>}
      </main>
    </div>
  )
}

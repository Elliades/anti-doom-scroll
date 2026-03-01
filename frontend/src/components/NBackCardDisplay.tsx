/**
 * Renders a single playing card for N-Back, using CSS Playing Cards.
 * Card codes: "AC" (Ace Clubs), "2D" (2 Diamonds), "10H" (10 Hearts), "KS" (King Spades).
 * Suits: C=clubs, D=diams, H=hearts, S=spades
 */
const SUIT_MAP: Record<string, string> = { C: 'clubs', D: 'diams', H: 'hearts', S: 'spades' }
const RANK_MAP: Record<string, string> = {
  A: 'a', '2': '2', '3': '3', '4': '4', '5': '5', '6': '6',
  '7': '7', '8': '8', '9': '9', '10': '10', T: '10', J: 'j', Q: 'q', K: 'k'
}

const RANK_DISPLAY: Record<string, string> = {
  a: 'A', '2': '2', '3': '3', '4': '4', '5': '5', '6': '6',
  '7': '7', '8': '8', '9': '9', '10': '10', j: 'J', q: 'Q', k: 'K'
}

function parseCardCode(code: string): { rank: string; suit: string } | null {
  if (!code || code.length < 2) return null
  const upper = code.toUpperCase()
  let rank: string
  let suitChar: string
  if (upper.startsWith('10') && upper.length >= 3) {
    rank = '10'
    suitChar = upper[2]
  } else if (upper.length >= 2) {
    rank = upper[0] === 'T' ? '10' : RANK_MAP[upper[0]] ?? upper[0]
    suitChar = upper[upper.length - 1]
  } else {
    return null
  }
  const suit = SUIT_MAP[suitChar]
  if (!suit || !rank) return null
  return { rank: rank.toLowerCase(), suit }
}

export function isCardCode(s: string): boolean {
  return parseCardCode(s) !== null
}

export function NBackCardDisplay({ code, size = 'large' }: { code: string; size?: 'large' | 'medium' }) {
  const parsed = parseCardCode(code)
  if (!parsed) return <span className="nback-letter">{code}</span>

  const { rank, suit } = parsed
  const rankClass = `rank-${rank}`
  const rankDisplay = RANK_DISPLAY[rank] ?? rank

  return (
    <div className={`playingCards simpleCards nback-cards nback-cards-${size}`}>
      <span className={`card ${rankClass} ${suit}`}>
        <span className="rank">{rankDisplay}</span>
        {/* Suit symbol rendered by CSS :after - leave empty to avoid overlap */}
        <span className="suit" />
      </span>
    </div>
  )
}

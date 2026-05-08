interface ComplexityBadgeProps {
  score: number
}

export function ComplexityBadge({ score }: ComplexityBadgeProps) {
  return (
    <p className="complexity-badge" aria-label="Complexity score">
      Cx {Math.round(score)}
    </p>
  )
}

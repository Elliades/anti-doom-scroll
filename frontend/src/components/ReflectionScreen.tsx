import type { ReflectionContentDto } from '../types/api'

interface ReflectionScreenProps {
  content: ReflectionContentDto
  onContinue: () => void
}

export function ReflectionScreen({ content, onContinue }: ReflectionScreenProps) {
  return (
    <>
      <header className="header">
        <span className="badge">Reflection</span>
      </header>
      <main className="main">
        <h2 className="reflection-title">{content.title}</h2>
        <p className="reflection-body">{content.body}</p>
      </main>
      <footer className="footer">
        <button onClick={onContinue}>Continue</button>
      </footer>
    </>
  )
}

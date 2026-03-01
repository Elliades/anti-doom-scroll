import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listSubjects } from '../api/subjects'
import type { SubjectDto } from '../api/subjects'

/**
 * List all subjects. Each subject links to /subjects/:code to pick an exercise.
 */
export function SubjectsListPage() {
  const [subjects, setSubjects] = useState<SubjectDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    listSubjects()
      .then((data) => !cancelled && setSubjects(data))
      .catch((e) => !cancelled && setError(e.message))
      .finally(() => !cancelled && setLoading(false))
    return () => { cancelled = true }
  }, [])

  if (loading) return <div className="screen center"><p className="pulse">Loading…</p></div>
  if (error) {
    return (
      <div className="screen center">
        <p className="error">{error}</p>
        <Link to="/">Back to app</Link>
      </div>
    )
  }

  return (
    <div className="screen">
      <header className="header">
        <Link to="/" className="back-link">← App</Link>
        <h1>Subjects</h1>
        <Link to="/exercises" className="subjects-link">All exercises</Link>
      </header>
      <main className="main">
        <ul className="subject-list">
          {subjects.map((s) => (
            <li key={s.id}>
              <Link to={`/subjects/${s.code}`}>{s.name}</Link>
              {s.description && <span className="muted"> — {s.description}</span>}
            </li>
          ))}
        </ul>
        {subjects.length === 0 && <p className="muted">No subjects yet.</p>}
      </main>
    </div>
  )
}

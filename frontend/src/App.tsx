import { useCallback, useEffect, useRef, useState } from 'react'
import { getJourney, getJourneyStepContent } from './api/journey'
import type { JourneyDto, JourneyStepContentDto } from './types/api'
import { SessionExerciseBlock } from './components/SessionExerciseBlock'
import { ReflectionScreen } from './components/ReflectionScreen'
import './App.css'

const JOURNEY_CODE = 'default'

function useQueryParam(name: string): string | null {
  if (typeof window === 'undefined') return null
  return new URLSearchParams(window.location.search).get(name)
}

function useJourneyStepFromUrl(): number {
  const step = useQueryParam('step')
  const n = step != null ? parseInt(step, 10) : NaN
  return Number.isFinite(n) && n >= 0 ? n : 0
}

function updateUrlStep(stepIndex: number, chapterIndex?: number) {
  if (typeof window === 'undefined') return
  const params = new URLSearchParams(window.location.search)
  params.set('step', String(stepIndex))
  if (chapterIndex != null) params.set('chapterIndex', String(chapterIndex))
  const url = `${window.location.pathname}?${params.toString()}`
  window.history.replaceState(null, '', url)
}

function App() {
  const [journey, setJourney] = useState<JourneyDto | null>(null)
  const [stepContent, setStepContent] = useState<JourneyStepContentDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [profileId, setProfileId] = useState<string | null>(null)
  const [elapsedSeconds, setElapsedSeconds] = useState(0)
  const sessionStartRef = useRef<number | null>(null)

  const urlStep = useJourneyStepFromUrl()
  const urlChapter = useQueryParam('chapterIndex')
  const initialChapter = (() => {
    const n = urlChapter != null ? parseInt(urlChapter, 10) : 0
    return Number.isFinite(n) && n >= 0 ? n : 0
  })()
  const [journeyStepIndex, setJourneyStepIndex] = useState(urlStep)
  const [chapterIndex, setChapterIndex] = useState(initialChapter)

  const syncStepFromUrl = useCallback(() => {
    const step = useQueryParam('step')
    const n = step != null ? parseInt(step, 10) : NaN
    const idx = Number.isFinite(n) && n >= 0 ? n : 0
    setJourneyStepIndex(idx)
    const c = useQueryParam('chapterIndex')
    const cn = c != null ? parseInt(c, 10) : 0
    setChapterIndex(Number.isFinite(cn) && cn >= 0 ? cn : 0)
  }, [])

  useEffect(() => {
    let cancelled = false
    getJourney(JOURNEY_CODE)
      .then((data) => {
        if (!cancelled) setJourney(data)
      })
      .catch((e) => !cancelled && setError(e.message))
      .finally(() => !cancelled && setLoading(false))
    return () => { cancelled = true }
  }, [])

  useEffect(() => {
    syncStepFromUrl()
  }, [syncStepFromUrl])

  useEffect(() => {
    if (!journey || journey.steps.length === 0) return
    const stepIndex = Math.min(journeyStepIndex, journey.steps.length - 1)
    if (stepIndex !== journeyStepIndex) {
      setJourneyStepIndex(stepIndex)
      updateUrlStep(stepIndex)
      return
    }
    let cancelled = false
    sessionStartRef.current = Date.now()
    setElapsedSeconds(0)
    getJourneyStepContent(stepIndex, {
      journeyCode: JOURNEY_CODE,
      profileId: profileId ?? undefined,
      chapterIndex,
    })
      .then((content) => {
        if (!cancelled) {
          setStepContent(content)
          if (content.session?.profileId) setProfileId(content.session.profileId)
        }
      })
      .catch((e) => !cancelled && setError(e.message))
    return () => { cancelled = true }
  }, [journey, journeyStepIndex, chapterIndex, profileId])

  useEffect(() => {
    if (!stepContent) return
    const interval = setInterval(() => {
      if (sessionStartRef.current != null) {
        setElapsedSeconds(Math.floor((Date.now() - sessionStartRef.current) / 1000))
      }
    }, 1000)
    return () => clearInterval(interval)
  }, [stepContent])

  const goToStep = useCallback((stepIndex: number, chapIndex?: number) => {
    setJourneyStepIndex(stepIndex)
    if (chapIndex != null) setChapterIndex(chapIndex)
    updateUrlStep(stepIndex, chapIndex)
    setStepContent(null)
    setError(null)
  }, [])

  const onStepDone = useCallback(() => {
    if (!journey) return
    const content = stepContent
    if (content?.type === 'CHAPTER_EXERCISES' && content.chapterSeries) {
      const { chapters, currentChapterIndex } = content.chapterSeries
      if (currentChapterIndex < chapters.length - 1) {
        goToStep(journeyStepIndex, currentChapterIndex + 1)
        return
      }
    }
    if (journeyStepIndex < journey.steps.length - 1) {
      goToStep(journeyStepIndex + 1)
    } else {
      goToStep(0)
    }
  }, [journey, journeyStepIndex, stepContent, goToStep])

  if (loading) return <div className="screen center"><p className="pulse">Loading…</p></div>
  if (error) return <div className="screen center"><p className="error">{error}</p></div>
  if (!journey || journey.steps.length === 0) {
    return <div className="screen center"><p>No journey configured.</p></div>
  }
  if (!stepContent) return <div className="screen center"><p className="pulse">Loading step…</p></div>

  const stepLabel = journey.steps[journeyStepIndex]?.type.replace(/_/g, ' ') ?? ''

  if (stepContent.type === 'OPEN_APP' && stepContent.session) {
    return (
      <div className="screen">
        <SessionExerciseBlock
          session={stepContent.session}
          profileId={stepContent.session.profileId}
          elapsedSeconds={elapsedSeconds}
          onDone={onStepDone}
          stepLabel={stepLabel || 'Warm-up'}
        />
        <div className="journey-nav">
          {journey.steps.map((_, i) => (
            <button
              key={i}
              type="button"
              className={i === journeyStepIndex ? 'active' : ''}
              onClick={() => goToStep(i)}
            >
              Step {i + 1}
            </button>
          ))}
        </div>
      </div>
    )
  }

  if (stepContent.type === 'REFLECTION' && stepContent.reflection) {
    return (
      <div className="screen">
        <ReflectionScreen content={stepContent.reflection} onContinue={onStepDone} />
        <div className="journey-nav">
          {journey.steps.map((_, i) => (
            <button
              key={i}
              type="button"
              className={i === journeyStepIndex ? 'active' : ''}
              onClick={() => goToStep(i)}
            >
              Step {i + 1}
            </button>
          ))}
        </div>
      </div>
    )
  }

  if (stepContent.type === 'CHAPTER_EXERCISES' && stepContent.chapterSeries) {
    const { chapters, currentChapterIndex, session } = stepContent.chapterSeries
    const chapterCode = chapters[currentChapterIndex] ?? ''
    return (
      <div className="screen">
        {session ? (
          <>
            <p className="chapter-label">Chapter: {chapterCode}</p>
            <SessionExerciseBlock
              session={session}
              profileId={session.profileId}
              elapsedSeconds={elapsedSeconds}
              onDone={onStepDone}
              stepLabel={`Chapter ${currentChapterIndex + 1}/${chapters.length}`}
            />
          </>
        ) : (
          <>
            <p className="step">No exercises for this chapter.</p>
            <footer className="footer">
              <button onClick={onStepDone}>Continue</button>
            </footer>
          </>
        )}
        <div className="journey-nav">
          {journey.steps.map((_, i) => (
            <button
              key={i}
              type="button"
              className={i === journeyStepIndex ? 'active' : ''}
              onClick={() => goToStep(i)}
            >
              Step {i + 1}
            </button>
          ))}
        </div>
      </div>
    )
  }

  return <div className="screen center"><p>Unknown step type.</p></div>
}

export default App

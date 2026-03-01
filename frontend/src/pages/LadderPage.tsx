import { LadderSessionBlock } from '../components/LadderSessionBlock'

export interface LadderPageProps {
  /** Ladder config code: default, sum, etc. */
  ladderCode?: string
}

export function LadderPage({ ladderCode = 'default' }: LadderPageProps) {
  return <LadderSessionBlock ladderCode={ladderCode} />
}

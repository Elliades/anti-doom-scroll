import { useParams } from 'react-router-dom'
import { LadderMixSessionBlock } from '../components/LadderMixSessionBlock'

export function LadderMixPage() {
  const { mixCode = 'mix' } = useParams<{ mixCode: string }>()
  return <LadderMixSessionBlock mixCode={mixCode} />
}

import { useParams } from 'react-router-dom'
import { LadderSessionBlock } from '../components/LadderSessionBlock'

export function LadderPage() {
  const { code = 'default' } = useParams<{ code: string }>()
  return <LadderSessionBlock ladderCode={code} />
}

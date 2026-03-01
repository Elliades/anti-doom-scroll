import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { JourneyPage } from './pages/JourneyPage'
import { LadderPage } from './pages/LadderPage'
import { SubjectsListPage } from './pages/SubjectsListPage'
import { SubjectDetailPage } from './pages/SubjectDetailPage'
import { ExercisesListPage } from './pages/ExercisesListPage'
import { PlayExercisePage } from './pages/PlayExercisePage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<JourneyPage />} />
        <Route path="/ladder" element={<LadderPage />} />
        <Route path="/ladder/sum" element={<LadderPage ladderCode="sum" />} />
        <Route path="/subjects" element={<SubjectsListPage />} />
        <Route path="/subjects/:code" element={<SubjectDetailPage />} />
        <Route path="/exercises" element={<ExercisesListPage />} />
        <Route path="/play/:exerciseId" element={<PlayExercisePage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App

package app.antidoomscroll.infrastructure.persistence

import app.antidoomscroll.domain.SubjectScoringConfig
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Seeds subjects and exercises when running with profile `local` (H2 in-memory).
 * Flyway is disabled for local, so no migrations run — this ensures /api/subjects and /api/exercises return data.
 */
@Component
@Profile("local")
@Order(1)
class LocalDataSeeder(
    private val subjectRepository: SubjectJpaRepository,
    private val exerciseRepository: ExerciseJpaRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        if (subjectRepository.count() > 0L) return // already seeded (e.g. tests)

        val defaultScoring = SubjectScoringConfig(
            accuracyType = SubjectScoringConfig.AccuracyType.BINARY,
            speedTargetMs = 10_000L,
            confidenceWeight = 0.0,
            streakBonusCap = 0.1
        )
        val emptyScoring = SubjectScoringConfig()

        val subjectDefault = SubjectEntity().apply {
            id = UUID.fromString("b0000000-0000-0000-0000-000000000001")
            code = "default"
            name = "Default"
            description = "Default subject for general exercises"
            scoringConfig = defaultScoring
        }
        val subjectB1 = SubjectEntity().apply {
            id = UUID.fromString("b0000000-0000-0000-0000-000000000004")
            code = "B1"
            name = "N-back"
            description = "Working memory: n-back"
            scoringConfig = emptyScoring
        }
        val subjectMemory = SubjectEntity().apply {
            id = UUID.fromString("b0000000-0000-0000-0000-000000000008")
            code = "MEMORY"
            name = "Memory"
            description = "Memory games: find matching pairs"
            scoringConfig = defaultScoring
        }
        val subjectWord = SubjectEntity().apply {
            id = UUID.fromString("b0000000-0000-0000-0000-000000000010")
            code = "WORD"
            name = "Word"
            description = "Word games: anagrams and vocabulary."
            scoringConfig = defaultScoring
        }
        subjectRepository.saveAll(listOf(subjectDefault, subjectB1, subjectMemory, subjectWord))

        val defaultId = subjectDefault.id!!
        val b1Id = subjectB1.id!!
        val memoryId = subjectMemory.id!!
        val wordId = subjectWord.id!!

        val exercises = listOf(
            // Sum (FLASHCARD_QA ADD only): 4 exercises, one per difficulty
            // Opening: 1 digit + 1-2 digits (max 3 digits total)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000001")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "ULTRA_EASY"
                prompt = "Solve the addition."
                expectedAnswers = emptyList()
                timeLimitSeconds = 30
                exerciseParams = mapOf(
                    "operation" to "ADD",
                    "firstMax" to 9,
                    "secondMax" to 99
                )
            },
            // Easy: 1-2 digits + 1-2 digits
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000002")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "EASY"
                prompt = "Solve the addition."
                expectedAnswers = emptyList()
                timeLimitSeconds = 45
                exerciseParams = mapOf(
                    "operation" to "ADD",
                    "firstMax" to 99,
                    "secondMax" to 99
                )
            },
            // Medium: 2-3 digits + 2-3 digits
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000010")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "MEDIUM"
                prompt = "Solve the addition."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf(
                    "operation" to "ADD",
                    "firstMax" to 999,
                    "secondMax" to 999
                )
            },
            // Hard: 3-4 digits + 3-4 digits
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000011")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "HARD"
                prompt = "Solve the addition."
                expectedAnswers = emptyList()
                timeLimitSeconds = 90
                exerciseParams = mapOf(
                    "operation" to "ADD",
                    "firstMax" to 9999,
                    "secondMax" to 9999
                )
            },
            // Subtraction (FLASHCARD_QA SUBTRACT): same difficulty progression, non-negative results
            // ULTRA_EASY: 1–2 digit − 1 digit
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000020")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "ULTRA_EASY"
                prompt = "Solve the subtraction."
                expectedAnswers = emptyList()
                timeLimitSeconds = 30
                exerciseParams = mapOf(
                    "operation" to "SUBTRACT",
                    "firstMax" to 99,
                    "secondMax" to 9
                )
            },
            // EASY: 1–2 digits − 1–2 digits
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000021")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "EASY"
                prompt = "Solve the subtraction."
                expectedAnswers = emptyList()
                timeLimitSeconds = 45
                exerciseParams = mapOf(
                    "operation" to "SUBTRACT",
                    "firstMax" to 99,
                    "secondMax" to 99
                )
            },
            // MEDIUM: 2–3 digits − 2–3 digits
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000022")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "MEDIUM"
                prompt = "Solve the subtraction."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf(
                    "operation" to "SUBTRACT",
                    "firstMax" to 999,
                    "secondMax" to 999
                )
            },
            // HARD: 3–4 digits − 3–4 digits
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000023")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "HARD"
                prompt = "Solve the subtraction."
                expectedAnswers = emptyList()
                timeLimitSeconds = 90
                exerciseParams = mapOf(
                    "operation" to "SUBTRACT",
                    "firstMax" to 9999,
                    "secondMax" to 9999
                )
            },
            // Multiplication (FLASHCARD_QA MULTIPLY): difficulty by factor size and tables
            // ULTRA_EASY: 2, 5, or 10 × single digit (e.g. 2×7, 10×4)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000030")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "ULTRA_EASY"
                prompt = "Solve the multiplication."
                expectedAnswers = emptyList()
                timeLimitSeconds = 30
                exerciseParams = mapOf(
                    "operation" to "MULTIPLY",
                    "firstMin" to 1,
                    "firstMax" to 10,
                    "firstValues" to listOf(2, 5, 10),
                    "secondMin" to 1,
                    "secondMax" to 9
                )
            },
            // EASY: times tables — one digit × 1–12 (e.g. 7×8, 4×11)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000031")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "EASY"
                prompt = "Solve the multiplication."
                expectedAnswers = emptyList()
                timeLimitSeconds = 45
                exerciseParams = mapOf(
                    "operation" to "MULTIPLY",
                    "firstMin" to 1,
                    "firstMax" to 9,
                    "secondMin" to 1,
                    "secondMax" to 12
                )
            },
            // MEDIUM: two-digit × one-digit (e.g. 24×7, 56×3)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000032")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "MEDIUM"
                prompt = "Solve the multiplication."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf(
                    "operation" to "MULTIPLY",
                    "firstMin" to 10,
                    "firstMax" to 99,
                    "secondMin" to 1,
                    "secondMax" to 9
                )
            },
            // HARD: two-digit × two-digit (e.g. 24×17, 56×83)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000033")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "HARD"
                prompt = "Solve the multiplication."
                expectedAnswers = emptyList()
                timeLimitSeconds = 90
                exerciseParams = mapOf(
                    "operation" to "MULTIPLY",
                    "firstMin" to 10,
                    "firstMax" to 99,
                    "secondMin" to 10,
                    "secondMax" to 99
                )
            },
            // Division (FLASHCARD_QA DIVIDE): clean quotients only (dividend = divisor × quotient)
            // ULTRA_EASY: ÷ 2, 5, or 10 with quotient 1–9 (e.g. 20÷2, 45÷5, 30÷10)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000040")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "ULTRA_EASY"
                prompt = "Solve the division."
                expectedAnswers = emptyList()
                timeLimitSeconds = 30
                exerciseParams = mapOf(
                    "operation" to "DIVIDE",
                    "firstMin" to 1,
                    "firstMax" to 9,
                    "secondMax" to 10,
                    "secondValues" to listOf(2, 5, 10)
                )
            },
            // EASY: divisor 2–9, quotient 1–12 (times-table divisions, e.g. 56÷7, 36÷3)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000041")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "EASY"
                prompt = "Solve the division."
                expectedAnswers = emptyList()
                timeLimitSeconds = 45
                exerciseParams = mapOf(
                    "operation" to "DIVIDE",
                    "firstMin" to 1,
                    "firstMax" to 12,
                    "secondMin" to 2,
                    "secondMax" to 9
                )
            },
            // MEDIUM: divisor 1–9, quotient 1–99 (e.g. 432÷6, 891÷9)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000042")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "MEDIUM"
                prompt = "Solve the division."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf(
                    "operation" to "DIVIDE",
                    "firstMin" to 1,
                    "firstMax" to 99,
                    "secondMin" to 1,
                    "secondMax" to 9
                )
            },
            // HARD: two-digit ÷ two-digit (quotient and divisor 10–99, e.g. 576÷24)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000043")
                subjectId = defaultId
                type = "FLASHCARD_QA"
                difficulty = "HARD"
                prompt = "Solve the division."
                expectedAnswers = emptyList()
                timeLimitSeconds = 90
                exerciseParams = mapOf(
                    "operation" to "DIVIDE",
                    "firstMin" to 10,
                    "firstMax" to 99,
                    "secondMin" to 10,
                    "secondMax" to 99
                )
            },
            // N_BACK: parametric (n, suitCount). Easy: n=1, suitCount=1. Harder: n=2 suitCount=2; n=3 suitCount=4.
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000001")
                subjectId = b1Id
                type = "N_BACK"
                difficulty = "ULTRA_EASY"
                prompt = "1-Back: Tap when the card matches the previous one."
                expectedAnswers = emptyList()
                timeLimitSeconds = 30
                exerciseParams = mapOf("n" to 1, "suitCount" to 1)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000002")
                subjectId = b1Id
                type = "N_BACK"
                difficulty = "EASY"
                prompt = "2-Back: Tap when the card matches the one from 2 steps back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf("n" to 2, "suitCount" to 2)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000003")
                subjectId = b1Id
                type = "N_BACK"
                difficulty = "MEDIUM"
                prompt = "3-Back: Tap when the card matches the one from 3 steps back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf("n" to 3, "suitCount" to 4)
            },
            // N_BACK_GRID: 3x3 grid, position-based
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000010")
                subjectId = b1Id
                type = "N_BACK_GRID"
                difficulty = "ULTRA_EASY"
                prompt = "Grid 1-Back: Tap Match when the highlighted cell is the same as 1 step back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 45
                exerciseParams = mapOf(
                    "n" to 1,
                    "sequence" to listOf(0, 4, 2, 4, 8, 1, 8),
                    "matchIndices" to listOf(3, 6),
                    "gridSize" to 3
                )
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000011")
                subjectId = b1Id
                type = "N_BACK_GRID"
                difficulty = "EASY"
                prompt = "Grid 2-Back: Tap Match when the highlighted cell matches the one from 2 steps back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf(
                    "n" to 2,
                    "sequence" to listOf(0, 2, 4, 2, 6, 8, 4),
                    "matchIndices" to listOf(3, 6),
                    "gridSize" to 3
                )
            },
            // DUAL_NBACK_GRID: position + color
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000012")
                subjectId = b1Id
                type = "DUAL_NBACK_GRID"
                difficulty = "ULTRA_EASY"
                prompt = "Dual Grid 1-Back: Tap Match Position or Match Color when that attribute matches 1 step back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf(
                    "n" to 1,
                    "sequence" to listOf(
                        mapOf("position" to 0, "color" to "#4285F4"),
                        mapOf("position" to 4, "color" to "#EA4335"),
                        mapOf("position" to 0, "color" to "#FBBC04"),
                        mapOf("position" to 2, "color" to "#4285F4"),
                        mapOf("position" to 4, "color" to "#34A853")
                    ),
                    "matchPositionIndices" to listOf(2),
                    "matchColorIndices" to listOf(3),
                    "colors" to listOf("#4285F4", "#EA4335", "#FBBC04", "#34A853"),
                    "gridSize" to 3
                )
            },
            // DUAL_NBACK_CARD: suit + rank
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000013")
                subjectId = b1Id
                type = "DUAL_NBACK_CARD"
                difficulty = "ULTRA_EASY"
                prompt = "Dual Card 1-Back: Tap Match Color (suit) or Match Number (rank) when that matches 1 step back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf(
                    "n" to 1,
                    "sequence" to listOf("AC", "2D", "2C", "3H", "AS", "3S"),
                    "matchColorIndices" to listOf(5),
                    "matchNumberIndices" to listOf(2)
                )
            },
            // MEMORY_CARD_PAIRS
            ExerciseEntity().apply {
                id = UUID.fromString("d0000000-0000-0000-0000-000000000001")
                subjectId = memoryId
                type = "MEMORY_CARD_PAIRS"
                difficulty = "EASY"
                prompt = "Find all matching pairs. Flip two cards at a time."
                expectedAnswers = emptyList()
                timeLimitSeconds = 120
                exerciseParams = mapOf(
                    "pairCount" to 4,
                    "symbols" to listOf("🍎", "🍊", "🍋", "🍇")
                )
            },
            ExerciseEntity().apply {
                id = UUID.fromString("d0000000-0000-0000-0000-000000000002")
                subjectId = memoryId
                type = "MEMORY_CARD_PAIRS"
                difficulty = "MEDIUM"
                prompt = "Find all matching pairs. Flip two cards at a time."
                expectedAnswers = emptyList()
                timeLimitSeconds = 180
                exerciseParams = mapOf(
                    "pairCount" to 6,
                    "symbols" to listOf("🐶", "🐱", "🐰", "🐻", "🦊", "🐼")
                )
            },
            ExerciseEntity().apply {
                id = UUID.fromString("d0000000-0000-0000-0000-000000000003")
                subjectId = memoryId
                type = "MEMORY_CARD_PAIRS"
                difficulty = "ULTRA_EASY"
                prompt = "Find the 3 matching pairs."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf(
                    "pairCount" to 3,
                    "symbols" to listOf("⭐", "❤️", "🔵")
                )
            },
            // SUM_PAIR
            ExerciseEntity().apply {
                id = UUID.fromString("e0000000-0000-0000-0000-000000000001")
                subjectId = memoryId
                type = "SUM_PAIR"
                difficulty = "EASY"
                prompt = "Find pairs where first number + static = second number."
                expectedAnswers = emptyList()
                timeLimitSeconds = 120
                exerciseParams = mapOf(
                    "staticNumbers" to listOf(5),
                    "pairsPerRound" to 4,
                    "minValue" to 1,
                    "maxValue" to 50
                )
            },
            ExerciseEntity().apply {
                id = UUID.fromString("e0000000-0000-0000-0000-000000000002")
                subjectId = memoryId
                type = "SUM_PAIR"
                difficulty = "MEDIUM"
                prompt = "Find sum pairs. Complete all pairs for the current static, then the next."
                expectedAnswers = emptyList()
                timeLimitSeconds = 180
                exerciseParams = mapOf(
                    "staticNumbers" to listOf(3, 7),
                    "pairsPerRound" to 3,
                    "minValue" to 1,
                    "maxValue" to 99
                )
            },
            ExerciseEntity().apply {
                id = UUID.fromString("e0000000-0000-0000-0000-000000000003")
                subjectId = memoryId
                type = "SUM_PAIR"
                difficulty = "ULTRA_EASY"
                prompt = "Find pairs where first + static = second."
                expectedAnswers = emptyList()
                timeLimitSeconds = 90
                exerciseParams = mapOf(
                    "staticNumbers" to listOf(2),
                    "pairsPerRound" to 3,
                    "minValue" to 1,
                    "maxValue" to 30
                )
            },
            ExerciseEntity().apply {
                id = UUID.fromString("e0000000-0000-0000-0000-000000000004")
                subjectId = memoryId
                type = "SUM_PAIR"
                difficulty = "HARD"
                prompt = "Find sum pairs. Complete each round before the next (3 rounds)."
                expectedAnswers = emptyList()
                timeLimitSeconds = 240
                exerciseParams = mapOf(
                    "staticNumbers" to listOf(2, 5, 10),
                    "pairsPerRound" to 4,
                    "minValue" to 1,
                    "maxValue" to 99
                )
            },
            // ANAGRAM (French) — under WORD category, accessible from WORD and all 30 sub-subjects
            ExerciseEntity().apply {
                id = UUID.fromString("f0000000-0000-0000-0000-000000000001")
                subjectId = wordId
                type = "ANAGRAM"
                difficulty = "ULTRA_EASY"
                prompt = "Trouvez le mot à partir des lettres affichées."
                expectedAnswers = emptyList()
                timeLimitSeconds = 90
                exerciseParams = mapOf("minLetters" to 2, "maxLetters" to 3, "language" to "fr")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f0000000-0000-0000-0000-000000000002")
                subjectId = wordId
                type = "ANAGRAM"
                difficulty = "EASY"
                prompt = "Trouvez le mot à partir des lettres affichées."
                expectedAnswers = emptyList()
                timeLimitSeconds = 120
                exerciseParams = mapOf("minLetters" to 3, "maxLetters" to 4, "language" to "fr")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f0000000-0000-0000-0000-000000000003")
                subjectId = wordId
                type = "ANAGRAM"
                difficulty = "MEDIUM"
                prompt = "Trouvez le mot à partir des lettres affichées."
                expectedAnswers = emptyList()
                timeLimitSeconds = 150
                exerciseParams = mapOf("minLetters" to 4, "maxLetters" to 5, "language" to "fr")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f0000000-0000-0000-0000-000000000004")
                subjectId = wordId
                type = "ANAGRAM"
                difficulty = "HARD"
                prompt = "Trouvez le mot à partir des lettres affichées."
                expectedAnswers = emptyList()
                timeLimitSeconds = 180
                exerciseParams = mapOf("minLetters" to 6, "maxLetters" to 7, "language" to "fr")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f0000000-0000-0000-0000-000000000005")
                subjectId = wordId
                type = "ANAGRAM"
                difficulty = "VERY_HARD"
                prompt = "Trouvez le mot à partir des lettres affichées."
                expectedAnswers = emptyList()
                timeLimitSeconds = 210
                exerciseParams = mapOf("minLetters" to 8, "maxLetters" to 15, "language" to "fr")
            }
        )

        exerciseRepository.saveAll(exercises)
    }
}

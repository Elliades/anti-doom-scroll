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
        val subjectWordleFr = SubjectEntity().apply {
            id = UUID.fromString("b0000000-0000-0000-0000-000000000011")
            code = "WORDLE_FR"
            name = "Wordle (FR)"
            description = "Devinez le mot en 6 essais. Lettres vertes = bonne position, jaunes = mauvaise position."
            scoringConfig = SubjectScoringConfig(
                accuracyType = SubjectScoringConfig.AccuracyType.BINARY,
                speedTargetMs = 120_000L,
                confidenceWeight = 0.1,
                streakBonusCap = 0.1
            )
        }
        val subjectWordleEn = SubjectEntity().apply {
            id = UUID.fromString("b0000000-0000-0000-0000-000000000012")
            code = "WORDLE_EN"
            name = "Wordle (EN)"
            description = "Guess the word in 6 tries. Green = right letter & position, yellow = right letter wrong position."
            scoringConfig = SubjectScoringConfig(
                accuracyType = SubjectScoringConfig.AccuracyType.BINARY,
                speedTargetMs = 120_000L,
                confidenceWeight = 0.1,
                streakBonusCap = 0.1
            )
        }
        val subjectEstimation = SubjectEntity().apply {
            id = UUID.fromString("b0000000-0000-0000-0000-000000000013")
            code = "ESTIMATION"
            name = "Estimation"
            description = "Approximate numerical answers: monuments, distances, populations, mental math. Scored on accuracy and speed."
            scoringConfig = SubjectScoringConfig(
                accuracyType = SubjectScoringConfig.AccuracyType.PARTIAL,
                speedTargetMs = 15_000L,
                confidenceWeight = 0.0,
                streakBonusCap = 0.1
            )
        }
        val subjectDigitSpan = SubjectEntity().apply {
            id = UUID.fromString("b0000000-0000-0000-0000-000000000014")
            code = "DIGIT_SPAN"
            name = "Digit Span"
            description = "Working memory: memorize digits and recall them in order, ascending, descending, even/odd, or every-other."
            scoringConfig = defaultScoring
        }
        subjectRepository.saveAll(listOf(subjectDefault, subjectB1, subjectMemory, subjectWord, subjectWordleFr, subjectWordleEn, subjectEstimation, subjectDigitSpan))

        val defaultId = subjectDefault.id!!
        val b1Id = subjectB1.id!!
        val memoryId = subjectMemory.id!!
        val wordId = subjectWord.id!!
        val wordleFrId = subjectWordleFr.id!!
        val wordleEnId = subjectWordleEn.id!!
        val estimationId = subjectEstimation.id!!
        val digitSpanId = subjectDigitSpan.id!!

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
            // ── N_BACK ladder exercises: card phase (parametric, dynamically generated) ──────────
            // 1-back: increasing suit count
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000014")
                subjectId = b1Id; type = "N_BACK"; difficulty = "ULTRA_EASY"
                prompt = "1-Back: Tap when the card matches the previous one. (2 suits)"
                expectedAnswers = emptyList(); timeLimitSeconds = 30
                exerciseParams = mapOf("n" to 1, "suitCount" to 2)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000015")
                subjectId = b1Id; type = "N_BACK"; difficulty = "ULTRA_EASY"
                prompt = "1-Back: Tap when the card matches the previous one. (3 suits)"
                expectedAnswers = emptyList(); timeLimitSeconds = 30
                exerciseParams = mapOf("n" to 1, "suitCount" to 3)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000016")
                subjectId = b1Id; type = "N_BACK"; difficulty = "ULTRA_EASY"
                prompt = "1-Back: Tap when the card matches the previous one. (4 suits)"
                expectedAnswers = emptyList(); timeLimitSeconds = 30
                exerciseParams = mapOf("n" to 1, "suitCount" to 4)
            },
            // 2-back: increasing suit count
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000017")
                subjectId = b1Id; type = "N_BACK"; difficulty = "EASY"
                prompt = "2-Back: Tap when the card matches the one from 2 steps back. (1 suit)"
                expectedAnswers = emptyList(); timeLimitSeconds = 60
                exerciseParams = mapOf("n" to 2, "suitCount" to 1)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000018")
                subjectId = b1Id; type = "N_BACK"; difficulty = "EASY"
                prompt = "2-Back: Tap when the card matches the one from 2 steps back. (3 suits)"
                expectedAnswers = emptyList(); timeLimitSeconds = 60
                exerciseParams = mapOf("n" to 2, "suitCount" to 3)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000019")
                subjectId = b1Id; type = "N_BACK"; difficulty = "EASY"
                prompt = "2-Back: Tap when the card matches the one from 2 steps back. (4 suits)"
                expectedAnswers = emptyList(); timeLimitSeconds = 60
                exerciseParams = mapOf("n" to 2, "suitCount" to 4)
            },
            // 3-back: increasing suit count
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000020")
                subjectId = b1Id; type = "N_BACK"; difficulty = "MEDIUM"
                prompt = "3-Back: Tap when the card matches the one from 3 steps back. (1 suit)"
                expectedAnswers = emptyList(); timeLimitSeconds = 60
                exerciseParams = mapOf("n" to 3, "suitCount" to 1)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000021")
                subjectId = b1Id; type = "N_BACK"; difficulty = "MEDIUM"
                prompt = "3-Back: Tap when the card matches the one from 3 steps back. (2 suits)"
                expectedAnswers = emptyList(); timeLimitSeconds = 60
                exerciseParams = mapOf("n" to 3, "suitCount" to 2)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000022")
                subjectId = b1Id; type = "N_BACK"; difficulty = "MEDIUM"
                prompt = "3-Back: Tap when the card matches the one from 3 steps back. (3 suits)"
                expectedAnswers = emptyList(); timeLimitSeconds = 60
                exerciseParams = mapOf("n" to 3, "suitCount" to 3)
            },
            // ── N_BACK_GRID ladder exercises: parametric (n + gridSize + sequenceLength) ─────────
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000030")
                subjectId = b1Id; type = "N_BACK_GRID"; difficulty = "ULTRA_EASY"
                prompt = "Grid 1-Back: Tap Match when the highlighted cell matches 1 step back. (3×3)"
                expectedAnswers = emptyList(); timeLimitSeconds = 45
                exerciseParams = mapOf("n" to 1, "gridSize" to 3, "sequenceLength" to 8)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000031")
                subjectId = b1Id; type = "N_BACK_GRID"; difficulty = "ULTRA_EASY"
                prompt = "Grid 1-Back: Tap Match when the highlighted cell matches 1 step back. (4×4)"
                expectedAnswers = emptyList(); timeLimitSeconds = 55
                exerciseParams = mapOf("n" to 1, "gridSize" to 4, "sequenceLength" to 10)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000032")
                subjectId = b1Id; type = "N_BACK_GRID"; difficulty = "EASY"
                prompt = "Grid 2-Back: Tap Match when the cell matches 2 steps back. (3×3)"
                expectedAnswers = emptyList(); timeLimitSeconds = 55
                exerciseParams = mapOf("n" to 2, "gridSize" to 3, "sequenceLength" to 10)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000033")
                subjectId = b1Id; type = "N_BACK_GRID"; difficulty = "EASY"
                prompt = "Grid 2-Back: Tap Match when the cell matches 2 steps back. (4×4)"
                expectedAnswers = emptyList(); timeLimitSeconds = 65
                exerciseParams = mapOf("n" to 2, "gridSize" to 4, "sequenceLength" to 12)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000034")
                subjectId = b1Id; type = "N_BACK_GRID"; difficulty = "MEDIUM"
                prompt = "Grid 3-Back: Tap Match when the cell matches 3 steps back. (3×3)"
                expectedAnswers = emptyList(); timeLimitSeconds = 65
                exerciseParams = mapOf("n" to 3, "gridSize" to 3, "sequenceLength" to 10)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000035")
                subjectId = b1Id; type = "N_BACK_GRID"; difficulty = "MEDIUM"
                prompt = "Grid 3-Back: Tap Match when the cell matches 3 steps back. (4×4)"
                expectedAnswers = emptyList(); timeLimitSeconds = 75
                exerciseParams = mapOf("n" to 3, "gridSize" to 4, "sequenceLength" to 12)
            },
            // ── DUAL_NBACK_GRID ladder exercises: parametric (n + gridSize + colorCount) ─────────
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000040")
                subjectId = b1Id; type = "DUAL_NBACK_GRID"; difficulty = "ULTRA_EASY"
                prompt = "Dual Grid 1-Back (short): Tap Match Position or Match Color when that attribute matches 1 step back."
                expectedAnswers = emptyList(); timeLimitSeconds = 55
                exerciseParams = mapOf("n" to 1, "gridSize" to 3, "colorCount" to 4, "sequenceLength" to 8)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000041")
                subjectId = b1Id; type = "DUAL_NBACK_GRID"; difficulty = "EASY"
                prompt = "Dual Grid 1-Back: Tap Match Position or Match Color when that attribute matches 1 step back."
                expectedAnswers = emptyList(); timeLimitSeconds = 65
                exerciseParams = mapOf("n" to 1, "gridSize" to 3, "colorCount" to 4, "sequenceLength" to 12)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000042")
                subjectId = b1Id; type = "DUAL_NBACK_GRID"; difficulty = "EASY"
                prompt = "Dual Grid 2-Back: Tap Match Position or Match Color when that attribute matches 2 steps back."
                expectedAnswers = emptyList(); timeLimitSeconds = 65
                exerciseParams = mapOf("n" to 2, "gridSize" to 3, "colorCount" to 4, "sequenceLength" to 10)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000043")
                subjectId = b1Id; type = "DUAL_NBACK_GRID"; difficulty = "MEDIUM"
                prompt = "Dual Grid 2-Back (4×4): Tap Match Position or Match Color when that attribute matches 2 steps back."
                expectedAnswers = emptyList(); timeLimitSeconds = 75
                exerciseParams = mapOf("n" to 2, "gridSize" to 4, "colorCount" to 4, "sequenceLength" to 12)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000044")
                subjectId = b1Id; type = "DUAL_NBACK_GRID"; difficulty = "MEDIUM"
                prompt = "Dual Grid 3-Back: Tap Match Position or Match Color when that attribute matches 3 steps back."
                expectedAnswers = emptyList(); timeLimitSeconds = 75
                exerciseParams = mapOf("n" to 3, "gridSize" to 3, "colorCount" to 4, "sequenceLength" to 10)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000045")
                subjectId = b1Id; type = "DUAL_NBACK_GRID"; difficulty = "HARD"
                prompt = "Dual Grid 3-Back (4×4): Tap Match Position or Match Color when that attribute matches 3 steps back."
                expectedAnswers = emptyList(); timeLimitSeconds = 90
                exerciseParams = mapOf("n" to 3, "gridSize" to 4, "colorCount" to 4, "sequenceLength" to 12)
            },
            // ── DUAL_NBACK_CARD ladder exercises: parametric (n + suitCount + sequenceLength) ────
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000050")
                subjectId = b1Id; type = "DUAL_NBACK_CARD"; difficulty = "ULTRA_EASY"
                prompt = "Dual Card 1-Back (short): Tap Match Color (suit) or Match Number (rank) when it matches 1 step back."
                expectedAnswers = emptyList(); timeLimitSeconds = 55
                exerciseParams = mapOf("n" to 1, "suitCount" to 4, "sequenceLength" to 8)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000051")
                subjectId = b1Id; type = "DUAL_NBACK_CARD"; difficulty = "EASY"
                prompt = "Dual Card 1-Back: Tap Match Color (suit) or Match Number (rank) when it matches 1 step back."
                expectedAnswers = emptyList(); timeLimitSeconds = 65
                exerciseParams = mapOf("n" to 1, "suitCount" to 4, "sequenceLength" to 12)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000052")
                subjectId = b1Id; type = "DUAL_NBACK_CARD"; difficulty = "EASY"
                prompt = "Dual Card 2-Back: Tap Match Color (suit) or Match Number (rank) when it matches 2 steps back."
                expectedAnswers = emptyList(); timeLimitSeconds = 65
                exerciseParams = mapOf("n" to 2, "suitCount" to 4, "sequenceLength" to 10)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000053")
                subjectId = b1Id; type = "DUAL_NBACK_CARD"; difficulty = "MEDIUM"
                prompt = "Dual Card 2-Back (long): Tap Match Color (suit) or Match Number (rank) when it matches 2 steps back."
                expectedAnswers = emptyList(); timeLimitSeconds = 75
                exerciseParams = mapOf("n" to 2, "suitCount" to 4, "sequenceLength" to 12)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000054")
                subjectId = b1Id; type = "DUAL_NBACK_CARD"; difficulty = "MEDIUM"
                prompt = "Dual Card 3-Back: Tap Match Color (suit) or Match Number (rank) when it matches 3 steps back."
                expectedAnswers = emptyList(); timeLimitSeconds = 75
                exerciseParams = mapOf("n" to 3, "suitCount" to 4, "sequenceLength" to 10)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("c0000000-0000-0000-0000-000000000055")
                subjectId = b1Id; type = "DUAL_NBACK_CARD"; difficulty = "HARD"
                prompt = "Dual Card 3-Back (long): Tap Match Color (suit) or Match Number (rank) when it matches 3 steps back."
                expectedAnswers = emptyList(); timeLimitSeconds = 90
                exerciseParams = mapOf("n" to 3, "suitCount" to 4, "sequenceLength" to 12)
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
            ExerciseEntity().apply {
                id = UUID.fromString("e0000000-0000-0000-0000-000000000006")
                subjectId = memoryId
                type = "SUM_PAIR"
                difficulty = "EASY"
                prompt = "Find pairs where first + static = second. Numbers are 1–2 digits."
                expectedAnswers = emptyList()
                timeLimitSeconds = 120
                exerciseParams = mapOf(
                    "staticNumbers" to listOf(5),
                    "pairsPerRound" to 4,
                    "minDigits" to 1,
                    "maxDigits" to 2
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
            },
            // WORDLE_FR: EASY=3 letters, MEDIUM=5, HARD=6, VERY_HARD=7
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000200")
                subjectId = wordleFrId
                type = "WORDLE"
                difficulty = "EASY"
                prompt = "Devinez le mot de 3 lettres en 6 essais."
                expectedAnswers = emptyList()
                timeLimitSeconds = 120
                exerciseParams = mapOf("language" to "fr", "maxAttempts" to 6)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000201")
                subjectId = wordleFrId
                type = "WORDLE"
                difficulty = "MEDIUM"
                prompt = "Devinez le mot de 5 lettres en 6 essais."
                expectedAnswers = emptyList()
                timeLimitSeconds = 180
                exerciseParams = mapOf("language" to "fr", "maxAttempts" to 6)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000202")
                subjectId = wordleFrId
                type = "WORDLE"
                difficulty = "HARD"
                prompt = "Devinez le mot de 6 lettres en 6 essais."
                expectedAnswers = emptyList()
                timeLimitSeconds = 240
                exerciseParams = mapOf("language" to "fr", "maxAttempts" to 6)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000203")
                subjectId = wordleFrId
                type = "WORDLE"
                difficulty = "VERY_HARD"
                prompt = "Devinez le mot de 7 lettres en 6 essais."
                expectedAnswers = emptyList()
                timeLimitSeconds = 300
                exerciseParams = mapOf("language" to "fr", "maxAttempts" to 6)
            },
            // WORDLE_EN: EASY=3 letters, MEDIUM=5, HARD=6, VERY_HARD=7
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000210")
                subjectId = wordleEnId
                type = "WORDLE"
                difficulty = "EASY"
                prompt = "Guess the 3-letter word in 6 tries."
                expectedAnswers = emptyList()
                timeLimitSeconds = 120
                exerciseParams = mapOf("language" to "en", "maxAttempts" to 6)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000211")
                subjectId = wordleEnId
                type = "WORDLE"
                difficulty = "MEDIUM"
                prompt = "Guess the 5-letter word in 6 tries."
                expectedAnswers = emptyList()
                timeLimitSeconds = 180
                exerciseParams = mapOf("language" to "en", "maxAttempts" to 6)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000212")
                subjectId = wordleEnId
                type = "WORDLE"
                difficulty = "HARD"
                prompt = "Guess the 6-letter word in 6 tries."
                expectedAnswers = emptyList()
                timeLimitSeconds = 240
                exerciseParams = mapOf("language" to "en", "maxAttempts" to 6)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000213")
                subjectId = wordleEnId
                type = "WORDLE"
                difficulty = "VERY_HARD"
                prompt = "Guess the 7-letter word in 6 tries."
                expectedAnswers = emptyList()
                timeLimitSeconds = 300
                exerciseParams = mapOf("language" to "en", "maxAttempts" to 6)
            },
            // ESTIMATION: approximate numerical answers — ULTRA_EASY (everyday knowledge)
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000300")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "How many days are in a year?"
                expectedAnswers = listOf("365")
                timeLimitSeconds = 20
                exerciseParams = mapOf("correctAnswer" to 365.0, "unit" to "days", "toleranceFactor" to 1.03, "category" to "math")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000301")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "How tall is the Eiffel Tower (in meters)?"
                expectedAnswers = listOf("330")
                timeLimitSeconds = 25
                exerciseParams = mapOf("correctAnswer" to 330.0, "unit" to "m", "toleranceFactor" to 1.5, "category" to "geography", "hint" to "It was the tallest man-made structure when built in 1889.")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000302")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "What is the speed of sound in air (m/s)?"
                expectedAnswers = listOf("343")
                timeLimitSeconds = 25
                exerciseParams = mapOf("correctAnswer" to 343.0, "unit" to "m/s", "toleranceFactor" to 1.3, "category" to "science", "hint" to "It depends on temperature; ~20°C.")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000303")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "Estimate: π × 10 = ?"
                expectedAnswers = listOf("31.4")
                timeLimitSeconds = 15
                exerciseParams = mapOf("correctAnswer" to 31.416, "unit" to "", "toleranceFactor" to 1.05, "category" to "math", "hint" to "π ≈ 3.14159…")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000304")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "How many hours are in a week?"
                expectedAnswers = listOf("168")
                timeLimitSeconds = 20
                exerciseParams = mapOf("correctAnswer" to 168.0, "unit" to "hours", "toleranceFactor" to 1.05, "category" to "math")
            },
            // ESTIMATION EASY
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000305")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "EASY"
                prompt = "What is the height of Mount Everest (in meters)?"
                expectedAnswers = listOf("8849")
                timeLimitSeconds = 30
                exerciseParams = mapOf("correctAnswer" to 8849.0, "unit" to "m", "toleranceFactor" to 1.3, "category" to "geography", "hint" to "It is the highest peak on Earth.")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000306")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "EASY"
                prompt = "What is the straight-line distance from Paris to New York (in km)?"
                expectedAnswers = listOf("5837")
                timeLimitSeconds = 30
                exerciseParams = mapOf("correctAnswer" to 5837.0, "unit" to "km", "toleranceFactor" to 2.0, "category" to "geography")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000307")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "EASY"
                prompt = "What is the population of France (in millions)?"
                expectedAnswers = listOf("68")
                timeLimitSeconds = 30
                exerciseParams = mapOf("correctAnswer" to 68.0, "unit" to "million people", "toleranceFactor" to 1.5, "category" to "geography")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000308")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "EASY"
                prompt = "Estimate: 17 × 23 = ?"
                expectedAnswers = listOf("391")
                timeLimitSeconds = 20
                exerciseParams = mapOf("correctAnswer" to 391.0, "unit" to "", "toleranceFactor" to 1.1, "category" to "math", "hint" to "Decompose: 17×20 + 17×3")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000309")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "EASY"
                prompt = "Estimate: √200 = ?"
                expectedAnswers = listOf("14.1")
                timeLimitSeconds = 20
                exerciseParams = mapOf("correctAnswer" to 14.142, "unit" to "", "toleranceFactor" to 1.08, "category" to "math", "hint" to "Between √196=14 and √225=15.")
            },
            // ESTIMATION MEDIUM
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000310")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "MEDIUM"
                prompt = "What is the approximate population of Earth (in billions)?"
                expectedAnswers = listOf("8")
                timeLimitSeconds = 25
                exerciseParams = mapOf("correctAnswer" to 8.1, "unit" to "billion people", "toleranceFactor" to 1.5, "category" to "science")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000311")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "MEDIUM"
                prompt = "What is the average distance from Earth to the Moon (in km)?"
                expectedAnswers = listOf("384400")
                timeLimitSeconds = 35
                exerciseParams = mapOf("correctAnswer" to 384400.0, "unit" to "km", "toleranceFactor" to 2.0, "category" to "science", "hint" to "Light takes about 1.3 seconds to travel that distance.")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000312")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "MEDIUM"
                prompt = "What is the area of France (in km²)?"
                expectedAnswers = listOf("551695")
                timeLimitSeconds = 35
                exerciseParams = mapOf("correctAnswer" to 551695.0, "unit" to "km²", "toleranceFactor" to 2.0, "category" to "geography", "hint" to "It is the largest country in the EU.")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000313")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "MEDIUM"
                prompt = "Estimate: e³ = ?"
                expectedAnswers = listOf("20.1")
                timeLimitSeconds = 25
                exerciseParams = mapOf("correctAnswer" to 20.086, "unit" to "", "toleranceFactor" to 1.15, "category" to "math", "hint" to "e ≈ 2.718; e² ≈ 7.39.")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000314")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "MEDIUM"
                prompt = "What is 2^10?"
                expectedAnswers = listOf("1024")
                timeLimitSeconds = 20
                exerciseParams = mapOf("correctAnswer" to 1024.0, "unit" to "", "toleranceFactor" to 1.05, "category" to "math", "hint" to "Powers of 2 double each time: 2, 4, 8, 16…")
            },
            // ESTIMATION HARD
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000315")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "HARD"
                prompt = "What is the speed of light in a vacuum (in km/s)?"
                expectedAnswers = listOf("299792")
                timeLimitSeconds = 30
                exerciseParams = mapOf("correctAnswer" to 299792.0, "unit" to "km/s", "toleranceFactor" to 1.1, "category" to "science", "hint" to "It is exactly 299 792 458 m/s.")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000316")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "HARD"
                prompt = "What is the average distance from Earth to the Sun (in millions of km)?"
                expectedAnswers = listOf("150")
                timeLimitSeconds = 30
                exerciseParams = mapOf("correctAnswer" to 149.6, "unit" to "million km", "toleranceFactor" to 1.5, "category" to "science", "hint" to "This distance is called 1 astronomical unit (1 AU).")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000317")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "HARD"
                prompt = "Estimate: 7^5 = ?"
                expectedAnswers = listOf("16807")
                timeLimitSeconds = 30
                exerciseParams = mapOf("correctAnswer" to 16807.0, "unit" to "", "toleranceFactor" to 1.15, "category" to "math", "hint" to "7^2=49, 7^3=343, 7^4=2401…")
            },
            // ESTIMATION VERY_HARD
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000318")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "VERY_HARD"
                prompt = "How many seconds are in a year?"
                expectedAnswers = listOf("31536000")
                timeLimitSeconds = 40
                exerciseParams = mapOf("correctAnswer" to 31536000.0, "unit" to "seconds", "toleranceFactor" to 1.05, "category" to "math", "hint" to "365 × 24 × 60 × 60")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000319")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "VERY_HARD"
                prompt = "What is the estimated age of the universe (in billions of years)?"
                expectedAnswers = listOf("13.8")
                timeLimitSeconds = 35
                exerciseParams = mapOf("correctAnswer" to 13.8, "unit" to "billion years", "toleranceFactor" to 1.3, "category" to "science", "hint" to "Measured from the cosmic microwave background radiation.")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000320")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "VERY_HARD"
                prompt = "What is 2^20 (exact)?"
                expectedAnswers = listOf("1048576")
                timeLimitSeconds = 30
                exerciseParams = mapOf("correctAnswer" to 1048576.0, "unit" to "", "toleranceFactor" to 1.05, "category" to "math", "hint" to "2^10 = 1 024, so 2^20 = 1 024².")
            },
            // Extra ULTRA_EASY for first-level variety
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000321")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "Estimate: 372 ÷ 3 = ?"
                expectedAnswers = listOf("124")
                timeLimitSeconds = 20
                exerciseParams = mapOf("correctAnswer" to 124.0, "unit" to "", "toleranceFactor" to 1.15, "category" to "math", "timeWeightHigher" to true)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000322")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "A car travels at 100 km/h. How long (in hours) to cover 50 km?"
                expectedAnswers = listOf("0.5")
                timeLimitSeconds = 25
                exerciseParams = mapOf("correctAnswer" to 0.5, "unit" to "hours", "toleranceFactor" to 1.2, "category" to "math", "hint" to "time = distance ÷ speed")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000323")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "Budget is 72K, daily rate is 612. How many days can you cover?"
                expectedAnswers = listOf("117.6")
                timeLimitSeconds = 25
                exerciseParams = mapOf("correctAnswer" to 117.647, "unit" to "days", "toleranceFactor" to 1.15, "category" to "math", "hint" to "days = budget ÷ daily rate")
            },
            // Car speed + conversion h/min — easier tau for first level
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000324")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "A car travels at 130 km/h. How long (in hours) to cover 50 km?"
                expectedAnswers = listOf("0.385")
                timeLimitSeconds = 25
                exerciseParams = mapOf("correctAnswer" to 0.385, "unit" to "hours", "toleranceFactor" to 1.4, "category" to "math", "hint" to "time = distance ÷ speed")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000325")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "A car travels at 60 km/h. How long (in minutes) to cover 30 km?"
                expectedAnswers = listOf("30")
                timeLimitSeconds = 25
                exerciseParams = mapOf("correctAnswer" to 30.0, "unit" to "minutes", "toleranceFactor" to 1.4, "category" to "math", "hint" to "time = distance ÷ speed; 1 h = 60 min")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000326")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "How many minutes are in 1.5 hours?"
                expectedAnswers = listOf("90")
                timeLimitSeconds = 20
                exerciseParams = mapOf("correctAnswer" to 90.0, "unit" to "minutes", "toleranceFactor" to 1.4, "category" to "math", "hint" to "1 hour = 60 minutes")
            },
            ExerciseEntity().apply {
                id = UUID.fromString("a0000000-0000-0000-0000-000000000327")
                subjectId = estimationId
                type = "ESTIMATION"
                difficulty = "ULTRA_EASY"
                prompt = "How many minutes are in 2 hours?"
                expectedAnswers = listOf("120")
                timeLimitSeconds = 20
                exerciseParams = mapOf("correctAnswer" to 120.0, "unit" to "minutes", "toleranceFactor" to 1.4, "category" to "math", "hint" to "1 hour = 60 minutes")
            },
            // REMEMBER_NUMBER: memorize a number, solve math, recall
            ExerciseEntity().apply {
                id = UUID.fromString("f3000000-0000-0000-0000-000000000001")
                subjectId = memoryId
                type = "REMEMBER_NUMBER"
                difficulty = "ULTRA_EASY"
                prompt = "Remember the number, solve the math problem, then recall the number."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf("numberDigits" to 2, "displayTimeMs" to 3000, "mathOperation" to "ADD", "mathFirstMax" to 9, "mathSecondMax" to 9)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f3000000-0000-0000-0000-000000000002")
                subjectId = memoryId
                type = "REMEMBER_NUMBER"
                difficulty = "EASY"
                prompt = "Remember the number, solve the math problem, then recall the number."
                expectedAnswers = emptyList()
                timeLimitSeconds = 60
                exerciseParams = mapOf("numberDigits" to 3, "displayTimeMs" to 2500, "mathOperation" to "ADD", "mathFirstMax" to 99, "mathSecondMax" to 9)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f3000000-0000-0000-0000-000000000003")
                subjectId = memoryId
                type = "REMEMBER_NUMBER"
                difficulty = "MEDIUM"
                prompt = "Remember the number, solve the math problem, then recall the number."
                expectedAnswers = emptyList()
                timeLimitSeconds = 90
                exerciseParams = mapOf("numberDigits" to 4, "displayTimeMs" to 2000, "mathOperation" to "SUBTRACT", "mathFirstMax" to 99, "mathSecondMax" to 99)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f3000000-0000-0000-0000-000000000004")
                subjectId = memoryId
                type = "REMEMBER_NUMBER"
                difficulty = "HARD"
                prompt = "Remember the number, solve the math problem, then recall the number."
                expectedAnswers = emptyList()
                timeLimitSeconds = 90
                exerciseParams = mapOf("numberDigits" to 5, "displayTimeMs" to 1500, "mathOperation" to "MULTIPLY", "mathFirstMax" to 12, "mathSecondMax" to 9)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f3000000-0000-0000-0000-000000000005")
                subjectId = memoryId
                type = "REMEMBER_NUMBER"
                difficulty = "VERY_HARD"
                prompt = "Remember the number, solve the math problem, then recall the number."
                expectedAnswers = emptyList()
                timeLimitSeconds = 120
                exerciseParams = mapOf("numberDigits" to 6, "displayTimeMs" to 1200, "mathOperation" to "MULTIPLY", "mathFirstMax" to 99, "mathSecondMax" to 9)
            },
            // DIGIT_SPAN: progressive digit recall with challenge modes
            ExerciseEntity().apply {
                id = UUID.fromString("f2000000-0000-0000-0000-000000000001")
                subjectId = digitSpanId
                type = "DIGIT_SPAN"
                difficulty = "ULTRA_EASY"
                prompt = "Memorize the digits, then type them back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 300
                exerciseParams = mapOf("startLength" to 3, "displayTimeMs" to 3000, "maxLength" to 15)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f2000000-0000-0000-0000-000000000002")
                subjectId = digitSpanId
                type = "DIGIT_SPAN"
                difficulty = "EASY"
                prompt = "Memorize the digits, then type them back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 300
                exerciseParams = mapOf("startLength" to 4, "displayTimeMs" to 3000, "maxLength" to 15)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f2000000-0000-0000-0000-000000000003")
                subjectId = digitSpanId
                type = "DIGIT_SPAN"
                difficulty = "MEDIUM"
                prompt = "Memorize the digits, then type them back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 300
                exerciseParams = mapOf("startLength" to 5, "displayTimeMs" to 2500, "maxLength" to 15)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f2000000-0000-0000-0000-000000000004")
                subjectId = digitSpanId
                type = "DIGIT_SPAN"
                difficulty = "HARD"
                prompt = "Memorize the digits, then type them back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 300
                exerciseParams = mapOf("startLength" to 6, "displayTimeMs" to 2000, "maxLength" to 15)
            },
            ExerciseEntity().apply {
                id = UUID.fromString("f2000000-0000-0000-0000-000000000005")
                subjectId = digitSpanId
                type = "DIGIT_SPAN"
                difficulty = "VERY_HARD"
                prompt = "Memorize the digits, then type them back."
                expectedAnswers = emptyList()
                timeLimitSeconds = 300
                exerciseParams = mapOf("startLength" to 7, "displayTimeMs" to 1500, "maxLength" to 15)
            }
        )

        exerciseRepository.saveAll(exercises)
    }
}

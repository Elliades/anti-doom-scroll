-- Math flashcards: generated addition, subtraction, multiplication, division.
-- Params: operation, firstMax, secondMax (digit-based: 9=1 digit, 99=2, 999=3, 9999=4).
-- ULTRA_EASY: 1 digit + 1-2 digits. EASY: 1-2 digits. MEDIUM: 1-3. HARD: 1-4.

-- Update existing ultra-easy: ADD, 1 digit + 1-2 digits
UPDATE exercise SET exercise_params = '{"operation":"ADD","firstMax":9,"secondMax":99}'
WHERE id = 'a0000000-0000-0000-0000-000000000001' AND type = 'FLASHCARD_QA';

-- Update existing easy: ADD, 1-2 digits + 1-2 digits
UPDATE exercise SET exercise_params = '{"operation":"ADD","firstMax":99,"secondMax":99}'
WHERE id = 'a0000000-0000-0000-0000-000000000002' AND type = 'FLASHCARD_QA';

-- Addition: medium (1-3 digits), hard (1-4 digits)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES
    ('a0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'MEDIUM',
     'Solve the addition.', '[]', 60, '{"operation":"ADD","firstMax":999,"secondMax":999}'),
    ('a0000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'HARD',
     'Solve the addition.', '[]', 90, '{"operation":"ADD","firstMax":9999,"secondMax":9999}')
ON CONFLICT (id) DO NOTHING;

-- Subtraction: ultra-easy to hard (1-2 digit − 1 digit, up to 1-4 − 1-4)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES
    ('a0000000-0000-0000-0000-000000000020', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'ULTRA_EASY',
     'Solve the subtraction.', '[]', 30, '{"operation":"SUBTRACT","firstMax":99,"secondMax":9}'),
    ('a0000000-0000-0000-0000-000000000021', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'EASY',
     'Solve the subtraction.', '[]', 45, '{"operation":"SUBTRACT","firstMax":99,"secondMax":99}'),
    ('a0000000-0000-0000-0000-000000000022', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'MEDIUM',
     'Solve the subtraction.', '[]', 60, '{"operation":"SUBTRACT","firstMax":999,"secondMax":999}'),
    ('a0000000-0000-0000-0000-000000000023', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'HARD',
     'Solve the subtraction.', '[]', 90, '{"operation":"SUBTRACT","firstMax":9999,"secondMax":9999}')
ON CONFLICT (id) DO NOTHING;

-- Multiplication: 1×1 digit to 1-3×1-3 digits
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES
    ('a0000000-0000-0000-0000-000000000030', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'ULTRA_EASY',
     'Solve the multiplication.', '[]', 30, '{"operation":"MULTIPLY","firstMax":9,"secondMax":9}'),
    ('a0000000-0000-0000-0000-000000000031', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'EASY',
     'Solve the multiplication.', '[]', 45, '{"operation":"MULTIPLY","firstMax":99,"secondMax":9}'),
    ('a0000000-0000-0000-0000-000000000032', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'MEDIUM',
     'Solve the multiplication.', '[]', 60, '{"operation":"MULTIPLY","firstMax":99,"secondMax":99}'),
    ('a0000000-0000-0000-0000-000000000033', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'HARD',
     'Solve the multiplication.', '[]', 90, '{"operation":"MULTIPLY","firstMax":999,"secondMax":999}')
ON CONFLICT (id) DO NOTHING;

-- Division: divisor and quotient digit ranges (dividend = divisor × quotient)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES
    ('a0000000-0000-0000-0000-000000000040', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'ULTRA_EASY',
     'Solve the division.', '[]', 30, '{"operation":"DIVIDE","firstMax":9,"secondMax":9}'),
    ('a0000000-0000-0000-0000-000000000041', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'EASY',
     'Solve the division.', '[]', 45, '{"operation":"DIVIDE","firstMax":99,"secondMax":9}'),
    ('a0000000-0000-0000-0000-000000000042', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'MEDIUM',
     'Solve the division.', '[]', 60, '{"operation":"DIVIDE","firstMax":99,"secondMax":99}'),
    ('a0000000-0000-0000-0000-000000000043', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'HARD',
     'Solve the division.', '[]', 90, '{"operation":"DIVIDE","firstMax":999,"secondMax":99}')
ON CONFLICT (id) DO NOTHING;

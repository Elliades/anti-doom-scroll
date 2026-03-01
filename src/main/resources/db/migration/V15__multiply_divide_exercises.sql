-- Multiply and Divide (FLASHCARD_QA) with custom difficulty progressions.
-- Multiply: ULTRA_EASY = 2,5,10 × digit; EASY = times tables 1–9×1–12; MEDIUM = 2-digit×1-digit; HARD = 2-digit×2-digit.
-- Divide: ULTRA_EASY = ÷2,5,10; EASY = ÷2–9, quotient 1–12; MEDIUM = ÷1–9, quotient 1–99; HARD = 2-digit÷2-digit.

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES
    -- Multiplication
    ('a0000000-0000-0000-0000-000000000030', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'ULTRA_EASY',
     'Solve the multiplication.', '[]', 30, '{"operation":"MULTIPLY","firstMin":1,"firstMax":10,"firstValues":[2,5,10],"secondMin":1,"secondMax":9}'),
    ('a0000000-0000-0000-0000-000000000031', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'EASY',
     'Solve the multiplication.', '[]', 45, '{"operation":"MULTIPLY","firstMin":1,"firstMax":9,"secondMin":1,"secondMax":12}'),
    ('a0000000-0000-0000-0000-000000000032', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'MEDIUM',
     'Solve the multiplication.', '[]', 60, '{"operation":"MULTIPLY","firstMin":10,"firstMax":99,"secondMin":1,"secondMax":9}'),
    ('a0000000-0000-0000-0000-000000000033', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'HARD',
     'Solve the multiplication.', '[]', 90, '{"operation":"MULTIPLY","firstMin":10,"firstMax":99,"secondMin":10,"secondMax":99}'),
    -- Division
    ('a0000000-0000-0000-0000-000000000040', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'ULTRA_EASY',
     'Solve the division.', '[]', 30, '{"operation":"DIVIDE","firstMin":1,"firstMax":9,"secondMax":10,"secondValues":[2,5,10]}'),
    ('a0000000-0000-0000-0000-000000000041', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'EASY',
     'Solve the division.', '[]', 45, '{"operation":"DIVIDE","firstMin":1,"firstMax":12,"secondMin":2,"secondMax":9}'),
    ('a0000000-0000-0000-0000-000000000042', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'MEDIUM',
     'Solve the division.', '[]', 60, '{"operation":"DIVIDE","firstMin":1,"firstMax":99,"secondMin":1,"secondMax":9}'),
    ('a0000000-0000-0000-0000-000000000043', 'b0000000-0000-0000-0000-000000000001', 'FLASHCARD_QA', 'HARD',
     'Solve the division.', '[]', 90, '{"operation":"DIVIDE","firstMin":10,"firstMax":99,"secondMin":10,"secondMax":99}')
ON CONFLICT (id) DO NOTHING;

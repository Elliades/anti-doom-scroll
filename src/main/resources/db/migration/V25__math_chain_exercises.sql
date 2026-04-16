-- Seed MATH_CHAIN exercises under "default" subject.
-- Actual chain generation (startNumber, steps, expectedAnswer) happens at response time
-- via MathChainGenerator; these rows are templates with difficulty only.

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds)
SELECT
    gen_random_uuid(),
    s.id,
    'MATH_CHAIN',
    d.difficulty,
    'Mental math chain',
    '["0"]',
    120
FROM subject s
CROSS JOIN (VALUES ('ULTRA_EASY'), ('EASY'), ('MEDIUM'), ('HARD')) AS d(difficulty)
WHERE s.code = 'default';

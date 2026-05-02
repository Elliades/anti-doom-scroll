-- Easiest digit span seed (startLength 2) for early working-memory ladder levels
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'f2000000-0000-0000-0000-000000000006',
    'b0000000-0000-0000-0000-000000000014',
    'DIGIT_SPAN',
    'ULTRA_EASY',
    'Memorize the digits, then type them back.',
    '[]',
    300,
    '{"startLength":2,"displayTimeMs":3500,"maxLength":15}',
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

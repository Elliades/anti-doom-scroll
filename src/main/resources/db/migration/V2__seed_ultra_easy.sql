-- One ultra-easy exercise so "reopen" can serve in < 1 second (cached).
INSERT INTO exercise (id, type, difficulty, prompt, expected_answers, axis, time_limit_seconds)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'FLASHCARD_QA',
    'ULTRA_EASY',
    'What is 1 + 1?',
    '["2"]',
    'default',
    30
) ON CONFLICT (id) DO NOTHING;

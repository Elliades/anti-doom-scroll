-- Seed 2-back and 3-back exercises (subject B1) so GET /api/nback/2 and /api/nback/3 return an exercise.
-- 2-back: match when current == item 2 positions back. Sequence has matches at indices 3, 5, 8, 12.
-- 3-back: match when current == item 3 positions back. Sequence has matches at indices 4, 8, 13.

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'c0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK',
    'EASY',
    '2-Back: Tap when the card matches the one from 2 steps back.',
    '[]',
    60,
    '{"n": 2, "sequence": ["AC","AC","3H","AC","4S","AC","5C","6D","5C","7S","8C","4S","9D"], "matchIndices": [3, 5, 8, 12]}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'c0000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK',
    'MEDIUM',
    '3-Back: Tap when the card matches the one from 3 steps back.',
    '[]',
    60,
    '{"n": 3, "sequence": ["AC","AC","3H","4S","AC","5C","6D","AC","7H","8S","AC"], "matchIndices": [4, 7, 10]}'
) ON CONFLICT (id) DO NOTHING;

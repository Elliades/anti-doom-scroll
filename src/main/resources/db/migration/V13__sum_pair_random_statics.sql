-- Add SUM_PAIR exercise with randomly generated staticNumbers (staticCount, staticMin, staticMax).
-- Each play gets different statics; values are generated and sorted.
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'e0000000-0000-0000-0000-000000000005',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'MEDIUM',
    'Find pairs where first + static = second. Statics vary each play.',
    '[]',
    180,
    '{"staticCount": 2, "staticMin": 2, "staticMax": 10, "pairsPerRound": 3, "minValue": 1, "maxValue": 99}'
) ON CONFLICT (id) DO NOTHING;

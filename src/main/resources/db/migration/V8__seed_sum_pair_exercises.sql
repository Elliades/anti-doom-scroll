-- SUM_PAIR exercises: find pairs (a, b) where a + static = b.
-- Single static (easy) and multiple statics (harder) in Memory subject.

-- Easy: single static 5, 4 pairs per round
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'e0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'EASY',
    'Find pairs where first number + static = second number.',
    '[]',
    120,
    '{"staticNumbers": [5], "pairsPerRound": 4, "minValue": 1, "maxValue": 50}'
) ON CONFLICT (id) DO NOTHING;

-- Medium: two statics (3 and 7), 3 pairs per round each
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'e0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'MEDIUM',
    'Find sum pairs. Complete all pairs for the current static, then the next.',
    '[]',
    180,
    '{"staticNumbers": [3, 7], "pairsPerRound": 3, "minValue": 1, "maxValue": 99}'
) ON CONFLICT (id) DO NOTHING;

-- Ultra-easy: single static 2, 3 pairs
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'e0000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'ULTRA_EASY',
    'Find pairs where first + static = second.',
    '[]',
    90,
    '{"staticNumbers": [2], "pairsPerRound": 3, "minValue": 1, "maxValue": 30}'
) ON CONFLICT (id) DO NOTHING;

-- Hard: 3 statics (2, 5, 10), 4 pairs per round; range 1-99 gives rangeSize 33 >= 10+4
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'e0000000-0000-0000-0000-000000000004',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'HARD',
    'Find sum pairs. Complete each round before the next (3 rounds).',
    '[]',
    240,
    '{"staticNumbers": [2, 5, 10], "pairsPerRound": 4, "minValue": 1, "maxValue": 99}'
) ON CONFLICT (id) DO NOTHING;

-- IMAGE_PAIR: pair-matching by same background + same image (e.g. animals with colored backgrounds).
-- SUM_PAIR with digit range: one exercise using minDigits/maxDigits for displayed number range.

-- Image-pair: 4 pairs, max 2 pairs per background, 2 background types (no color + 1 color)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'f0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000008',
    'IMAGE_PAIR',
    'EASY',
    'Find pairs with the same background and the same image.',
    '[]',
    120,
    '{"pairCount": 4, "maxPairsPerBackground": 2, "colorCount": 1}'
) ON CONFLICT (id) DO NOTHING;

-- Image-pair: 6 pairs, max 2 per background, 3 colors (no color + 2)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'f0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000008',
    'IMAGE_PAIR',
    'MEDIUM',
    'Match cards: same background and same animal.',
    '[]',
    180,
    '{"pairCount": 6, "maxPairsPerBackground": 2, "colorCount": 2}'
) ON CONFLICT (id) DO NOTHING;

-- SUM_PAIR with digit range: 1–2 digit numbers only (minDigits=1, maxDigits=2 → 1–99)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'e0000000-0000-0000-0000-000000000006',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'EASY',
    'Find pairs where first + static = second. Numbers are 1–2 digits.',
    '[]',
    120,
    '{"staticNumbers": [5], "pairsPerRound": 4, "minDigits": 1, "maxDigits": 2}'
) ON CONFLICT (id) DO NOTHING;

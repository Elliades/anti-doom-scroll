-- Memory subject and MEMORY_CARD_PAIRS exercises (pair-matching card game).
-- Subject: Memory games (visual/spatial short-term memory).

INSERT INTO subject (id, code, name, description, scoring_config)
VALUES (
    'b0000000-0000-0000-0000-000000000008',
    'MEMORY',
    'Memory',
    'Memory games: find matching pairs of cards.',
    '{"accuracyType":"BINARY","speedTargetMs":60000,"confidenceWeight":0.1,"streakBonusCap":0.1}'
) ON CONFLICT (id) DO NOTHING;

-- Easy: 4 pairs (8 cards)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'd0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000008',
    'MEMORY_CARD_PAIRS',
    'EASY',
    'Find all matching pairs. Flip two cards at a time.',
    '[]',
    120,
    '{"pairCount": 4, "symbols": ["🍎", "🍊", "🍋", "🍇"]}'
) ON CONFLICT (id) DO NOTHING;

-- Medium: 6 pairs (12 cards)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'd0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000008',
    'MEMORY_CARD_PAIRS',
    'MEDIUM',
    'Find all matching pairs. Flip two cards at a time.',
    '[]',
    180,
    '{"pairCount": 6, "symbols": ["🐶", "🐱", "🐰", "🐻", "🦊", "🐼"]}'
) ON CONFLICT (id) DO NOTHING;

-- Ultra-easy: 3 pairs (6 cards) for quick session
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'd0000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000008',
    'MEMORY_CARD_PAIRS',
    'ULTRA_EASY',
    'Find the 3 matching pairs.',
    '[]',
    60,
    '{"pairCount": 3, "symbols": ["⭐", "❤️", "🔵"]}'
) ON CONFLICT (id) DO NOTHING;

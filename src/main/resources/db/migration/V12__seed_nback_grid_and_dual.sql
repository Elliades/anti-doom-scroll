-- Seed Grid N-Back, Dual N-Back Grid, and Dual N-Back Card exercises (subject B1).
-- Grid: 3x3 positions 0-8 (row-major). Dual Grid: position + color. Dual Card: suit + rank matches.

-- 1-Back Grid: positions 0,1,2,3,4,5,6,7,8; match when same position N back
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'c0000000-0000-0000-0000-000000000010',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK_GRID',
    'ULTRA_EASY',
    'Grid 1-Back: Tap Match when the highlighted cell is the same as 1 step back.',
    '[]',
    45,
    '{"n": 1, "sequence": [0, 4, 2, 4, 8, 1, 8], "matchIndices": [3, 6], "gridSize": 3}'
) ON CONFLICT (id) DO NOTHING;

-- 2-Back Grid
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'c0000000-0000-0000-0000-000000000011',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK_GRID',
    'EASY',
    'Grid 2-Back: Tap Match when the highlighted cell matches the one from 2 steps back.',
    '[]',
    60,
    '{"n": 2, "sequence": [0, 2, 4, 2, 6, 8, 4], "matchIndices": [3, 6], "gridSize": 3}'
) ON CONFLICT (id) DO NOTHING;

-- Dual 1-Back Grid: position + color (blue, red, yellow, green)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'c0000000-0000-0000-0000-000000000012',
    'b0000000-0000-0000-0000-000000000004',
    'DUAL_NBACK_GRID',
    'ULTRA_EASY',
    'Dual Grid 1-Back: Tap Match Position or Match Color when that attribute matches 1 step back.',
    '[]',
    60,
    '{"n": 1, "sequence": [{"position": 0, "color": "#4285F4"}, {"position": 4, "color": "#EA4335"}, {"position": 0, "color": "#FBBC04"}, {"position": 2, "color": "#4285F4"}, {"position": 4, "color": "#34A853"}], "matchPositionIndices": [2], "matchColorIndices": [3], "colors": ["#4285F4", "#EA4335", "#FBBC04", "#34A853"], "gridSize": 3}'
) ON CONFLICT (id) DO NOTHING;

-- Dual 1-Back Card: suit (color) and rank (number) matches
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'c0000000-0000-0000-0000-000000000013',
    'b0000000-0000-0000-0000-000000000004',
    'DUAL_NBACK_CARD',
    'ULTRA_EASY',
    'Dual Card 1-Back: Tap Match Color (suit) or Match Number (rank) when that matches 1 step back.',
    '[]',
    60,
    '{"n": 1, "sequence": ["AC", "2D", "2C", "3H", "AS", "3S"], "matchColorIndices": [5], "matchNumberIndices": [2]}'
) ON CONFLICT (id) DO NOTHING;

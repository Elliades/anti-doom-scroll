-- Idempotent: clear so multiple test classes sharing the same in-memory DB can re-run this script
DELETE FROM exercise;
DELETE FROM subject;

-- Subjects for tests
INSERT INTO subject (id, code, name, description, scoring_config, created_at)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'default', 'Default', 'Default subject', '{"accuracyType":"BINARY","speedTargetMs":10000,"confidenceWeight":0,"streakBonusCap":0.1}', CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000004', 'B1', 'N-back', 'Working memory', '{}', CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000008', 'MEMORY', 'Memory', 'Memory games', '{}', CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000010', 'WORD', 'Word', 'Word games', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}', CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'Estimation', 'Approximate numerical answers', '{"accuracyType":"PARTIAL","speedTargetMs":15000,"confidenceWeight":0.0,"streakBonusCap":0.1}', CURRENT_TIMESTAMP);

-- One ultra-easy ADD exercise for integration tests (H2, sum ladder level 0)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000001',
    'FLASHCARD_QA',
    'ULTRA_EASY',
    'What is 1 + 1?',
    '["2"]',
    30,
    '{"operation":"ADD","firstMax":9,"secondMax":99}',
    CURRENT_TIMESTAMP
);

-- Ultra-easy N-back exercise for integration tests (card-based)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'c0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK',
    'ULTRA_EASY',
    '1-Back: Tap when the card matches the previous one.',
    '[]',
    30,
    '{"n": 1, "sequence": ["AC","AC","2D","3H","4S","4S","5C","6D","7H","7H"], "matchIndices": [1, 5, 9]}',
    CURRENT_TIMESTAMP
);

-- 2-back for GET /api/nback/2
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'c0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK',
    'EASY',
    '2-Back: Tap when the card matches the one from 2 steps back.',
    '[]',
    60,
    '{"n": 2, "sequence": ["AC","AC","3H","AC","4S","AC","5C","6D","5C"], "matchIndices": [3, 5, 8]}',
    CURRENT_TIMESTAMP
);

-- 3-back for GET /api/nback/3
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'c0000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK',
    'MEDIUM',
    '3-Back: Tap when the card matches the one from 3 steps back.',
    '[]',
    60,
    '{"n": 3, "sequence": ["AC","AC","3H","4S","AC","5C","6D","AC","7H","8S","AC"], "matchIndices": [4, 7, 10]}',
    CURRENT_TIMESTAMP
);

-- Third exercise for openapp session (3 steps), ADD EASY for sum ladder
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000001',
    'FLASHCARD_QA',
    'EASY',
    'What is 2 + 2?',
    '["4"]',
    45,
    '{"operation":"ADD","firstMax":99,"secondMax":99}',
    CURRENT_TIMESTAMP
);

-- SUBTRACT EASY for sum ladder level 2 (ADD+SUBTRACT)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000020',
    'b0000000-0000-0000-0000-000000000001',
    'FLASHCARD_QA',
    'ULTRA_EASY',
    'Solve the subtraction.',
    '[]',
    30,
    '{"operation":"SUBTRACT","firstMax":99,"secondMax":9}',
    CURRENT_TIMESTAMP
);

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000021',
    'b0000000-0000-0000-0000-000000000001',
    'FLASHCARD_QA',
    'EASY',
    'Solve the subtraction.',
    '[]',
    45,
    '{"operation":"SUBTRACT","firstMax":99,"secondMax":99}',
    CURRENT_TIMESTAMP
);

-- MEMORY_CARD_PAIRS exercises for pair ladder tests
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'd0000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000008',
    'MEMORY_CARD_PAIRS',
    'ULTRA_EASY',
    'Find the 3 matching pairs.',
    '[]',
    60,
    '{"pairCount": 3, "symbols": ["⭐", "❤️", "🔵"]}',
    CURRENT_TIMESTAMP
);

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'd0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000008',
    'MEMORY_CARD_PAIRS',
    'EASY',
    'Find all matching pairs. Flip two cards at a time.',
    '[]',
    120,
    '{"pairCount": 4, "symbols": ["🍎", "🍊", "🍋", "🍇"]}',
    CURRENT_TIMESTAMP
);

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'd0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000008',
    'MEMORY_CARD_PAIRS',
    'MEDIUM',
    'Find all matching pairs. Flip two cards at a time.',
    '[]',
    180,
    '{"pairCount": 6, "symbols": ["🐶", "🐱", "🐰", "🐻", "🦊", "🐼"]}',
    CURRENT_TIMESTAMP
);

-- SUM_PAIR exercise for GET /api/exercises/{id} integration test
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'e0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'EASY',
    'Find pairs where first + static = second.',
    '[]',
    120,
    '{"staticNumbers": [5], "pairsPerRound": 3, "minValue": 1, "maxValue": 50}',
    CURRENT_TIMESTAMP
);

-- N_BACK_GRID for integration tests
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'c0000000-0000-0000-0000-000000000010',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK_GRID',
    'ULTRA_EASY',
    'Grid 1-Back: Tap Match when the highlighted cell is the same as 1 step back.',
    '[]',
    45,
    '{"n": 1, "sequence": [0, 4, 2, 4, 8], "matchIndices": [3], "gridSize": 3}',
    CURRENT_TIMESTAMP
);

-- DUAL_NBACK_GRID for integration tests
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'c0000000-0000-0000-0000-000000000012',
    'b0000000-0000-0000-0000-000000000004',
    'DUAL_NBACK_GRID',
    'ULTRA_EASY',
    'Dual Grid 1-Back',
    '[]',
    60,
    '{"n": 1, "sequence": [{"position": 0, "color": "#4285F4"}, {"position": 4, "color": "#EA4335"}, {"position": 0, "color": "#FBBC04"}], "matchPositionIndices": [2], "matchColorIndices": [], "colors": ["#4285F4", "#EA4335", "#FBBC04", "#34A853"], "gridSize": 3}',
    CURRENT_TIMESTAMP
);

-- SUM_PAIR with random statics (staticCount/staticMin/staticMax)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'e0000000-0000-0000-0000-000000000005',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'MEDIUM',
    'Find pairs where first + static = second. Statics vary each play.',
    '[]',
    180,
    '{"staticCount": 2, "staticMin": 2, "staticMax": 10, "pairsPerRound": 3, "minValue": 1, "maxValue": 99}',
    CURRENT_TIMESTAMP
);

-- SUM_PAIR ULTRA_EASY for pair ladder tests (static=2, small range)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'e0000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'ULTRA_EASY',
    'Find pairs where first + static = second.',
    '[]',
    90,
    '{"staticNumbers": [2], "pairsPerRound": 3, "minValue": 1, "maxValue": 30}',
    CURRENT_TIMESTAMP
);

-- SUM_PAIR HARD for pair ladder tests (3 statics, larger range)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'e0000000-0000-0000-0000-000000000004',
    'b0000000-0000-0000-0000-000000000008',
    'SUM_PAIR',
    'HARD',
    'Find sum pairs. Complete each round before the next (3 rounds).',
    '[]',
    240,
    '{"staticNumbers": [2, 5, 10], "pairsPerRound": 4, "minValue": 1, "maxValue": 99}',
    CURRENT_TIMESTAMP
);

-- DUAL_NBACK_CARD for integration tests
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'c0000000-0000-0000-0000-000000000013',
    'b0000000-0000-0000-0000-000000000004',
    'DUAL_NBACK_CARD',
    'ULTRA_EASY',
    'Dual Card 1-Back',
    '[]',
    60,
    '{"n": 1, "sequence": ["AC", "2D", "2C", "3H"], "matchColorIndices": [], "matchNumberIndices": [2]}',
    CURRENT_TIMESTAMP
);

-- Multiply (ULTRA_EASY: 2, 5, 10 × digit) for integration tests
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000030',
    'b0000000-0000-0000-0000-000000000001',
    'FLASHCARD_QA',
    'ULTRA_EASY',
    'Solve the multiplication.',
    '[]',
    30,
    '{"operation":"MULTIPLY","firstMin":1,"firstMax":10,"firstValues":[2,5,10],"secondMin":1,"secondMax":9}',
    CURRENT_TIMESTAMP
);

-- Divide (ULTRA_EASY: ÷ 2, 5, 10) for integration tests
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000040',
    'b0000000-0000-0000-0000-000000000001',
    'FLASHCARD_QA',
    'ULTRA_EASY',
    'Solve the division.',
    '[]',
    30,
    '{"operation":"DIVIDE","firstMin":1,"firstMax":9,"secondMax":10,"secondValues":[2,5,10]}',
    CURRENT_TIMESTAMP
);

-- ESTIMATION exercises for integration tests — all 5 difficulties for ladder coverage
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES
    -- ULTRA_EASY (×2)
    ('a0000000-0000-0000-0000-000000000300', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'ULTRA_EASY',
     'How many days are in a year?', '["365"]', 20,
     '{"correctAnswer":365,"unit":"days","toleranceFactor":1.03,"category":"math"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000301', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'ULTRA_EASY',
     'How tall is the Eiffel Tower (in meters)?', '["330"]', 25,
     '{"correctAnswer":330,"unit":"m","toleranceFactor":1.5,"category":"geography","hint":"It was the tallest man-made structure when built in 1889."}', CURRENT_TIMESTAMP),
    -- EASY (×2)
    ('a0000000-0000-0000-0000-000000000305', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'EASY',
     'What is the height of Mount Everest (in meters)?', '["8849"]', 30,
     '{"correctAnswer":8849,"unit":"m","toleranceFactor":1.3,"category":"geography"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000308', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'EASY',
     'Estimate: 17 × 23 = ?', '["391"]', 20,
     '{"correctAnswer":391,"unit":"","toleranceFactor":1.1,"category":"math","hint":"Decompose: 17×20 + 17×3"}', CURRENT_TIMESTAMP),
    -- MEDIUM (×2)
    ('a0000000-0000-0000-0000-000000000310', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'MEDIUM',
     'What is the approximate population of Earth (in billions)?', '["8"]', 25,
     '{"correctAnswer":8.1,"unit":"billion people","toleranceFactor":1.5,"category":"science"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000313', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'MEDIUM',
     'Estimate: e³ = ?', '["20.1"]', 25,
     '{"correctAnswer":20.086,"unit":"","toleranceFactor":1.15,"category":"math","hint":"e ≈ 2.718; e² ≈ 7.39."}', CURRENT_TIMESTAMP),
    -- HARD (×2)
    ('a0000000-0000-0000-0000-000000000315', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'HARD',
     'What is the speed of light in a vacuum (in km/s)?', '["299792"]', 30,
     '{"correctAnswer":299792,"unit":"km/s","toleranceFactor":1.1,"category":"science","hint":"It is exactly 299 792 458 m/s."}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000317', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'HARD',
     'Estimate: 7^5 = ?', '["16807"]', 30,
     '{"correctAnswer":16807,"unit":"","toleranceFactor":1.15,"category":"math","hint":"7^2=49, 7^3=343, 7^4=2401…"}', CURRENT_TIMESTAMP),
    -- VERY_HARD (×2)
    ('a0000000-0000-0000-0000-000000000318', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'VERY_HARD',
     'How many seconds are in a year?', '["31536000"]', 40,
     '{"correctAnswer":31536000,"unit":"seconds","toleranceFactor":1.05,"category":"math","hint":"365 × 24 × 60 × 60"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000319', 'b0000000-0000-0000-0000-000000000013', 'ESTIMATION', 'VERY_HARD',
     'What is the estimated age of the universe (in billions of years)?', '["13.8"]', 35,
     '{"correctAnswer":13.8,"unit":"billion years","toleranceFactor":1.3,"category":"science","hint":"Measured from the cosmic microwave background radiation."}', CURRENT_TIMESTAMP);

-- N_BACK ladder exercises: additional parametric card variants
-- These three fill the gaps for suitCount values not covered by existing test exercises (c001/c002/c003 have no suitCount key)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at) VALUES
    -- n=1, suitCount=1 (ladder level 0)
    ('c0000000-0000-0000-0000-0000000000a1', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'ULTRA_EASY', '1-Back (1 suit, parametric)', '[]', 30, '{"n":1,"suitCount":1}', CURRENT_TIMESTAMP),
    -- n=2, suitCount=2 (ladder level 5)
    ('c0000000-0000-0000-0000-0000000000a2', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'EASY', '2-Back (2 suits, parametric)', '[]', 60, '{"n":2,"suitCount":2}', CURRENT_TIMESTAMP),
    -- n=3, suitCount=4 (ladder level 9)
    ('c0000000-0000-0000-0000-0000000000a3', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'MEDIUM', '3-Back (4 suits, parametric)', '[]', 60, '{"n":3,"suitCount":4}', CURRENT_TIMESTAMP);

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at) VALUES
    ('c0000000-0000-0000-0000-000000000014', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'ULTRA_EASY', '1-Back (2 suits)', '[]', 30, '{"n":1,"suitCount":2}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000015', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'ULTRA_EASY', '1-Back (3 suits)', '[]', 30, '{"n":1,"suitCount":3}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000016', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'ULTRA_EASY', '1-Back (4 suits)', '[]', 30, '{"n":1,"suitCount":4}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000017', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'EASY', '2-Back (1 suit)', '[]', 60, '{"n":2,"suitCount":1}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000018', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'EASY', '2-Back (3 suits)', '[]', 60, '{"n":2,"suitCount":3}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000019', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'EASY', '2-Back (4 suits)', '[]', 60, '{"n":2,"suitCount":4}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000020', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'MEDIUM', '3-Back (1 suit)', '[]', 60, '{"n":3,"suitCount":1}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000021', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'MEDIUM', '3-Back (2 suits)', '[]', 60, '{"n":3,"suitCount":2}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000022', 'b0000000-0000-0000-0000-000000000004', 'N_BACK', 'MEDIUM', '3-Back (3 suits)', '[]', 60, '{"n":3,"suitCount":3}', CURRENT_TIMESTAMP);

-- N_BACK_GRID: parametric exercises (no static sequence)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at) VALUES
    ('c0000000-0000-0000-0000-000000000030', 'b0000000-0000-0000-0000-000000000004', 'N_BACK_GRID', 'ULTRA_EASY', 'Grid 1-Back 3x3 (short)', '[]', 45, '{"n":1,"gridSize":3,"sequenceLength":8}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000031', 'b0000000-0000-0000-0000-000000000004', 'N_BACK_GRID', 'ULTRA_EASY', 'Grid 1-Back 4x4', '[]', 55, '{"n":1,"gridSize":4,"sequenceLength":10}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000032', 'b0000000-0000-0000-0000-000000000004', 'N_BACK_GRID', 'EASY', 'Grid 2-Back 3x3', '[]', 55, '{"n":2,"gridSize":3,"sequenceLength":10}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000033', 'b0000000-0000-0000-0000-000000000004', 'N_BACK_GRID', 'EASY', 'Grid 2-Back 4x4', '[]', 65, '{"n":2,"gridSize":4,"sequenceLength":12}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000034', 'b0000000-0000-0000-0000-000000000004', 'N_BACK_GRID', 'MEDIUM', 'Grid 3-Back 3x3', '[]', 65, '{"n":3,"gridSize":3,"sequenceLength":10}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000035', 'b0000000-0000-0000-0000-000000000004', 'N_BACK_GRID', 'MEDIUM', 'Grid 3-Back 4x4', '[]', 75, '{"n":3,"gridSize":4,"sequenceLength":12}', CURRENT_TIMESTAMP);

-- DUAL_NBACK_GRID: parametric exercises
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at) VALUES
    ('c0000000-0000-0000-0000-000000000040', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_GRID', 'ULTRA_EASY', 'Dual Grid 1-Back (short)', '[]', 55, '{"n":1,"gridSize":3,"colorCount":4,"sequenceLength":8}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000041', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_GRID', 'EASY', 'Dual Grid 1-Back', '[]', 65, '{"n":1,"gridSize":3,"colorCount":4,"sequenceLength":12}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000042', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_GRID', 'EASY', 'Dual Grid 2-Back', '[]', 65, '{"n":2,"gridSize":3,"colorCount":4,"sequenceLength":10}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000043', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_GRID', 'MEDIUM', 'Dual Grid 2-Back 4x4', '[]', 75, '{"n":2,"gridSize":4,"colorCount":4,"sequenceLength":12}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000044', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_GRID', 'MEDIUM', 'Dual Grid 3-Back', '[]', 75, '{"n":3,"gridSize":3,"colorCount":4,"sequenceLength":10}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000045', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_GRID', 'HARD', 'Dual Grid 3-Back 4x4', '[]', 90, '{"n":3,"gridSize":4,"colorCount":4,"sequenceLength":12}', CURRENT_TIMESTAMP);

-- DUAL_NBACK_CARD: parametric exercises
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at) VALUES
    ('c0000000-0000-0000-0000-000000000050', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_CARD', 'ULTRA_EASY', 'Dual Card 1-Back (short)', '[]', 55, '{"n":1,"suitCount":4,"sequenceLength":8}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000051', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_CARD', 'EASY', 'Dual Card 1-Back', '[]', 65, '{"n":1,"suitCount":4,"sequenceLength":12}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000052', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_CARD', 'EASY', 'Dual Card 2-Back', '[]', 65, '{"n":2,"suitCount":4,"sequenceLength":10}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000053', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_CARD', 'MEDIUM', 'Dual Card 2-Back (long)', '[]', 75, '{"n":2,"suitCount":4,"sequenceLength":12}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000054', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_CARD', 'MEDIUM', 'Dual Card 3-Back', '[]', 75, '{"n":3,"suitCount":4,"sequenceLength":10}', CURRENT_TIMESTAMP),
    ('c0000000-0000-0000-0000-000000000055', 'b0000000-0000-0000-0000-000000000004', 'DUAL_NBACK_CARD', 'HARD', 'Dual Card 3-Back (long)', '[]', 90, '{"n":3,"suitCount":4,"sequenceLength":12}', CURRENT_TIMESTAMP);

-- ANAGRAM exercises for integration tests (all difficulties)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES
    ('a0000000-0000-0000-0000-000000000100', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'ULTRA_EASY', 'Trouvez le mot.', '[]', 90, '{"minLetters":2,"maxLetters":3,"language":"fr","hintIntervalSeconds":10,"letterColorHint":true}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000101', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'EASY', 'Trouvez le mot.', '[]', 120, '{"minLetters":3,"maxLetters":4,"language":"fr"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000102', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'MEDIUM', 'Trouvez le mot.', '[]', 150, '{"minLetters":4,"maxLetters":5,"language":"fr"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000103', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'HARD', 'Trouvez le mot.', '[]', 180, '{"minLetters":6,"maxLetters":7,"language":"fr"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000104', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'VERY_HARD', 'Trouvez le mot.', '[]', 210, '{"minLetters":8,"maxLetters":15,"language":"fr","hintIntervalSeconds":15,"letterColorHint":false}', CURRENT_TIMESTAMP);

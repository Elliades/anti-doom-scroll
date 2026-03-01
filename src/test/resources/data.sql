-- Idempotent: clear so multiple test classes sharing the same in-memory DB can re-run this script
DELETE FROM exercise;
DELETE FROM subject;

-- Subjects for tests
INSERT INTO subject (id, code, name, description, scoring_config, created_at)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'default', 'Default', 'Default subject', '{"accuracyType":"BINARY","speedTargetMs":10000,"confidenceWeight":0,"streakBonusCap":0.1}', CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000004', 'B1', 'N-back', 'Working memory', '{}', CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000008', 'MEMORY', 'Memory', 'Memory games', '{}', CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000010', 'WORD', 'Word', 'Word games', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}', CURRENT_TIMESTAMP);

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

-- ANAGRAM exercises for integration tests (all difficulties)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES
    ('a0000000-0000-0000-0000-000000000100', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'ULTRA_EASY', 'Trouvez le mot.', '[]', 90, '{"minLetters":2,"maxLetters":3,"language":"fr","hintIntervalSeconds":10,"letterColorHint":true}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000101', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'EASY', 'Trouvez le mot.', '[]', 120, '{"minLetters":3,"maxLetters":4,"language":"fr"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000102', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'MEDIUM', 'Trouvez le mot.', '[]', 150, '{"minLetters":4,"maxLetters":5,"language":"fr"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000103', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'HARD', 'Trouvez le mot.', '[]', 180, '{"minLetters":6,"maxLetters":7,"language":"fr"}', CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000104', 'b0000000-0000-0000-0000-000000000010', 'ANAGRAM', 'VERY_HARD', 'Trouvez le mot.', '[]', 210, '{"minLetters":8,"maxLetters":15,"language":"fr","hintIntervalSeconds":15,"letterColorHint":false}', CURRENT_TIMESTAMP);

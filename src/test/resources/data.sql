-- Subjects for tests
INSERT INTO subject (id, code, name, description, scoring_config, created_at)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'default', 'Default', 'Default subject', '{"accuracyType":"BINARY","speedTargetMs":10000,"confidenceWeight":0,"streakBonusCap":0.1}', CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000004', 'B1', 'N-back', 'Working memory', '{}', CURRENT_TIMESTAMP);

-- One ultra-easy exercise for integration tests (H2)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000001',
    'FLASHCARD_QA',
    'ULTRA_EASY',
    'What is 1 + 1?',
    '["2"]',
    30,
    CURRENT_TIMESTAMP
);

-- Ultra-easy N-back exercise for integration tests
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES (
    'c0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK',
    'ULTRA_EASY',
    '1-Back: Tap when the letter matches the previous one.',
    '[]',
    30,
    '{"n": 1, "sequence": ["A","B","A","C","C","D","E","F","E","G","H","H"], "matchIndices": [2, 4, 7, 11]}',
    CURRENT_TIMESTAMP
);

-- Third exercise for openapp session (3 steps)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000001',
    'FLASHCARD_QA',
    'EASY',
    'What is 2 + 2?',
    '["4"]',
    45,
    CURRENT_TIMESTAMP
);

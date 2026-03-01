-- Add exercise_params for type-specific data (e.g. N-Back: sequence, matchIndices)
ALTER TABLE exercise ADD COLUMN IF NOT EXISTS exercise_params JSONB;

-- Seed ultra-easy 1-back in subject B1 (N-back)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'c0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000004',
    'N_BACK',
    'ULTRA_EASY',
    '1-Back: Tap when the card matches the previous one.',
    '[]',
    30,
    '{"n": 1, "sequence": ["AC","AC","2D","3H","4S","4S","5C","6D","7H","7H","8S","9C"], "matchIndices": [1, 5, 9]}'
) ON CONFLICT (id) DO NOTHING;

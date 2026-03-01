-- Update N-Back exercises to use playing card sequences instead of letters.
-- 1-back: match when current card == previous
-- 2-back: match when current card == 2 steps back
-- 3-back: match when current card == 3 steps back

UPDATE exercise SET exercise_params = '{"n": 1, "sequence": ["AC","AC","2D","3H","4S","4S","5C","6D","7H","7H","8S","9C"], "matchIndices": [1, 5, 9]}'
WHERE id = 'c0000000-0000-0000-0000-000000000001' AND type = 'N_BACK';

UPDATE exercise SET exercise_params = '{"n": 2, "sequence": ["AC","AC","3H","AC","4S","AC","5C","6D","5C","7S","8C","4S","9D"], "matchIndices": [3, 5, 8, 12]}'
WHERE id = 'c0000000-0000-0000-0000-000000000002' AND type = 'N_BACK';

UPDATE exercise SET exercise_params = '{"n": 3, "sequence": ["AC","AC","3H","4S","AC","5C","6D","AC","7H","8S","AC"], "matchIndices": [4, 7, 10]}'
WHERE id = 'c0000000-0000-0000-0000-000000000003' AND type = 'N_BACK';

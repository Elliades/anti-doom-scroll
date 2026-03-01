-- Increase pairs per exercise and update prompts for single-board colored flow.
-- Uses CAST to support both PostgreSQL (jsonb) and H2 (parsed as JSON).
UPDATE exercise SET exercise_params = CAST('{"staticNumbers": [5], "pairsPerRound": 6, "minValue": 1, "maxValue": 50}' AS JSON)
WHERE id = 'e0000000-0000-0000-0000-000000000001' AND type = 'SUM_PAIR';

UPDATE exercise SET exercise_params = CAST('{"staticNumbers": [3, 7], "pairsPerRound": 5, "minValue": 1, "maxValue": 99}' AS JSON)
WHERE id = 'e0000000-0000-0000-0000-000000000002' AND type = 'SUM_PAIR';

UPDATE exercise SET exercise_params = CAST('{"staticNumbers": [2], "pairsPerRound": 4, "minValue": 1, "maxValue": 30}' AS JSON)
WHERE id = 'e0000000-0000-0000-0000-000000000003' AND type = 'SUM_PAIR';

UPDATE exercise SET exercise_params = CAST('{"staticNumbers": [2, 5, 10], "pairsPerRound": 6, "minValue": 1, "maxValue": 99}' AS JSON)
WHERE id = 'e0000000-0000-0000-0000-000000000004' AND type = 'SUM_PAIR';

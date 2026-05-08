-- Wordle: explicit wordLength + calibrated difficulty score (0=easiest, 100=hardest) for filtering / generation.
-- Values match WordleComplexity.compute for existing prompts and time limits.

UPDATE exercise
SET exercise_params = coalesce(exercise_params, '{}'::jsonb) || '{"wordLength":3,"wordleDifficultyScore0To100":25}'::jsonb
WHERE id = 'a0000000-0000-0000-0000-000000000200';

UPDATE exercise
SET exercise_params = coalesce(exercise_params, '{}'::jsonb) || '{"wordLength":5,"wordleDifficultyScore0To100":43}'::jsonb
WHERE id = 'a0000000-0000-0000-0000-000000000201';

UPDATE exercise
SET exercise_params = coalesce(exercise_params, '{}'::jsonb) || '{"wordLength":6,"wordleDifficultyScore0To100":48}'::jsonb
WHERE id = 'a0000000-0000-0000-0000-000000000202';

UPDATE exercise
SET exercise_params = coalesce(exercise_params, '{}'::jsonb) || '{"wordLength":7,"wordleDifficultyScore0To100":53}'::jsonb
WHERE id = 'a0000000-0000-0000-0000-000000000203';

UPDATE exercise
SET exercise_params = coalesce(exercise_params, '{}'::jsonb) || '{"wordLength":3,"wordleDifficultyScore0To100":24}'::jsonb
WHERE id = 'a0000000-0000-0000-0000-000000000210';

UPDATE exercise
SET exercise_params = coalesce(exercise_params, '{}'::jsonb) || '{"wordLength":5,"wordleDifficultyScore0To100":42}'::jsonb
WHERE id = 'a0000000-0000-0000-0000-000000000211';

UPDATE exercise
SET exercise_params = coalesce(exercise_params, '{}'::jsonb) || '{"wordLength":6,"wordleDifficultyScore0To100":47}'::jsonb
WHERE id = 'a0000000-0000-0000-0000-000000000212';

UPDATE exercise
SET exercise_params = coalesce(exercise_params, '{}'::jsonb) || '{"wordLength":7,"wordleDifficultyScore0To100":52}'::jsonb
WHERE id = 'a0000000-0000-0000-0000-000000000213';

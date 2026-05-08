-- REMEMBER_NUMBER exercises under MEMORY subject.
-- Actual number + math problem are generated at response time via RememberNumberGenerator.

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT
    'f3000000-0000-0000-0000-000000000001'::uuid,
    s.id, 'REMEMBER_NUMBER', 'ULTRA_EASY',
    'Remember the number, solve the math problem, then recall the number.',
    '[]', 60,
    '{"numberDigits":2,"displayTimeMs":3000,"mathOperation":"ADD","mathFirstMax":9,"mathSecondMax":9}'::jsonb
FROM subject s WHERE s.code = 'MEMORY'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT
    'f3000000-0000-0000-0000-000000000002'::uuid,
    s.id, 'REMEMBER_NUMBER', 'EASY',
    'Remember the number, solve the math problem, then recall the number.',
    '[]', 60,
    '{"numberDigits":3,"displayTimeMs":2500,"mathOperation":"ADD","mathFirstMax":99,"mathSecondMax":9}'::jsonb
FROM subject s WHERE s.code = 'MEMORY'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT
    'f3000000-0000-0000-0000-000000000003'::uuid,
    s.id, 'REMEMBER_NUMBER', 'MEDIUM',
    'Remember the number, solve the math problem, then recall the number.',
    '[]', 90,
    '{"numberDigits":4,"displayTimeMs":2000,"mathOperation":"SUBTRACT","mathFirstMax":99,"mathSecondMax":99}'::jsonb
FROM subject s WHERE s.code = 'MEMORY'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT
    'f3000000-0000-0000-0000-000000000004'::uuid,
    s.id, 'REMEMBER_NUMBER', 'HARD',
    'Remember the number, solve the math problem, then recall the number.',
    '[]', 90,
    '{"numberDigits":5,"displayTimeMs":1500,"mathOperation":"MULTIPLY","mathFirstMax":12,"mathSecondMax":9}'::jsonb
FROM subject s WHERE s.code = 'MEMORY'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT
    'f3000000-0000-0000-0000-000000000005'::uuid,
    s.id, 'REMEMBER_NUMBER', 'VERY_HARD',
    'Remember the number, solve the math problem, then recall the number.',
    '[]', 120,
    '{"numberDigits":6,"displayTimeMs":1200,"mathOperation":"MULTIPLY","mathFirstMax":99,"mathSecondMax":9}'::jsonb
FROM subject s WHERE s.code = 'MEMORY'
ON CONFLICT (id) DO NOTHING;

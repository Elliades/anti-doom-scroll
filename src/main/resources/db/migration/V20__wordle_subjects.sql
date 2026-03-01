-- Wordle subjects: French and English.
-- Difficulty → word length: EASY=3, MEDIUM=5, HARD=6, VERY_HARD=7.
-- language stored in exercise_params.

INSERT INTO subject (id, code, name, description, scoring_config)
VALUES (
    'b0000000-0000-0000-0000-000000000011',
    'WORDLE_FR',
    'Wordle (FR)',
    'Devinez le mot en 6 essais. Lettres vertes = bonne position, jaunes = mauvaise position.',
    '{"accuracyType":"BINARY","speedTargetMs":120000,"confidenceWeight":0.1,"streakBonusCap":0.1}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO subject (id, code, name, description, scoring_config)
VALUES (
    'b0000000-0000-0000-0000-000000000012',
    'WORDLE_EN',
    'Wordle (EN)',
    'Guess the word in 6 tries. Green = right letter & position, yellow = right letter wrong position.',
    '{"accuracyType":"BINARY","speedTargetMs":120000,"confidenceWeight":0.1,"streakBonusCap":0.1}'
) ON CONFLICT (id) DO NOTHING;

-- WORDLE_FR exercises (EASY=3 letters, MEDIUM=5, HARD=6, VERY_HARD=7)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000200',
    'b0000000-0000-0000-0000-000000000011',
    'WORDLE',
    'EASY',
    'Devinez le mot de 3 lettres en 6 essais.',
    '[]',
    120,
    '{"language": "fr", "maxAttempts": 6}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000201',
    'b0000000-0000-0000-0000-000000000011',
    'WORDLE',
    'MEDIUM',
    'Devinez le mot de 5 lettres en 6 essais.',
    '[]',
    180,
    '{"language": "fr", "maxAttempts": 6}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000202',
    'b0000000-0000-0000-0000-000000000011',
    'WORDLE',
    'HARD',
    'Devinez le mot de 6 lettres en 6 essais.',
    '[]',
    240,
    '{"language": "fr", "maxAttempts": 6}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000203',
    'b0000000-0000-0000-0000-000000000011',
    'WORDLE',
    'VERY_HARD',
    'Devinez le mot de 7 lettres en 6 essais.',
    '[]',
    300,
    '{"language": "fr", "maxAttempts": 6}'
) ON CONFLICT (id) DO NOTHING;

-- WORDLE_EN exercises (EASY=3 letters, MEDIUM=5, HARD=6, VERY_HARD=7)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000210',
    'b0000000-0000-0000-0000-000000000012',
    'WORDLE',
    'EASY',
    'Guess the 3-letter word in 6 tries.',
    '[]',
    120,
    '{"language": "en", "maxAttempts": 6}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000211',
    'b0000000-0000-0000-0000-000000000012',
    'WORDLE',
    'MEDIUM',
    'Guess the 5-letter word in 6 tries.',
    '[]',
    180,
    '{"language": "en", "maxAttempts": 6}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000212',
    'b0000000-0000-0000-0000-000000000012',
    'WORDLE',
    'HARD',
    'Guess the 6-letter word in 6 tries.',
    '[]',
    240,
    '{"language": "en", "maxAttempts": 6}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000213',
    'b0000000-0000-0000-0000-000000000012',
    'WORDLE',
    'VERY_HARD',
    'Guess the 7-letter word in 6 tries.',
    '[]',
    300,
    '{"language": "en", "maxAttempts": 6}'
) ON CONFLICT (id) DO NOTHING;

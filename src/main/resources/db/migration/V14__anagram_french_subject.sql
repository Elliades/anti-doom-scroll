-- Anagram subject (French). Letter range per difficulty: ULTRA_EASY 2-3, EASY 3-4, MEDIUM 4-5, HARD 5+.
-- Language from exercise_params (fr). English anagram subject can be added later with language: en.

INSERT INTO subject (id, code, name, description, scoring_config)
VALUES (
    'b0000000-0000-0000-0000-000000000009',
    'ANAGRAM_FR',
    'Anagrammes (FR)',
    'Trouvez le mot à partir des lettres mélangées. Hints toutes les 5 secondes.',
    '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'
) ON CONFLICT (id) DO NOTHING;

-- Ultra-easy: 2-3 letters
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000100',
    'b0000000-0000-0000-0000-000000000009',
    'ANAGRAM',
    'ULTRA_EASY',
    'Trouvez le mot à partir des lettres affichées.',
    '[]',
    90,
    '{"minLetters": 2, "maxLetters": 3, "language": "fr"}'
) ON CONFLICT (id) DO NOTHING;

-- Easy: 3-4 letters
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000101',
    'b0000000-0000-0000-0000-000000000009',
    'ANAGRAM',
    'EASY',
    'Trouvez le mot à partir des lettres affichées.',
    '[]',
    120,
    '{"minLetters": 3, "maxLetters": 4, "language": "fr"}'
) ON CONFLICT (id) DO NOTHING;

-- Medium: 4-5 letters
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000102',
    'b0000000-0000-0000-0000-000000000009',
    'ANAGRAM',
    'MEDIUM',
    'Trouvez le mot à partir des lettres affichées.',
    '[]',
    150,
    '{"minLetters": 4, "maxLetters": 5, "language": "fr"}'
) ON CONFLICT (id) DO NOTHING;

-- Hard: 5+ letters
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000103',
    'b0000000-0000-0000-0000-000000000009',
    'ANAGRAM',
    'HARD',
    'Trouvez le mot à partir des lettres affichées.',
    '[]',
    180,
    '{"minLetters": 5, "maxLetters": 10, "language": "fr"}'
) ON CONFLICT (id) DO NOTHING;

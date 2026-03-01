-- Anagram difficulties: MEDIUM 4-5, HARD 6-7, VERY_HARD 8+.
-- Update HARD exercise params; add VERY_HARD exercise.

UPDATE exercise
SET exercise_params = '{"minLetters": 6, "maxLetters": 7, "language": "fr"}'
WHERE id = 'a0000000-0000-0000-0000-000000000103' AND type = 'ANAGRAM';

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000104',
    'b0000000-0000-0000-0000-000000000010',
    'ANAGRAM',
    'VERY_HARD',
    'Trouvez le mot à partir des lettres affichées.',
    '[]',
    210,
    '{"minLetters": 8, "maxLetters": 15, "language": "fr"}'
) ON CONFLICT (id) DO NOTHING;

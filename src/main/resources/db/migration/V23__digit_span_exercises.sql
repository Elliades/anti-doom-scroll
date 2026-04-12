-- DIGIT_SPAN: working memory digit span (memorize, recall order + variants, optional progressive length).
-- Reuses MEMORY subject (b0000000-0000-0000-0000-000000000008).

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'f2000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000008',
    'DIGIT_SPAN',
    'ULTRA_EASY',
    'Mémorisez les chiffres, puis saisissez-les dans l''ordre. La série s''allonge après chaque réussite complète.',
    '[]',
    300,
    '{"length":3,"minDigit":0,"maxDigit":9,"displaySeconds":3,"progressive":true,"maxLength":6,"tasks":["FORWARD_ORDER"]}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'f2000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000008',
    'DIGIT_SPAN',
    'EASY',
    'Mémorisez les chiffres, puis deux rappels : ordre d''affichage, puis ordre croissant.',
    '[]',
    300,
    '{"length":3,"minDigit":0,"maxDigit":9,"displaySeconds":3,"progressive":true,"maxLength":7,"tasks":["FORWARD_ORDER","ASCENDING"]}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'f2000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000008',
    'DIGIT_SPAN',
    'MEDIUM',
    'Trois rappels : ordre, croissant, décroissant — puis la série grandit.',
    '[]',
    300,
    '{"length":4,"minDigit":0,"maxDigit":9,"displaySeconds":3,"progressive":true,"maxLength":8,"tasks":["FORWARD_ORDER","ASCENDING","DESCENDING"]}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'f2000000-0000-0000-0000-000000000004',
    'b0000000-0000-0000-0000-000000000008',
    'DIGIT_SPAN',
    'HARD',
    'Quatre modes de rappel : ordre, croissant, décroissant, puis pairs puis impairs.',
    '[]',
    300,
    '{"length":4,"minDigit":0,"maxDigit":9,"displaySeconds":3,"progressive":true,"maxLength":10,"tasks":["FORWARD_ORDER","ASCENDING","DESCENDING","EVEN_THEN_ODD"]}'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'f2000000-0000-0000-0000-000000000005',
    'b0000000-0000-0000-0000-000000000008',
    'DIGIT_SPAN',
    'VERY_HARD',
    'Cinq rappels dont un sur deux — la série continue jusqu''à erreur ou limite.',
    '[]',
    420,
    '{"length":4,"minDigit":0,"maxDigit":9,"displaySeconds":3,"progressive":true,"maxLength":12,"tasks":["FORWARD_ORDER","ASCENDING","DESCENDING","EVEN_THEN_ODD","EVERY_OTHER_FROM_FIRST"]}'
) ON CONFLICT (id) DO NOTHING;

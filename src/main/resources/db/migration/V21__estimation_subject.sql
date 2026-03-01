-- ESTIMATION subject: approximate numerical answers scored by time + accuracy (logarithmic scale).
-- Scoring formula (frontend): score = max(0, 1 − |ln(answer/correct)| / ln(toleranceFactor))
-- Categories: geography, science, math, history
-- Difficulties: ULTRA_EASY (everyday), EASY (school), MEDIUM (culture), HARD (expert), VERY_HARD (specialist)
-- Subject ID b0000000-0000-0000-0000-000000000013, exercises a0000000-0000-0000-0000-0003XXXXXXXX

INSERT INTO subject (id, code, name, description, scoring_config)
VALUES (
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION',
    'Estimation',
    'Approximate numerical answers: monuments, distances, populations, mental math. Scored on accuracy and speed.',
    '{"accuracyType":"PARTIAL","speedTargetMs":15000,"confidenceWeight":0.0,"streakBonusCap":0.1}'
) ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- ULTRA_EASY — everyday knowledge, wide tolerance
-- =====================================================================

-- Days in a year (math/calendar)
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000300',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'ULTRA_EASY',
    'How many days are in a year?',
    '["365"]', 20,
    '{"correctAnswer":365,"unit":"days","toleranceFactor":1.03,"category":"math"}'
) ON CONFLICT (id) DO NOTHING;

-- Height of Eiffel Tower
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000301',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'ULTRA_EASY',
    'How tall is the Eiffel Tower (in meters)?',
    '["330"]', 25,
    '{"correctAnswer":330,"unit":"m","toleranceFactor":1.5,"category":"geography","hint":"It was the tallest man-made structure when built in 1889."}'
) ON CONFLICT (id) DO NOTHING;

-- Speed of sound in air
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000302',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'ULTRA_EASY',
    'What is the speed of sound in air (m/s)?',
    '["343"]', 25,
    '{"correctAnswer":343,"unit":"m/s","toleranceFactor":1.3,"category":"science","hint":"It depends on temperature; ~20°C."}'
) ON CONFLICT (id) DO NOTHING;

-- π × 10
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000303',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'ULTRA_EASY',
    'Estimate: π × 10 = ?',
    '["31.4"]', 15,
    '{"correctAnswer":31.416,"unit":"","toleranceFactor":1.05,"category":"math","hint":"π ≈ 3.14159…"}'
) ON CONFLICT (id) DO NOTHING;

-- Hours in a week
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000304',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'ULTRA_EASY',
    'How many hours are in a week?',
    '["168"]', 20,
    '{"correctAnswer":168,"unit":"hours","toleranceFactor":1.05,"category":"math"}'
) ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- EASY — school-level general knowledge
-- =====================================================================

-- Height of Mount Everest
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000305',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'EASY',
    'What is the height of Mount Everest (in meters)?',
    '["8849"]', 30,
    '{"correctAnswer":8849,"unit":"m","toleranceFactor":1.3,"category":"geography","hint":"It is the highest peak on Earth."}'
) ON CONFLICT (id) DO NOTHING;

-- Distance Paris to New York
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000306',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'EASY',
    'What is the straight-line distance from Paris to New York (in km)?',
    '["5837"]', 30,
    '{"correctAnswer":5837,"unit":"km","toleranceFactor":2.0,"category":"geography"}'
) ON CONFLICT (id) DO NOTHING;

-- Population of France
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000307',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'EASY',
    'What is the population of France (in millions)?',
    '["68"]', 30,
    '{"correctAnswer":68,"unit":"million people","toleranceFactor":1.5,"category":"geography"}'
) ON CONFLICT (id) DO NOTHING;

-- 17 × 23
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000308',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'EASY',
    'Estimate: 17 × 23 = ?',
    '["391"]', 20,
    '{"correctAnswer":391,"unit":"","toleranceFactor":1.1,"category":"math","hint":"Decompose: 17×20 + 17×3"}'
) ON CONFLICT (id) DO NOTHING;

-- √200
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000309',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'EASY',
    'Estimate: √200 = ?',
    '["14.1"]', 20,
    '{"correctAnswer":14.142,"unit":"","toleranceFactor":1.08,"category":"math","hint":"Between √196=14 and √225=15."}'
) ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- MEDIUM — cultural knowledge + harder math
-- =====================================================================

-- Population of Earth
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000310',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'MEDIUM',
    'What is the approximate population of Earth (in billions)?',
    '["8"]', 25,
    '{"correctAnswer":8.1,"unit":"billion people","toleranceFactor":1.5,"category":"science"}'
) ON CONFLICT (id) DO NOTHING;

-- Distance Earth → Moon
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000311',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'MEDIUM',
    'What is the average distance from Earth to the Moon (in km)?',
    '["384400"]', 35,
    '{"correctAnswer":384400,"unit":"km","toleranceFactor":2.0,"category":"science","hint":"Light takes about 1.3 seconds to travel that distance."}'
) ON CONFLICT (id) DO NOTHING;

-- Area of France
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000312',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'MEDIUM',
    'What is the area of France (in km²)?',
    '["551695"]', 35,
    '{"correctAnswer":551695,"unit":"km²","toleranceFactor":2.0,"category":"geography","hint":"It is the largest country in the EU."}'
) ON CONFLICT (id) DO NOTHING;

-- e^3
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000313',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'MEDIUM',
    'Estimate: e³ = ?',
    '["20.1"]', 25,
    '{"correctAnswer":20.086,"unit":"","toleranceFactor":1.15,"category":"math","hint":"e ≈ 2.718; e² ≈ 7.39."}'
) ON CONFLICT (id) DO NOTHING;

-- 2^10
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000314',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'MEDIUM',
    'What is 2^10?',
    '["1024"]', 20,
    '{"correctAnswer":1024,"unit":"","toleranceFactor":1.05,"category":"math","hint":"Powers of 2 double each time: 2, 4, 8, 16…"}'
) ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- HARD — expert-level, requires deep knowledge or strong mental math
-- =====================================================================

-- Speed of light
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000315',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'HARD',
    'What is the speed of light in a vacuum (in km/s)?',
    '["299792"]', 30,
    '{"correctAnswer":299792,"unit":"km/s","toleranceFactor":1.1,"category":"science","hint":"It is exactly 299 792 458 m/s."}'
) ON CONFLICT (id) DO NOTHING;

-- Distance Earth → Sun
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000316',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'HARD',
    'What is the average distance from Earth to the Sun (in millions of km)?',
    '["150"]', 30,
    '{"correctAnswer":149.6,"unit":"million km","toleranceFactor":1.5,"category":"science","hint":"This distance is called 1 astronomical unit (1 AU)."}'
) ON CONFLICT (id) DO NOTHING;

-- 7^5
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000317',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'HARD',
    'Estimate: 7^5 = ?',
    '["16807"]', 30,
    '{"correctAnswer":16807,"unit":"","toleranceFactor":1.15,"category":"math","hint":"7^2=49, 7^3=343, 7^4=2401…"}'
) ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- VERY_HARD — specialist knowledge or precise mental math
-- =====================================================================

-- Seconds in a year
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000318',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'VERY_HARD',
    'How many seconds are in a year?',
    '["31536000"]', 40,
    '{"correctAnswer":31536000,"unit":"seconds","toleranceFactor":1.05,"category":"math","hint":"365 × 24 × 60 × 60"}'
) ON CONFLICT (id) DO NOTHING;

-- Age of the universe
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000319',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'VERY_HARD',
    'What is the estimated age of the universe (in billions of years)?',
    '["13.8"]', 35,
    '{"correctAnswer":13.8,"unit":"billion years","toleranceFactor":1.3,"category":"science","hint":"Measured from the cosmic microwave background radiation."}'
) ON CONFLICT (id) DO NOTHING;

-- 2^20
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
VALUES (
    'a0000000-0000-0000-0000-000000000320',
    'b0000000-0000-0000-0000-000000000013',
    'ESTIMATION', 'VERY_HARD',
    'What is 2^20 (exact)?',
    '["1048576"]', 30,
    '{"correctAnswer":1048576,"unit":"","toleranceFactor":1.05,"category":"math","hint":"2^10 = 1 024, so 2^20 = 1 024²."}'
) ON CONFLICT (id) DO NOTHING;

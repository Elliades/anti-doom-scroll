-- N-Back ladder exercises: parametric variants for the 30-level nback ladder.
-- Card (N_BACK): n=1..3 × suitCount=1..4 (sequences generated at request time from n+suitCount).
-- Grid (N_BACK_GRID): n=1..3 × gridSize=3..4 (sequences generated at request time from n+gridSize).
-- Dual Grid (DUAL_NBACK_GRID): n=1..3 × gridSize=3..4 (position+color, generated at request time).
-- Dual Card (DUAL_NBACK_CARD): n=1..3 (suit+rank, generated at request time).
-- IDs: c0000000-0000-0000-0000-0000000000XX (XX = 14..55)

-- ── N_BACK: 1-back with increasing suit count ──────────────────────────────────────────────────
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000014',
       s.id, 'N_BACK', 'ULTRA_EASY',
       '1-Back: Tap when the card matches the previous one. (2 suits)',
       '[]', 30, '{"n":1,"suitCount":2}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000015',
       s.id, 'N_BACK', 'ULTRA_EASY',
       '1-Back: Tap when the card matches the previous one. (3 suits)',
       '[]', 30, '{"n":1,"suitCount":3}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000016',
       s.id, 'N_BACK', 'ULTRA_EASY',
       '1-Back: Tap when the card matches the previous one. (4 suits)',
       '[]', 30, '{"n":1,"suitCount":4}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

-- ── N_BACK: 2-back with increasing suit count ──────────────────────────────────────────────────
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000017',
       s.id, 'N_BACK', 'EASY',
       '2-Back: Tap when the card matches the one from 2 steps back. (1 suit)',
       '[]', 60, '{"n":2,"suitCount":1}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000018',
       s.id, 'N_BACK', 'EASY',
       '2-Back: Tap when the card matches the one from 2 steps back. (3 suits)',
       '[]', 60, '{"n":2,"suitCount":3}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000019',
       s.id, 'N_BACK', 'EASY',
       '2-Back: Tap when the card matches the one from 2 steps back. (4 suits)',
       '[]', 60, '{"n":2,"suitCount":4}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

-- ── N_BACK: 3-back with increasing suit count ──────────────────────────────────────────────────
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000020',
       s.id, 'N_BACK', 'MEDIUM',
       '3-Back: Tap when the card matches the one from 3 steps back. (1 suit)',
       '[]', 60, '{"n":3,"suitCount":1}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000021',
       s.id, 'N_BACK', 'MEDIUM',
       '3-Back: Tap when the card matches the one from 3 steps back. (2 suits)',
       '[]', 60, '{"n":3,"suitCount":2}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000022',
       s.id, 'N_BACK', 'MEDIUM',
       '3-Back: Tap when the card matches the one from 3 steps back. (3 suits)',
       '[]', 60, '{"n":3,"suitCount":3}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

-- ── N_BACK_GRID: parametric (no static sequence — generated at request time) ────────────────────
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000030',
       s.id, 'N_BACK_GRID', 'ULTRA_EASY',
       'Grid 1-Back: Tap Match when the highlighted cell matches 1 step back. (3×3)',
       '[]', 45, '{"n":1,"gridSize":3,"sequenceLength":8}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000031',
       s.id, 'N_BACK_GRID', 'ULTRA_EASY',
       'Grid 1-Back: Tap Match when the highlighted cell matches 1 step back. (4×4)',
       '[]', 55, '{"n":1,"gridSize":4,"sequenceLength":10}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000032',
       s.id, 'N_BACK_GRID', 'EASY',
       'Grid 2-Back: Tap Match when the cell matches 2 steps back. (3×3)',
       '[]', 55, '{"n":2,"gridSize":3,"sequenceLength":10}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000033',
       s.id, 'N_BACK_GRID', 'EASY',
       'Grid 2-Back: Tap Match when the cell matches 2 steps back. (4×4)',
       '[]', 65, '{"n":2,"gridSize":4,"sequenceLength":12}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000034',
       s.id, 'N_BACK_GRID', 'MEDIUM',
       'Grid 3-Back: Tap Match when the cell matches 3 steps back. (3×3)',
       '[]', 65, '{"n":3,"gridSize":3,"sequenceLength":10}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000035',
       s.id, 'N_BACK_GRID', 'MEDIUM',
       'Grid 3-Back: Tap Match when the cell matches 3 steps back. (4×4)',
       '[]', 75, '{"n":3,"gridSize":4,"sequenceLength":12}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

-- ── DUAL_NBACK_GRID: parametric (position+color, generated at request time) ─────────────────────
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000040',
       s.id, 'DUAL_NBACK_GRID', 'ULTRA_EASY',
       'Dual Grid 1-Back (short): Tap Match Position or Match Color when that attribute matches 1 step back.',
       '[]', 55, '{"n":1,"gridSize":3,"colorCount":4,"sequenceLength":8}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000041',
       s.id, 'DUAL_NBACK_GRID', 'EASY',
       'Dual Grid 1-Back: Tap Match Position or Match Color when that attribute matches 1 step back.',
       '[]', 65, '{"n":1,"gridSize":3,"colorCount":4,"sequenceLength":12}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000042',
       s.id, 'DUAL_NBACK_GRID', 'EASY',
       'Dual Grid 2-Back: Tap Match Position or Match Color when that attribute matches 2 steps back.',
       '[]', 65, '{"n":2,"gridSize":3,"colorCount":4,"sequenceLength":10}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000043',
       s.id, 'DUAL_NBACK_GRID', 'MEDIUM',
       'Dual Grid 2-Back (4×4): Tap Match Position or Match Color when that attribute matches 2 steps back.',
       '[]', 75, '{"n":2,"gridSize":4,"colorCount":4,"sequenceLength":12}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000044',
       s.id, 'DUAL_NBACK_GRID', 'MEDIUM',
       'Dual Grid 3-Back: Tap Match Position or Match Color when that attribute matches 3 steps back.',
       '[]', 75, '{"n":3,"gridSize":3,"colorCount":4,"sequenceLength":10}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000045',
       s.id, 'DUAL_NBACK_GRID', 'HARD',
       'Dual Grid 3-Back (4×4): Tap Match Position or Match Color when that attribute matches 3 steps back.',
       '[]', 90, '{"n":3,"gridSize":4,"colorCount":4,"sequenceLength":12}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

-- ── DUAL_NBACK_CARD: parametric (suit+rank, generated at request time) ──────────────────────────
INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000050',
       s.id, 'DUAL_NBACK_CARD', 'ULTRA_EASY',
       'Dual Card 1-Back (short): Tap Match Color (suit) or Match Number (rank) when it matches 1 step back.',
       '[]', 55, '{"n":1,"suitCount":4,"sequenceLength":8}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000051',
       s.id, 'DUAL_NBACK_CARD', 'EASY',
       'Dual Card 1-Back: Tap Match Color (suit) or Match Number (rank) when it matches 1 step back.',
       '[]', 65, '{"n":1,"suitCount":4,"sequenceLength":12}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000052',
       s.id, 'DUAL_NBACK_CARD', 'EASY',
       'Dual Card 2-Back: Tap Match Color (suit) or Match Number (rank) when it matches 2 steps back.',
       '[]', 65, '{"n":2,"suitCount":4,"sequenceLength":10}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000053',
       s.id, 'DUAL_NBACK_CARD', 'MEDIUM',
       'Dual Card 2-Back (long): Tap Match Color (suit) or Match Number (rank) when it matches 2 steps back.',
       '[]', 75, '{"n":2,"suitCount":4,"sequenceLength":12}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000054',
       s.id, 'DUAL_NBACK_CARD', 'MEDIUM',
       'Dual Card 3-Back: Tap Match Color (suit) or Match Number (rank) when it matches 3 steps back.',
       '[]', 75, '{"n":3,"suitCount":4,"sequenceLength":10}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params)
SELECT 'c0000000-0000-0000-0000-000000000055',
       s.id, 'DUAL_NBACK_CARD', 'HARD',
       'Dual Card 3-Back (long): Tap Match Color (suit) or Match Number (rank) when it matches 3 steps back.',
       '[]', 90, '{"n":3,"suitCount":4,"sequenceLength":12}'
FROM subject s WHERE s.code = 'B1'
ON CONFLICT (id) DO NOTHING;

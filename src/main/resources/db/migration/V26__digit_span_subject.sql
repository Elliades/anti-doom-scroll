-- Digit Span: progressive digit recall with challenge modes (ascending, descending, even/odd, every-other)
INSERT INTO subject (id, code, name, description, scoring_config, created_at)
VALUES (
    'b0000000-0000-0000-0000-000000000014',
    'DIGIT_SPAN',
    'Digit Span',
    'Working memory: memorize digits and recall them in order, ascending, descending, even/odd, or every-other.',
    '{"accuracyType":"BINARY","speedTargetMs":10000,"confidenceWeight":0,"streakBonusCap":0.1}',
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise (id, subject_id, type, difficulty, prompt, expected_answers, time_limit_seconds, exercise_params, created_at)
VALUES
    ('f2000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000014', 'DIGIT_SPAN', 'ULTRA_EASY',
     'Memorize the digits, then type them back.', '[]', 300,
     '{"startLength":3,"displayTimeMs":3000,"maxLength":15}', CURRENT_TIMESTAMP),
    ('f2000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000014', 'DIGIT_SPAN', 'EASY',
     'Memorize the digits, then type them back.', '[]', 300,
     '{"startLength":4,"displayTimeMs":3000,"maxLength":15}', CURRENT_TIMESTAMP),
    ('f2000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000014', 'DIGIT_SPAN', 'MEDIUM',
     'Memorize the digits, then type them back.', '[]', 300,
     '{"startLength":5,"displayTimeMs":2500,"maxLength":15}', CURRENT_TIMESTAMP),
    ('f2000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000014', 'DIGIT_SPAN', 'HARD',
     'Memorize the digits, then type them back.', '[]', 300,
     '{"startLength":6,"displayTimeMs":2000,"maxLength":15}', CURRENT_TIMESTAMP),
    ('f2000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000014', 'DIGIT_SPAN', 'VERY_HARD',
     'Memorize the digits, then type them back.', '[]', 300,
     '{"startLength":7,"displayTimeMs":1500,"maxLength":15}', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

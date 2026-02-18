-- Subjects: first-class entity for scaling (add subjects + exercises without code change).
-- Scoring config per subject (accuracy, speed, confidence, streak cap).

CREATE TABLE subject (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_subject_id UUID REFERENCES subject(id),
    scoring_config JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE INDEX idx_subject_code ON subject(code);
CREATE INDEX idx_subject_parent ON subject(parent_subject_id);

-- Default subject for existing exercises and "reopen" ultra-easy.
INSERT INTO subject (id, code, name, description, scoring_config)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'default',
    'Default',
    'Default subject for general exercises',
    '{"accuracyType":"BINARY","speedTargetMs":10000,"confidenceWeight":0,"streakBonusCap":0.1}'
);

-- Link exercises to subject: add column, backfill, set NOT NULL, drop axis.
ALTER TABLE exercise ADD COLUMN subject_id UUID REFERENCES subject(id);
UPDATE exercise SET subject_id = 'b0000000-0000-0000-0000-000000000001' WHERE subject_id IS NULL;
ALTER TABLE exercise ALTER COLUMN subject_id SET NOT NULL;
ALTER TABLE exercise DROP COLUMN IF EXISTS axis;

CREATE INDEX idx_exercise_subject_difficulty ON exercise(subject_id, difficulty);
DROP INDEX IF EXISTS idx_exercise_difficulty_axis;

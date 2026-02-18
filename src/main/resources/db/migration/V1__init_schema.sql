-- All timestamps in UTC. User timezone stored separately on profile.

CREATE TABLE user_profile (
    id UUID PRIMARY KEY,
    display_name VARCHAR(255),
    timezone_id VARCHAR(64) NOT NULL DEFAULT 'UTC',
    session_default_seconds INT NOT NULL DEFAULT 180,
    low_battery_mode_seconds INT NOT NULL DEFAULT 45,
    anonymous BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE exercise (
    id UUID PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    difficulty VARCHAR(32) NOT NULL,
    prompt TEXT NOT NULL,
    expected_answers JSONB,
    axis VARCHAR(64) NOT NULL DEFAULT 'default',
    time_limit_seconds INT NOT NULL DEFAULT 60,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE attempt (
    id UUID PRIMARY KEY,
    exercise_id UUID NOT NULL REFERENCES exercise(id),
    profile_id UUID NOT NULL REFERENCES user_profile(id),
    response TEXT,
    correct BOOLEAN NOT NULL,
    reaction_time_ms BIGINT,
    confidence_percent INT,
    score DOUBLE PRECISION NOT NULL,
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE deck (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES user_profile(id),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE card (
    id UUID PRIMARY KEY,
    deck_id UUID NOT NULL REFERENCES deck(id),
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    ease_factor DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval_days INT NOT NULL DEFAULT 0,
    repetitions INT NOT NULL DEFAULT 0,
    next_review_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE daily_plan (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES user_profile(id),
    plan_date DATE NOT NULL,
    axes JSONB NOT NULL DEFAULT '[]',
    queued_exercise_ids JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    UNIQUE (profile_id, plan_date)
);

CREATE INDEX idx_exercise_difficulty_axis ON exercise(difficulty, axis);
CREATE INDEX idx_attempt_profile_attempted ON attempt(profile_id, attempted_at);
CREATE INDEX idx_card_next_review ON card(deck_id, next_review_at);

CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE refresh_token
    ADD COLUMN session_id UUID NOT NULL DEFAULT gen_random_uuid(),
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN last_used_at TIMESTAMP,
    ADD COLUMN revoked_at TIMESTAMP;

UPDATE refresh_token
SET revoked_at = COALESCE(revoked_at, NOW())
WHERE revoked = TRUE;

CREATE INDEX idx_refresh_token_users_active
    ON refresh_token (users_id)
    WHERE revoked = FALSE;

CREATE UNIQUE INDEX idx_refresh_token_session_active
    ON refresh_token (session_id)
    WHERE revoked = FALSE;

CREATE TABLE kicker_match_access_codes (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    usage_count INTEGER NOT NULL DEFAULT 0,
    revoked_at TIMESTAMP,
    revoked_reason VARCHAR(16),
    created_by UUID NOT NULL,

    CONSTRAINT chk_kicker_match_access_code_status
        CHECK (status IN ('ACTIVE', 'REVOKED')),
    CONSTRAINT chk_kicker_match_access_code_revoked_reason
        CHECK (revoked_reason IS NULL OR revoked_reason IN ('MANUAL', 'EXPIRED')),
    CONSTRAINT fk_kicker_match_access_code_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
);

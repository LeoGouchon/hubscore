-- Remove old foreign key constraints
ALTER TABLE kicker_elo
    DROP CONSTRAINT fk_kicker_elo_match,
    ADD CONSTRAINT fk_kicker_elo_match
        FOREIGN KEY (match_id) REFERENCES kicker_matches(id) ON DELETE CASCADE;

ALTER TABLE kicker_elo_seasonal
    DROP CONSTRAINT fk_kicker_elo_seasonal_match,
    ADD CONSTRAINT fk_kicker_elo_seasonal_match
        FOREIGN KEY (match_id) REFERENCES kicker_matches(id) ON DELETE CASCADE;
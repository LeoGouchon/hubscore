ALTER TABLE kicker_matches
    ADD COLUMN created_by UUID,

    ADD CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES users(id)
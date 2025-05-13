ALTER TABLE refresh_token
    DROP CONSTRAINT fksa85a9lsg3ldy1nkmvs9o5ium;

ALTER TABLE refresh_token
    ADD CONSTRAINT fksa85a9lsg3ldy1nkmvs9o5ium
        FOREIGN KEY (users_id)
            REFERENCES users(id)
            ON DELETE CASCADE;
ALTER TABLE refresh_token
    DROP CONSTRAINT fk_refresh_token_users;

ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_users
        FOREIGN KEY (users_id)
            REFERENCES users (id)
            ON DELETE CASCADE;
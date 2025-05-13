ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (users_id) REFERENCES users (id)
            ON DELETE CASCADE;
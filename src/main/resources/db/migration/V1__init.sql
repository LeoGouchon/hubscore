CREATE TABLE players (
                         id SERIAL PRIMARY KEY,
                         firstname VARCHAR(255),
                         lastname VARCHAR(255)
);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       is_admin BOOLEAN,
                       players_id BIGINT UNIQUE,
                       CONSTRAINT fk_users_players FOREIGN KEY(players_id) REFERENCES players(id)
);

CREATE TABLE refresh_token (
                               id SERIAL PRIMARY KEY,
                               token VARCHAR(255) UNIQUE NOT NULL,
                               users_id BIGINT NOT NULL,
                               expiry_date TIMESTAMP NOT NULL,
                               revoked BOOLEAN,
                               CONSTRAINT fk_refresh_token_users FOREIGN KEY(users_id) REFERENCES users(id)
);

CREATE TABLE invitation_token (
                                  id SERIAL PRIMARY KEY,
                                  token VARCHAR(255),
                                  player_id BIGINT,
                                  expiry_date TIMESTAMP,
                                  is_used BOOLEAN,
                                  CONSTRAINT fk_invitation_token_players FOREIGN KEY(player_id) REFERENCES players(id)
);

CREATE TABLE matches (
                         id SERIAL PRIMARY KEY,
                         points_history TEXT,
                         final_score_a INTEGER,
                         final_score_b INTEGER,
                         start_time TIMESTAMP,
                         end_time TIMESTAMP,
                         player_a_id BIGINT,
                         player_b_id BIGINT,
                         CONSTRAINT fk_matches_player_a FOREIGN KEY(player_a_id) REFERENCES players(id),
                         CONSTRAINT fk_matches_player_b FOREIGN KEY(player_b_id) REFERENCES players(id)
);
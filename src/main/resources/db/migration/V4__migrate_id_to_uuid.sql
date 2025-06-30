CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- KICKER_MATCHES
ALTER TABLE kicker_matches ADD COLUMN id_tmp UUID;

UPDATE kicker_matches SET id_tmp = gen_random_uuid();

ALTER TABLE kicker_matches DROP CONSTRAINT kicker_matches_pkey;
ALTER TABLE kicker_matches DROP COLUMN id;
ALTER TABLE kicker_matches RENAME COLUMN id_tmp TO id;
ALTER TABLE kicker_matches ADD PRIMARY KEY (id);

-- USERS
ALTER TABLE users ADD COLUMN id_tmp UUID;

UPDATE users SET id_tmp = gen_random_uuid();

ALTER TABLE refresh_token DROP CONSTRAINT fk_refresh_token_users;

ALTER TABLE refresh_token ADD COLUMN users_id_tmp UUID;

UPDATE refresh_token r SET users_id_tmp = u.id_tmp FROM users u WHERE r.users_id = u.id;

ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN id_tmp TO id;
ALTER TABLE users ADD PRIMARY KEY (id);

ALTER TABLE refresh_token DROP COLUMN users_id;
ALTER TABLE refresh_token RENAME COLUMN users_id_tmp TO users_id;
ALTER TABLE refresh_token ADD CONSTRAINT fk_refresh_token_users FOREIGN KEY (users_id) REFERENCES users (id);

-- REFRESH_TOKEN
ALTER TABLE refresh_token ADD COLUMN id_tmp UUID;

UPDATE refresh_token SET id_tmp = gen_random_uuid();

ALTER TABLE refresh_token DROP CONSTRAINT refresh_token_pkey;
ALTER TABLE refresh_token DROP COLUMN id;
ALTER TABLE refresh_token RENAME COLUMN id_tmp TO id;
ALTER TABLE refresh_token ADD PRIMARY KEY (id);

-- INVITATION-TOKEN
ALTER TABLE invitation_token ADD COLUMN id_tmp UUID;

UPDATE invitation_token SET id_tmp = gen_random_uuid();

ALTER TABLE invitation_token DROP CONSTRAINT invitation_token_pkey;
ALTER TABLE invitation_token DROP COLUMN id;
ALTER TABLE invitation_token RENAME COLUMN id_tmp TO id;
ALTER TABLE invitation_token ADD PRIMARY KEY (id);

-- SQUASH-MATCHES
ALTER TABLE squash_matches ADD COLUMN id_tmp UUID;

UPDATE squash_matches SET id_tmp = gen_random_uuid();

ALTER TABLE squash_matches DROP CONSTRAINT matches_pkey;
ALTER TABLE squash_matches DROP COLUMN id;
ALTER TABLE squash_matches RENAME COLUMN id_tmp TO id;
ALTER TABLE squash_matches ADD PRIMARY KEY (id);

-- PLAYERS
ALTER TABLE players ADD COLUMN id_tmp UUID;

UPDATE players SET id_tmp = gen_random_uuid();

ALTER TABLE users ADD COLUMN player_id_tmp UUID;
ALTER TABLE squash_matches ADD COLUMN player_a_id_tmp UUID;
ALTER TABLE squash_matches ADD COLUMN player_b_id_tmp UUID;
ALTER TABLE kicker_matches ADD COLUMN player_one_team_a_id_tmp UUID;
ALTER TABLE kicker_matches ADD COLUMN player_two_team_a_id_tmp UUID;
ALTER TABLE kicker_matches ADD COLUMN player_one_team_b_id_tmp UUID;
ALTER TABLE kicker_matches ADD COLUMN player_two_team_b_id_tmp UUID;
ALTER TABLE invitation_token ADD COLUMN player_id_tmp UUID;

UPDATE users u SET player_id_tmp = p.id_tmp::uuid FROM players p WHERE u.players_id = p.id;
UPDATE squash_matches s SET player_a_id_tmp = p.id_tmp::uuid FROM players p WHERE s.player_a_id = p.id;
UPDATE squash_matches s SET player_b_id_tmp = p.id_tmp::uuid FROM players p WHERE s.player_b_id = p.id;
UPDATE kicker_matches k SET player_one_team_a_id_tmp = p.id_tmp::uuid FROM players p WHERE k.player_one_team_a_id = p.id;
UPDATE kicker_matches k SET player_two_team_a_id_tmp = p.id_tmp::uuid FROM players p WHERE k.player_two_team_a_id = p.id;
UPDATE kicker_matches k SET player_one_team_b_id_tmp = p.id_tmp::uuid FROM players p WHERE k.player_one_team_b_id = p.id;
UPDATE kicker_matches k SET player_two_team_b_id_tmp = p.id_tmp::uuid FROM players p WHERE k.player_two_team_b_id = p.id;
UPDATE invitation_token i SET player_id_tmp = p.id_tmp::uuid FROM players p WHERE i.player_id = p.id;

ALTER TABLE users DROP CONSTRAINT fk_users_players;
ALTER TABLE squash_matches DROP CONSTRAINT fk_matches_player_a;
ALTER TABLE squash_matches DROP CONSTRAINT fk_matches_player_b;
ALTER TABLE kicker_matches DROP CONSTRAINT fk_kicker_matches_player_one_team_a;
ALTER TABLE kicker_matches DROP CONSTRAINT fk_kicker_matches_player_two_team_a;
ALTER TABLE kicker_matches DROP CONSTRAINT fk_kicker_matches_player_one_team_b;
ALTER TABLE kicker_matches DROP CONSTRAINT fk_kicker_matches_player_two_team_b;
ALTER TABLE invitation_token DROP CONSTRAINT fk_invitation_token_players;

ALTER TABLE players DROP CONSTRAINT players_pkey;
ALTER TABLE players DROP COLUMN id;
ALTER TABLE players RENAME COLUMN id_tmp TO id;
ALTER TABLE players ADD PRIMARY KEY (id);

ALTER TABLE users DROP COLUMN players_id;
ALTER TABLE users RENAME COLUMN player_id_tmp TO players_id;
ALTER TABLE users ADD CONSTRAINT fk_users_player_id FOREIGN KEY (players_id) REFERENCES players (id);

ALTER TABLE squash_matches DROP COLUMN player_a_id;
ALTER TABLE squash_matches RENAME COLUMN player_a_id_tmp TO player_a_id;
ALTER TABLE squash_matches ADD CONSTRAINT fk_squash_matches_player_a_id FOREIGN KEY (player_a_id) REFERENCES players (id);
ALTER TABLE squash_matches DROP COLUMN player_b_id;
ALTER TABLE squash_matches RENAME COLUMN player_b_id_tmp TO player_b_id;
ALTER TABLE squash_matches ADD CONSTRAINT fk_squash_matches_player_b_id FOREIGN KEY (player_b_id) REFERENCES players (id);

ALTER TABLE kicker_matches DROP COLUMN player_one_team_a_id;
ALTER TABLE kicker_matches RENAME COLUMN player_one_team_a_id_tmp TO player_one_team_a_id;
ALTER TABLE kicker_matches ADD CONSTRAINT fk_kicker_matches_player_one_team_a_id FOREIGN KEY (player_one_team_a_id) REFERENCES players (id);
ALTER TABLE kicker_matches DROP COLUMN player_two_team_a_id;
ALTER TABLE kicker_matches RENAME COLUMN player_two_team_a_id_tmp TO player_two_team_a_id;
ALTER TABLE kicker_matches ADD CONSTRAINT fk_kicker_matches_player_two_team_a_id FOREIGN KEY (player_two_team_a_id) REFERENCES players (id);
ALTER TABLE kicker_matches DROP COLUMN player_one_team_b_id;
ALTER TABLE kicker_matches RENAME COLUMN player_one_team_b_id_tmp TO player_one_team_b_id;
ALTER TABLE kicker_matches ADD CONSTRAINT fk_kicker_matches_player_one_team_b_id FOREIGN KEY (player_one_team_b_id) REFERENCES players (id);
ALTER TABLE kicker_matches DROP COLUMN player_two_team_b_id;
ALTER TABLE kicker_matches RENAME COLUMN player_two_team_b_id_tmp TO player_two_team_b_id;
ALTER TABLE kicker_matches ADD CONSTRAINT fk_kicker_matches_player_two_team_b_id FOREIGN KEY (player_two_team_b_id) REFERENCES players (id);

ALTER TABLE invitation_token DROP COLUMN player_id;
ALTER TABLE invitation_token RENAME COLUMN player_id_tmp TO player_id;
ALTER TABLE invitation_token ADD CONSTRAINT fk_invitation_token_player_id FOREIGN KEY (player_id) REFERENCES players (id);

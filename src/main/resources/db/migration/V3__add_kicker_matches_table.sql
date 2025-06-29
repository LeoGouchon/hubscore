CREATE TABLE kicker_matches
(
    id                   BIGSERIAL PRIMARY KEY,
    final_score_team_a   INTEGER,
    final_score_team_b   INTEGER,
    created_at           TIMESTAMP,
    player_one_team_a_id BIGINT,
    player_two_team_a_id BIGINT,
    player_one_team_b_id BIGINT,
    player_two_team_b_id BIGINT,

    CONSTRAINT fk_kicker_matches_player_one_team_a FOREIGN KEY (player_one_team_a_id) REFERENCES players (id),
    CONSTRAINT fk_kicker_matches_player_two_team_a FOREIGN KEY (player_two_team_a_id) REFERENCES players (id),
    CONSTRAINT fk_kicker_matches_player_one_team_b FOREIGN KEY (player_one_team_b_id) REFERENCES players (id),
    CONSTRAINT fk_kicker_matches_player_two_team_b FOREIGN KEY (player_two_team_b_id) REFERENCES players (id)
);
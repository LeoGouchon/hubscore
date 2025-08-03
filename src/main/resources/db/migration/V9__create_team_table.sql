CREATE TABLE teams
(
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    shortname VARCHAR(15) NOT NULL,
    is_kicker BOOLEAN DEFAULT FALSE,
    is_squash BOOLEAN DEFAULT FALSE
);

CREATE TABLE player_team
(
    team_id UUID NOT NULL,
    player_id UUID NOT NULL,
    PRIMARY KEY (team_id, player_id),
    CONSTRAINT fk_team_players_team_id FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_players_player_id FOREIGN KEY (player_id) REFERENCES players (id)
);
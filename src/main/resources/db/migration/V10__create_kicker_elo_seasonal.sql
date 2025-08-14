CREATE TABLE kicker_elo_seasonal
(
    match_id         UUID      NOT NULL,
    player_id        UUID      NOT NULL,
    elo_before_match INTEGER   NOT NULL,
    elo_after_match  INTEGER   NOT NULL,
    elo_change       INTEGER,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    season_year      INTEGER   NOT NULL,
    season_quarter   INTEGER   NOT NULL,

    CONSTRAINT pk_kicker_elo_seasonal PRIMARY KEY (match_id, player_id),
    CONSTRAINT fk_kicker_elo_seasonal_match FOREIGN KEY (match_id) REFERENCES kicker_matches (id),
    CONSTRAINT fk_kicker_elo_seasonal_player FOREIGN KEY (player_id) REFERENCES players (id)
);

CREATE TABLE player_kicker_informations
(
    player_id                   UUID    NOT NULL,
    player_current_elo          INTEGER NOT NULL,
    player_current_seasonal_elo INTEGER,

    CONSTRAINT pk_player_kicker_informations PRIMARY KEY (player_id),
    CONSTRAINT fk_player_kicker_informations_player FOREIGN KEY (player_id) REFERENCES players (id)
);

UPDATE player_kicker_informations pki
SET player_current_elo          = p.kicker_current_elo,
    player_id                   = p.id
FROM players p
WHERE pki.player_id = p.id;

ALTER TABLE players
    DROP COLUMN kicker_current_elo;
CREATE TABLE kicker_elo (
                            match_id UUID NOT NULL,
                            player_id UUID NOT NULL,
                            elo_before_match INTEGER NOT NULL,
                            elo_after_match INTEGER NOT NULL,
                            elo_change INTEGER,
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                            CONSTRAINT pk_kicker_elo PRIMARY KEY (match_id, player_id),
                            CONSTRAINT fk_kicker_elo_match FOREIGN KEY (match_id) REFERENCES kicker_matches(id),
                            CONSTRAINT fk_kicker_elo_player FOREIGN KEY (player_id) REFERENCES players(id)
);


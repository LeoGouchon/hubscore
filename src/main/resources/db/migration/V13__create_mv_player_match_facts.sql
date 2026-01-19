CREATE MATERIALIZED VIEW mv_player_match_facts AS

WITH match_players AS (

    -- Team A - player 1
    SELECT
        m.id AS match_id,
        m.created_at AS match_date,
        m.final_score_team_a AS player_score,
        m.final_score_team_b AS opponent_score,

        m.player_one_team_a_id AS player_id,
        m.player_two_team_a_id AS teammate_id,

        m.player_one_team_b_id AS opponent_one_id,
        m.player_two_team_b_id AS opponent_two_id

    FROM kicker_matches m

    UNION ALL

    -- Team A - player 2
    SELECT
        m.id,
        m.created_at,
        m.final_score_team_a AS player_score,
        m.final_score_team_b AS opponent_score,
        m.player_two_team_a_id,
        m.player_one_team_a_id,
        m.player_one_team_b_id,
        m.player_two_team_b_id

    FROM kicker_matches m

    UNION ALL

    -- Team B - player 1
    SELECT
        m.id,
        m.created_at,
        m.final_score_team_b AS player_score,
        m.final_score_team_a AS opponent_score,
        m.player_one_team_b_id,
        m.player_two_team_b_id,
        m.player_one_team_a_id,
        m.player_two_team_a_id
    FROM kicker_matches m

    UNION ALL

    -- Team B - player 2
    SELECT
        m.id,
        m.created_at,
        m.final_score_team_b AS player_score,
        m.final_score_team_a AS opponent_score,
        m.player_two_team_b_id,
        m.player_one_team_b_id,
        m.player_one_team_a_id,
        m.player_two_team_a_id
    FROM kicker_matches m
)

SELECT
    mp.match_id,
    mp.match_date,
    mp.player_score,
    mp.opponent_score,
    mp.player_id,
    mp.teammate_id,
    mp.opponent_one_id,
    mp.opponent_two_id,

    ARRAY[mp.opponent_one_id, mp.opponent_two_id] AS opponents,

    e.elo_change AS elo_delta,
    es.elo_change AS seasonal_elo_delta,

    e.elo_before_match,
    es.elo_before_match AS elo_seasonal_before_match,

    e.elo_after_match,
    es.elo_after_match AS elo_seasonal_after_match

FROM match_players mp
         JOIN kicker_elo e
              ON e.match_id = mp.match_id
                  AND e.player_id = mp.player_id

         JOIN kicker_elo_seasonal es
              ON es.match_id = mp.match_id
                  AND es.player_id = mp.player_id;

-- INDEX --
-- REFRESH CONCURRENTLY
CREATE UNIQUE INDEX idx_mv_pmf_pk
    ON mv_player_match_facts (match_id, player_id);

-- MAIN INDEX
CREATE INDEX idx_mv_pmf_player
    ON mv_player_match_facts (player_id);

CREATE INDEX idx_mv_pmf_player_teammate
    ON mv_player_match_facts (player_id, teammate_id);

-- INDEX GIN MATCHUP
CREATE INDEX idx_mv_pmf_opponents
    ON mv_player_match_facts
        USING GIN (opponents);

-- FOR SOME RECURRENT CALCULATION
CREATE INDEX idx_mv_pmf_player_date_desc
    ON mv_player_match_facts (player_id, match_date DESC);

CREATE INDEX idx_mv_pmf_match_date
    ON mv_player_match_facts (match_date);
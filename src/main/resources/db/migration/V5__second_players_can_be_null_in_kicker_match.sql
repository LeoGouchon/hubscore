-- Allowed NULL on player_two_team_a_id
ALTER TABLE kicker_matches ALTER COLUMN player_two_team_a_id DROP NOT NULL;

-- Allowed NULL on player_two_team_b_id
ALTER TABLE kicker_matches ALTER COLUMN player_two_team_b_id DROP NOT NULL;

ALTER TABLE kicker_matches ALTER COLUMN final_score_team_a SET NOT NULL;
ALTER TABLE kicker_matches ALTER COLUMN final_score_team_b SET NOT NULL;
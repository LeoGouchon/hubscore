package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KickerMatchRepository extends JpaRepository<KickerMatches, UUID>, JpaSpecificationExecutor<KickerMatches> {

    @Query(
            value = """
            WITH all_players AS (
                SELECT player_one_team_a_id AS player_id, final_score_team_a AS score FROM kicker_matches
                UNION ALL
                SELECT player_two_team_a_id AS player_id, final_score_team_a AS score FROM kicker_matches
                UNION ALL
                SELECT player_one_team_b_id AS player_id, final_score_team_b AS score FROM kicker_matches
                UNION ALL
                SELECT player_two_team_b_id AS player_id, final_score_team_b AS score FROM kicker_matches
            )
            SELECT
                player_id AS playerId,
                p.firstname AS firstname,
                p.lastname AS lastname,
                COUNT(*) AS totalMatches,
                SUM(CASE WHEN score = 10 THEN 1 ELSE 0 END) AS wins,
                SUM(CASE WHEN score != 10 THEN 1 ELSE 0 END) AS losses,
                ROUND(SUM(CASE WHEN score = 10 THEN 1 ELSE 0 END)::numeric / COUNT(*), 2) AS winRate
            FROM all_players
            JOIN players p ON p.id = all_players.player_id
            GROUP BY player_id, p.firstname, p.lastname
            ORDER BY p.firstname DESC
        """,
            nativeQuery = true
    )
    List<GlobalStatsResponseDTO> getGlobalKickerStats();
}

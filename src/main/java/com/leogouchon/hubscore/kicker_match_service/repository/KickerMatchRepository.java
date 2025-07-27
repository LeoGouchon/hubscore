package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KickerMatchRepository extends JpaRepository<KickerMatches, UUID>, JpaSpecificationExecutor<KickerMatches> {

    @Query(
            value = """
                        WITH all_players AS (
                            SELECT player_one_team_a_id AS player_id, final_score_team_a AS score, date_trunc('day', created_at) FROM kicker_matches
                            UNION ALL
                            SELECT player_two_team_a_id AS player_id, final_score_team_a AS score, date_trunc('day', created_at) FROM kicker_matches
                            UNION ALL
                            SELECT player_one_team_b_id AS player_id, final_score_team_b AS score, date_trunc('day', created_at) FROM kicker_matches
                            UNION ALL
                            SELECT player_two_team_b_id AS player_id, final_score_team_b AS score, date_trunc('day', created_at) FROM kicker_matches
                        )
                        SELECT
                            player_id AS playerId,
                            p.firstname AS firstname,
                            p.lastname AS lastname,
                            p.kicker_current_elo AS currentElo,
                            COUNT(*) AS totalMatches,
                            SUM(CASE WHEN score = 10 THEN 1 ELSE 0 END) AS wins,
                            SUM(CASE WHEN score != 10 THEN 1 ELSE 0 END) AS losses,
                            ROUND(SUM(CASE WHEN score = 10 THEN 1 ELSE 0 END)::numeric / COUNT(*), 2) AS winRate
                        FROM all_players
                        JOIN players p ON p.id = all_players.player_id
                        GROUP BY player_id, p.firstname, p.lastname, p.kicker_current_elo
                        ORDER BY p.firstname DESC
                    """,
            nativeQuery = true
    )
    List<GlobalStatsResponseDTO> getGlobalKickerStats();

    @Query(value = """
                WITH all_players AS (
                    SELECT player_one_team_a_id AS player_id, final_score_team_a AS score, created_at FROM kicker_matches
                    UNION ALL
                    SELECT player_two_team_a_id AS player_id, final_score_team_a AS score, created_at FROM kicker_matches
                    UNION ALL
                    SELECT player_one_team_b_id AS player_id, final_score_team_b AS score, created_at FROM kicker_matches
                    UNION ALL
                    SELECT player_two_team_b_id AS player_id, final_score_team_b AS score, created_at FROM kicker_matches
                ),
                last_five_games AS (
                    SELECT
                        player_id,
                        CASE WHEN score = 10 THEN true ELSE false END AS win,
                        ROW_NUMBER() OVER (PARTITION BY player_id ORDER BY created_at DESC) AS rn
                    FROM all_players
                )
                SELECT win FROM last_five_games
                WHERE player_id = :playerId AND rn <= 5
                ORDER BY rn
            """, nativeQuery = true)
    List<Boolean> getLastFiveResultsByPlayerId(@Param("playerId") UUID playerId);

    @Query(value = """
                SELECT * FROM kicker_matches
                ORDER BY created_at;
                """, nativeQuery = true)
    List<KickerMatches> getAllByOrderByCreatedAtAsc();
}

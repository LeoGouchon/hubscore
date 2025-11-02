package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.GlobalStatsResponseProjection;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.LastKickerEloByDateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KickerMatchRepository extends JpaRepository<KickerMatches, UUID>, JpaSpecificationExecutor<KickerMatches> {

    @Query(
            value = """
                    WITH filtered_matches AS (
                            SELECT * FROM kicker_matches
                            WHERE (:year IS NULL OR EXTRACT(YEAR FROM created_at) = :year)
                              AND (:quarter IS NULL OR EXTRACT(QUARTER FROM created_at) = :quarter)
                        ),
                        all_players AS (
                            SELECT player_one_team_a_id AS player_id, final_score_team_a AS score FROM filtered_matches
                            UNION ALL
                            SELECT player_two_team_a_id AS player_id, final_score_team_a AS score FROM filtered_matches
                            UNION ALL
                            SELECT player_one_team_b_id AS player_id, final_score_team_b AS score FROM filtered_matches
                            UNION ALL
                            SELECT player_two_team_b_id AS player_id, final_score_team_b AS score FROM filtered_matches
                        ),
                        player_stats AS (
                            SELECT
                                ap.player_id,
                                COUNT(*) AS total_matches,
                                SUM(CASE WHEN score = 10 THEN 1 ELSE 0 END) AS wins,
                                SUM(CASE WHEN score != 10 THEN 1 ELSE 0 END) AS losses,
                                ROUND(SUM(CASE WHEN score = 10 THEN 1 ELSE 0 END)::numeric / COUNT(*), 2) AS win_rate
                            FROM all_players ap
                            GROUP BY ap.player_id
                        ),
                        ranked_players AS (
                            SELECT
                                ps.player_id,
                                RANK() OVER (ORDER BY p.player_current_elo DESC) AS rank
                            FROM player_stats ps
                            JOIN player_kicker_informations p ON p.player_id = ps.player_id
                            WHERE ps.total_matches >= 10
                        )
                        SELECT
                            ps.player_id AS playerId,
                            p.firstname,
                            p.lastname,
                            pki.player_current_elo AS currentElo,
                            ps.total_matches,
                            ps.wins,
                            ps.losses,
                            ps.win_rate,
                            COALESCE(rp.rank, 0) AS rank
                        FROM player_stats ps
                        JOIN players p ON p.id = ps.player_id
                        JOIN player_kicker_informations pki ON pki.player_id = p.id
                        LEFT JOIN ranked_players rp ON rp.player_id = ps.player_id
                        ORDER BY rank;
                    """,
            nativeQuery = true
    )
    List<GlobalStatsResponseProjection> getGlobalKickerStats(
            @Param("year") Integer year,
            @Param("quarter") Integer quarter
    );

    @Query(
            value = """
                    WITH match_counts AS (
                        SELECT player_id, COUNT(*) AS total_matches
                        FROM kicker_elo ke
                                 JOIN kicker_matches m ON m.id = ke.match_id
                        WHERE m.created_at <= :date
                        GROUP BY player_id
                    ),
                         latest_elo AS (
                             SELECT
                                 ke.player_id,
                                 ke.match_id,
                                 ke.elo_after_match,
                                 m.created_at,
                                 ROW_NUMBER() OVER (PARTITION BY ke.player_id ORDER BY m.created_at DESC) AS rn
                             FROM kicker_elo ke
                                      JOIN kicker_matches m ON m.id = ke.match_id
                             WHERE m.created_at <= :date
                         ),
                         ranked_players AS (
                             SELECT
                                 le.player_id,
                                 le.match_id,
                                 le.elo_after_match,
                                 RANK() OVER (ORDER BY le.elo_after_match DESC) AS rank
                             FROM latest_elo le
                                      JOIN match_counts mc ON le.player_id = mc.player_id
                             WHERE le.rn = 1 AND mc.total_matches >= 10
                         )
                        SELECT
                        le.player_id AS playerId,
                        le.elo_after_match AS elo,
                        COALESCE(rp.rank, 0) AS rank
                    FROM latest_elo le
                        LEFT JOIN ranked_players rp ON le.player_id = rp.player_id
                    WHERE le.rn = 1
                    ORDER BY rank;
                    """,
            nativeQuery = true
    )
    List<LastKickerEloByDateProjection> getLatestKickerEloByDate(Timestamp date);

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

    @Query(value = """
            SELECT COUNT(*) FROM kicker_matches;
            """, nativeQuery = true)
    Integer getTotalMatches();

    @Query(value = """
        SELECT COUNT(DISTINCT player) AS total_players
        FROM (
            SELECT player_one_team_a_id AS player FROM kicker_matches
            UNION ALL
            SELECT player_two_team_a_id FROM kicker_matches
            UNION ALL
            SELECT player_one_team_b_id FROM kicker_matches
            UNION ALL
            SELECT player_two_team_b_id FROM kicker_matches
        ) AS all_players;
    """, nativeQuery = true)
    Integer getTotalPlayers();

    List<KickerMatches> findAllByCreatedAtAfterOrderByCreatedAtAsc(Timestamp date);

    List<KickerMatches> findAllByOrderByCreatedAtAsc();
}

package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.dto.OpponentStatsDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.OverallStatsDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.PartnerStatsDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.GlobalStatsResponseProjection;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.LastKickerEloByDateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
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
                    WITH latest_per_player AS (
                        SELECT
                            pmf.player_id,
                            pmf.elo_after_match,
                            pmf.match_date,
                            ROW_NUMBER() OVER (
                                PARTITION BY pmf.player_id
                                ORDER BY pmf.match_date DESC
                            ) AS rn
                        FROM mv_player_match_facts pmf
                        WHERE pmf.match_date <= :date
                    ),
                    match_counts AS (
                        SELECT
                            player_id,
                            COUNT(*) AS total_matches
                        FROM mv_player_match_facts
                        WHERE match_date <= :date
                        GROUP BY player_id
                    ),
                    eligible_players AS (
                        SELECT
                            lpp.player_id,
                            lpp.elo_after_match
                        FROM latest_per_player lpp
                        JOIN match_counts mc
                          ON mc.player_id = lpp.player_id
                        WHERE lpp.rn = 1
                          AND mc.total_matches >= 10
                    ),
                    ranked_players AS (
                        SELECT
                            player_id,
                            elo_after_match,
                            RANK() OVER (ORDER BY elo_after_match DESC) AS rank
                        FROM eligible_players
                    )
                    SELECT
                        rp.player_id   AS playerId,
                        rp.elo_after_match AS elo,
                        rp.rank
                    FROM ranked_players rp
                    ORDER BY rp.rank;
                    """,
            nativeQuery = true
    )
    List<LastKickerEloByDateProjection> getLatestKickerEloByDate(Timestamp date);

    @Query(value = """
                SELECT (pmf.player_score = 10) as win
                FROM mv_player_match_facts pmf
                WHERE pmf.player_id = :playerId
                ORDER BY pmf.match_date DESC
                LIMIT 5
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

    @Query(value = """
            SELECT
                pmf.teammate_id AS id,
                p.firstname,
                p.lastname,
            
                COUNT(*) AS total_matches,
            
                SUM(CASE WHEN pmf.player_score = 10 THEN 1 ELSE 0 END) AS wins,
                SUM(CASE WHEN pmf.player_score < 10 THEN 1 ELSE 0 END) AS losses
            
            FROM mv_player_match_facts pmf
            JOIN players p ON p.id = pmf.teammate_id
            
            WHERE pmf.player_id = :playerId
            
            GROUP BY
                pmf.teammate_id,
                p.firstname,
                p.lastname;
            """, nativeQuery = true)
    List<PartnerStatsDTO> getPartnerStats(@Param("playerId") UUID playerId);


    @Query(value = """
                SELECT
                    opponent_id AS id,
                    p.firstname,
                    p.lastname,
            
                    SUM(CASE WHEN pmf.player_score = 10 THEN 1 ELSE 0 END) AS wins,
                    SUM(CASE WHEN pmf.player_score <> 10 THEN 1 ELSE 0 END)  AS losses
            
                FROM mv_player_match_facts pmf
                CROSS JOIN LATERAL unnest(pmf.opponents) AS opponent_id
                JOIN players p ON p.id = opponent_id
            
                WHERE pmf.player_id = :playerId
            
                GROUP BY
                    opponent_id,
                    p.firstname,
                    p.lastname;
            """, nativeQuery = true)
    List<OpponentStatsDTO> getOpponentStats(@Param("playerId") UUID playerId);

    @Query(value = """
            SELECT
                SUM(CASE WHEN pmf.player_score = 10 THEN 1 ELSE 0 END) AS wins,
                SUM(CASE WHEN pmf.player_score <> 10 THEN 1 ELSE 0 END) AS losses
            FROM mv_player_match_facts pmf
            WHERE pmf.player_id = :playerId;
            """, nativeQuery = true)
    OverallStatsDTO getAllTimeStats(@Param("playerId") UUID playerId);
}

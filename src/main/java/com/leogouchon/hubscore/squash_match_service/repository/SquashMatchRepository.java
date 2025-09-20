package com.leogouchon.hubscore.squash_match_service.repository;

import com.leogouchon.hubscore.squash_match_service.entity.SquashMatches;
import com.leogouchon.hubscore.squash_match_service.repository.projection.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SquashMatchRepository extends JpaRepository<SquashMatches, UUID>, JpaSpecificationExecutor<SquashMatches> {

    @Query(
            value = """
                             SELECT 
                                CAST(EXTRACT(EPOCH FROM DATE_TRUNC('day', m.end_time)) AS BIGINT) AS day_unix,
                                COUNT(*) AS total_matches,
                                p.id AS player_id,
                                p.firstname AS player_name,
                    
                                SUM(CASE 
                                    WHEN (m.player_a_id = p.id AND m.final_score_a > m.final_score_b) OR 
                                         (m.player_b_id = p.id AND m.final_score_b > m.final_score_a) 
                                    THEN 1 ELSE 0 END) AS wins,
                    
                                SUM(CASE
                                    WHEN (m.player_a_id = p.id AND m.final_score_a < m.final_score_b) OR
                                         (m.player_b_id = p.id AND m.final_score_b < m.final_score_a)
                                    THEN 1 ELSE 0 END) AS losses,
                    
                                SUM(CASE
                                    WHEN m.player_b_id = p.id THEN m.final_score_b
                                    WHEN m.player_a_id = p.id THEN m.final_score_a
                                    ELSE 0 END) AS points_scored,
                    
                                SUM(CASE
                                    WHEN m.player_a_id = p.id THEN m.final_score_b
                                    WHEN m.player_b_id = p.id THEN m.final_score_a
                                    ELSE 0 END) AS points_conceded
                    
                             FROM squash_matches AS m
                             JOIN players p ON p.id = m.player_a_id OR p.id = m.player_b_id
                             GROUP BY day_unix, p.id, p.firstname
                             ORDER BY day_unix desc, p.id
                    """, nativeQuery = true
    )
    List<SessionsDataProjection> getSessionsData(Pageable pageable);

    @Query(
            value = """
                                SELECT 
                                    COUNT(*) AS total_matches,
                    
                                    AVG(CASE
                                        WHEN m.final_score_a > m.final_score_b THEN m.final_score_b
                                        WHEN m.final_score_b > m.final_score_a THEN m.final_score_a
                                        ELSE 0 END
                                    ) as average_loser_score,
                    
                                    SUM(CASE
                                        WHEN 
                                            ABS(m.final_score_a - m.final_score_b) = 2 
                                                AND (m.final_score_a > 10 OR m.final_score_b > 10) 
                                            THEN 1 
                                            ELSE 0 
                                        END) as close_matches_count,
                    
                                    SUM(CASE
                                        WHEN 
                                            ABS(m.final_score_a - m.final_score_b) > 7 THEN 1 
                                            ELSE 0 
                                        END) as stomp_matches_count
                                FROM squash_matches as m;
                    """, nativeQuery = true)
    List<Object[]> getOverallStats();

    @Query(
            value = """
                    SELECT m.id,
                           m.final_score_a,
                           m.final_score_b,
                           p1.id as player_a_id, p1.firstname as player_a_firstname, p1.lastname as player_a_lastname,
                           p2.id as player_b_id, p2.firstname as player_b_firstname, p2.lastname as player_b_lastname,
                           m.start_time
                    FROM squash_matches m
                    JOIN players p1 ON p1.id = m.player_a_id
                    JOIN players p2 ON p2.id = m.player_b_id
                    WHERE (:playerId IS NULL OR m.player_a_id = :playerId OR m.player_b_id = :playerId)
                    ORDER BY ABS(m.final_score_a - m.final_score_b) DESC
                    LIMIT 5
                    """,
            nativeQuery = true
    )
    List<LightDataMatchProjection> getWorstScoreOverall(@Param("playerId") UUID playerId);

    @Query(
            value = """
                    SELECT m.id,
                           m.final_score_a,
                           m.final_score_b,
                           p1.id as player_a_id, 
                           p1.firstname as player_a_firstname, 
                           p1.lastname as player_a_lastname,
                           p2.id as player_b_id, 
                           p2.firstname as player_b_firstname, 
                           p2.lastname as player_b_lastname,
                           m.start_time
                    FROM squash_matches m
                             JOIN players p1 ON p1.id = m.player_a_id
                             JOIN players p2 ON p2.id = m.player_b_id
                    WHERE ABS(m.final_score_a - m.final_score_b) = 2
                      AND (:playerId IS NULL OR m.player_a_id = :playerId OR m.player_b_id = :playerId)
                    GROUP BY m.id, m.final_score_a, m.final_score_b,
                             p1.id, p1.firstname, p1.lastname,
                             p2.id, p2.firstname, p2.lastname,
                             m.start_time
                    ORDER BY (m.final_score_a + m.final_score_b) DESC
                    LIMIT 5;
                    """,
            nativeQuery = true
    )
    List<LightDataMatchProjection> getClosestScoreOverall(@Param("playerId") UUID playerId);

    @Query(
            value = """
                    SELECT
                        p.id AS player_id,
                        p.firstname,
                        p.lastname,
                    
                        COUNT(*) AS total_matches,
                    
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a > m.final_score_b)
                                    OR (m.player_b_id = :playerId AND m.final_score_b > m.final_score_a)
                                    THEN 1 ELSE 0 END
                        ) AS wins,
                    
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a < m.final_score_b)
                                    OR (m.player_b_id = :playerId AND m.final_score_b < m.final_score_a)
                                    THEN 1 ELSE 0 END
                        ) AS losses,
                    
                        AVG(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a > m.final_score_b)
                                    THEN m.final_score_b
                                WHEN (m.player_b_id = :playerId AND m.final_score_b > m.final_score_a)
                                    THEN m.final_score_a
                                END
                        ) AS average_opponent_lost_score,
                    
                        AVG(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a < m.final_score_b)
                                    THEN m.final_score_a
                                WHEN (m.player_b_id = :playerId AND m.final_score_b < m.final_score_a)
                                    THEN m.final_score_b
                                END
                        ) AS average_player_lost_score,
                    
                        -- Close matches won (difference = 2, both scores >= 10, and won)
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a >= 10 AND m.final_score_b >= 10 AND m.final_score_a - m.final_score_b = 2)
                                    OR (m.player_b_id = :playerId AND m.final_score_a >= 10 AND m.final_score_b >= 10 AND m.final_score_b - m.final_score_a = 2)
                                    THEN 1 ELSE 0 END
                        ) AS close_matches_won_count,
                    
                        -- Close matches lost (same as above but lost)
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a >= 10 AND m.final_score_b >= 10 AND m.final_score_b - m.final_score_a = 2)
                                    OR (m.player_b_id = :playerId AND m.final_score_a >= 10 AND m.final_score_b >= 10 AND m.final_score_a - m.final_score_b = 2)
                                    THEN 1 ELSE 0 END
                        ) AS close_matches_lost_count,
                    
                        -- Stomping others = won by more than 7 points
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a - m.final_score_b > 7)
                                    OR (m.player_b_id = :playerId AND m.final_score_b - m.final_score_a > 7)
                                    THEN 1 ELSE 0 END
                        ) AS stomp_matches_won_count,
                    
                        -- Stomped = lost by more than 7 points
                        SUM(CASE
                               WHEN (m.player_a_id = :playerId AND m.final_score_b - m.final_score_a > 7)
                                    OR (m.player_b_id = :playerId AND m.final_score_a - m.final_score_b > 7)
                                    THEN 1 ELSE 0 END
                        ) AS stomp_matches_lost_count
                    
                    FROM squash_matches m
                             JOIN players p ON p.id = m.player_a_id OR p.id = m.player_b_id
                    WHERE p.id = :playerId
                    GROUP BY p.id, p.firstname, p.lastname;
                    """, nativeQuery = true)
    List<PlayerStatsProjection> getStatsByPlayerId(@Param("playerId") UUID playerId);

    @Query(
            value = """
                    
                    SELECT
                        CASE
                            WHEN m.player_a_id = :playerId THEN m.player_b_id
                            ELSE m.player_a_id
                            END AS opponent_id,
                    
                        p.firstname as opponent_firstname,
                        p.lastname as opponent_lastname,
                    
                        COUNT(*) AS total_matches,
                    
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a > m.final_score_b) OR
                                     (m.player_b_id = :playerId AND m.final_score_b > m.final_score_a)
                                    THEN 1 ELSE 0 END) AS wins,
                    
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a < m.final_score_b) OR
                                     (m.player_b_id = :playerId AND m.final_score_b < m.final_score_a)
                                    THEN 1 ELSE 0 END) AS losses,
                    
                        ROUND(AVG(CASE
                                      WHEN (m.player_a_id = :playerId AND m.final_score_a < m.final_score_b) THEN m.final_score_a
                                      WHEN (m.player_b_id = :playerId AND m.final_score_b < m.final_score_a) THEN m.final_score_b
                                      END), 2) AS average_score_when_lost,
                    
                        -- Close matches won (difference of 2, and scores >= 10)
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a > m.final_score_b AND m.final_score_a >= 10 AND m.final_score_b >= 10 AND ABS(m.final_score_a - m.final_score_b) = 2)
                                    OR (m.player_b_id = :playerId AND m.final_score_b > m.final_score_a AND m.final_score_a >= 10 AND m.final_score_b >= 10 AND ABS(m.final_score_b - m.final_score_a) = 2)
                                    THEN 1 ELSE 0 END) AS close_won_count,
                    
                        -- Close matches lost (difference of 2, and scores >= 10)
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a < m.final_score_b AND m.final_score_a >= 10 AND m.final_score_b >= 10 AND ABS(m.final_score_a - m.final_score_b) = 2)
                                    OR (m.player_b_id = :playerId AND m.final_score_b < m.final_score_a AND m.final_score_a >= 10 AND m.final_score_b >= 10 AND ABS(m.final_score_b - m.final_score_a) = 2)
                                    THEN 1 ELSE 0 END) AS close_lost_count,
                    
                        -- Stomps suffered (lost with gap > 7)
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_b - m.final_score_a > 7)
                                    OR (m.player_b_id = :playerId AND m.final_score_a - m.final_score_b > 7)
                                    THEN 1 ELSE 0 END) AS stomps_lost_count,
                    
                        -- Stomps inflicted (won with gap > 7)
                        SUM(CASE
                                WHEN (m.player_a_id = :playerId AND m.final_score_a - m.final_score_b > 7)
                                    OR (m.player_b_id = :playerId AND m.final_score_b - m.final_score_a > 7)
                                    THEN 1 ELSE 0 END) AS stomps_won_count
                    
                    FROM squash_matches m
                             JOIN players p ON p.id = CASE
                                                          WHEN m.player_a_id = :playerId THEN m.player_b_id
                                                          ELSE m.player_a_id
                        END
                    WHERE m.player_a_id = :playerId OR m.player_b_id = :playerId
                    GROUP BY opponent_id, opponent_firstname, opponent_lastname
                    ORDER BY total_matches DESC;
                    """,
            nativeQuery = true
    )
    List<OpponentStatsProjection> getDetailedStatsAgainstEachOpponent(@Param("playerId") UUID playerId);

    @Query(
            value = """
                    WITH sorted_score AS (
                        SELECT
                            GREATEST(sm.final_score_a, sm.final_score_b) AS win_score,
                            LEAST(sm.final_score_a, sm.final_score_b)    AS lose_score
                        FROM squash_matches as sm
                    )
                    SELECT COUNT(*), ss.win_score, ss.lose_score
                    FROM sorted_score as ss
                    GROUP BY ss.win_score, ss.lose_score
                    ORDER BY ss.lose_score;
                    """,
            nativeQuery = true
    )
    List<ScoreDistributionProjection> getScoreDistribution();
}

package com.leogouchon.hubscore.squash_match_service.repository;

import com.leogouchon.hubscore.squash_match_service.entity.SquashMatches;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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
    List<Object[]> getSessionsData(Pageable pageable);

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
                        ABS(m.final_score_a - m.final_score_b) > 7
                            AND (m.final_score_a > 11 OR m.final_score_b > 11) 
                        THEN 1 
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
                           m.player_a_id,
                           m.player_b_id,
                           m.start_time
                    FROM squash_matches as m
                    WHERE ABS(m.final_score_a - m.final_score_b) =
                          (SELECT MAX(ABS(m2.final_score_a - m2.final_score_b)) FROM squash_matches as m2);
                    """, nativeQuery = true)
    List<Object[]> getWorstScoreOverall();

    @Query(
            value = """
                    SELECT m.id,
                           m.final_score_a,
                           m.final_score_b,
                           m.player_a_id,
                           m.player_b_id,
                           m.start_time
                    FROM squash_matches as m
                    WHERE ABS(m.final_score_a - m.final_score_b) =
                          (SELECT MIN(ABS(m2.final_score_a - m2.final_score_b)) FROM squash_matches as m2)
                      AND
                        (CASE WHEN m.final_score_a > m.final_score_b THEN m.final_score_a ELSE m.final_score_b END) =
                        (SELECT MAX(CASE WHEN m2.final_score_a > m2.final_score_b THEN m2.final_score_a ELSE m2.final_score_b END) FROM squash_matches as m2);
                    """, nativeQuery = true)
    List<Object[]> getClosestScoreOverall();
}

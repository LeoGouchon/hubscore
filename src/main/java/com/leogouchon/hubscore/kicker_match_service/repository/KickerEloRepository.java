package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.dto.EloHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerElo;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloId;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.EloVisibilityProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KickerEloRepository extends JpaRepository<KickerElo, KickerEloId> {
    @Query("""
        SELECT ke.eloAfterMatch
        FROM KickerElo ke
        JOIN KickerMatches m ON ke.match.id = m.id
        WHERE ke.player.id = :playerId
        ORDER BY m.createdAt DESC
        LIMIT 1
    """)
    Optional<Integer> findLastElo(
            @Param("playerId") UUID playerId
    );

    @Query("""
            SELECT ke
            FROM KickerElo ke
            WHERE ke.id.matchId IN :matchIds
            """)
    List<KickerElo> findAllByMatchIdIn(@Param("matchIds") List<UUID> matchIds);

    @Query(value = """
            SELECT
                target.match_id AS matchId,
                target.player_id AS playerId
            FROM kicker_elo target
            JOIN kicker_matches target_match ON target_match.id = target.match_id
            WHERE target.match_id IN (:matchIds)
              AND (
                  SELECT COUNT(*)
                  FROM kicker_elo previous
                  JOIN kicker_matches previous_match ON previous_match.id = previous.match_id
                  WHERE previous.player_id = target.player_id
                    AND previous_match.created_at < target_match.created_at
              ) >= :minRankedMatches
            """, nativeQuery = true)
    List<EloVisibilityProjection> findVisibleEloBeforeMatchPairs(
            @Param("matchIds") List<UUID> matchIds,
            @Param("minRankedMatches") int minRankedMatches
    );

    void deleteByMatchCreatedAtAfter(Timestamp date);

    @Query(value = """
            WITH history_player_elo AS (
                SELECT DISTINCT ON (ke.player_id, DATE(m.created_at))
                    DATE(m.created_at) AS match_day,
                    m.created_at AS date,
                    ke.player_id,
                    ke.elo_after_match AS elo
                FROM kicker_elo ke
                JOIN kicker_matches m ON ke.match_id = m.id
                ORDER BY ke.player_id, DATE(m.created_at), m.created_at DESC, ke.created_at DESC
            ),
            match_days AS (
                SELECT DISTINCT match_day
                FROM history_player_elo
            ),
            players AS (
                SELECT DISTINCT player_id
                FROM kicker_elo
            ),
            daily_player_elo AS (
                SELECT DISTINCT ON (p.player_id, md.match_day)
                    md.match_day,
                    p.player_id,
                    ke.elo_after_match AS elo
                FROM match_days md
                CROSS JOIN players p
                JOIN kicker_elo ke ON ke.player_id = p.player_id
                JOIN kicker_matches m ON ke.match_id = m.id
                WHERE DATE(m.created_at) <= md.match_day
                ORDER BY p.player_id, md.match_day, m.created_at DESC, ke.created_at DESC
            ),
            daily_elo_bounds AS (
                SELECT
                    match_day,
                    MAX(elo) AS max,
                    MIN(elo) AS min,
                    PERCENTILE_DISC(0.25) WITHIN GROUP (ORDER BY elo) AS first_quartile,
                    PERCENTILE_DISC(0.75) WITHIN GROUP (ORDER BY elo) AS third_quartile
                FROM daily_player_elo
                GROUP BY match_day
            )
            SELECT
                CAST(player_elo.match_day AS timestamp) AS date,
                player_elo.elo AS elo,
                elo_bounds.max AS max,
                elo_bounds.min AS min,
                elo_bounds.first_quartile AS "firstQuartile",
                elo_bounds.third_quartile AS "thirdQuartile"
            FROM history_player_elo player_elo
            JOIN daily_elo_bounds elo_bounds ON elo_bounds.match_day = player_elo.match_day
            WHERE player_elo.player_id = :playerId
            ORDER BY player_elo.match_day
            """, nativeQuery = true)
    List<EloHistoryDTO> getEloHistory(
            @Param("playerId") UUID playerId
    );
}

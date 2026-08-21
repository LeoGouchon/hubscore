package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.dto.EloHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.SeasonalStatsDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloId;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloSeasonal;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.GlobalStatsResponseProjection;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.EloVisibilityProjection;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.LastKickerEloByDateProjection;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.LoserScorePerDeltaEloProjection;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.SeasonStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KickerEloSeasonalRepository extends JpaRepository<KickerEloSeasonal, KickerEloId> {

    @Query("""
                SELECT ke.eloAfterMatch
                FROM KickerEloSeasonal ke
                JOIN KickerMatches m ON ke.match.id = m.id
                WHERE ke.player.id = :playerId
                  AND ke.seasonYear = :year
                  AND ke.seasonQuarter = :quarter
                ORDER BY m.createdAt DESC
                LIMIT 1
            """)
    Optional<Integer> findLastEloForSeason(
            @Param("playerId") UUID playerId,
            @Param("year") int year,
            @Param("quarter") int quarter
    );

    @Query("""
            SELECT COUNT(DISTINCT (seasonYear, seasonQuarter))
            FROM KickerEloSeasonal
            """)
    Integer getNbSeasons();

    @Query("""
            SELECT ke.seasonYear as year, ke.seasonQuarter as quarter, COUNT(DISTINCT ke.match.id) as nbMatches, COUNT(DISTINCT ke.player.id) as nbPlayers
            FROM KickerEloSeasonal ke
            GROUP BY ke.seasonQuarter, ke.seasonYear
            """)
    List<SeasonStatsProjection> getSeasonsStats();

    @Query(
            value = """
                    WITH filtered_pmf AS (
                        SELECT *
                        FROM mv_player_match_facts pmf
                        WHERE pmf.match_date <= :date
                          AND EXTRACT(YEAR FROM pmf.match_date) = :year
                          AND EXTRACT(QUARTER FROM pmf.match_date) = :quarter
                    ),
                    match_counts AS (
                        SELECT
                            player_id,
                            COUNT(*) AS total_matches
                        FROM filtered_pmf
                        GROUP BY player_id
                    ),
                    latest_elo AS (
                        SELECT
                            player_id,
                            elo_seasonal_after_match AS elo_after_match,
                            ROW_NUMBER() OVER (
                                PARTITION BY player_id
                                ORDER BY match_date DESC
                            ) AS rn
                        FROM filtered_pmf
                    ),
                    ranked_players AS (
                        SELECT
                            le.player_id,
                            le.elo_after_match,
                            RANK() OVER (ORDER BY le.elo_after_match DESC) AS rank
                        FROM latest_elo le
                        JOIN match_counts mc
                          ON mc.player_id = le.player_id
                        WHERE le.rn = 1
                          AND mc.total_matches >= :minRankedMatches
                    )
                    SELECT
                        rp.player_id AS playerId,
                        rp.elo_after_match AS elo,
                        rp.rank
                    FROM ranked_players rp
                    ORDER BY rp.rank;
                    """,
            nativeQuery = true
    )
    List<LastKickerEloByDateProjection> getLatestKickerSeasonEloByDate(
            @Param("date") Timestamp date,
            @Param("year") Integer year,
            @Param("quarter") Integer quarter,
            @Param("minRankedMatches") int minRankedMatches
    );

    @Query(
            value = """
                       WITH filtered_pmf AS (
                                  SELECT *
                                  FROM mv_player_match_facts pmf
                                  WHERE EXTRACT(YEAR FROM pmf.match_date) = :year
                                    AND EXTRACT(QUARTER FROM pmf.match_date) = :quarter
                              ),
                              player_stats AS (
                                  SELECT
                                      player_id,
                                      COUNT(*) AS total_matches,
                                      SUM(CASE WHEN player_score = 10 THEN 1 ELSE 0 END) AS wins,
                                      SUM(CASE WHEN player_score <> 10 THEN 1 ELSE 0 END) AS losses,
                                      ROUND(
                                          SUM(CASE WHEN player_score = 10 THEN 1 ELSE 0 END)::numeric
                                          / COUNT(*),
                                          2
                                      ) AS win_rate
                                  FROM filtered_pmf
                                  GROUP BY player_id
                              ),
                              last_season_elo AS (
                                  SELECT
                                      player_id,
                                      elo_seasonal_after_match AS last_season_elo
                                  FROM (
                                      SELECT
                                          player_id,
                                          elo_seasonal_after_match,
                                          ROW_NUMBER() OVER (
                                              PARTITION BY player_id
                                              ORDER BY match_date DESC
                                          ) AS rn
                                      FROM filtered_pmf
                                  ) x
                                  WHERE rn = 1
                              ),
                              ranked_players AS (
                                  SELECT
                                      ps.player_id,
                                      lse.last_season_elo,
                                      RANK() OVER (ORDER BY lse.last_season_elo DESC) AS rank
                                  FROM player_stats ps
                                  JOIN last_season_elo lse
                                    ON lse.player_id = ps.player_id
                                  WHERE ps.total_matches >= :minRankedMatches
                              )
                              SELECT
                                  ps.player_id AS playerId,
                                  p.firstname,
                                  p.lastname,
                                  rp.last_season_elo AS currentElo,
                                  ps.total_matches,
                                  ps.wins,
                                  ps.losses,
                                  ps.win_rate,
                                  COALESCE(rp.rank, 0) AS rank
                              FROM player_stats ps
                              JOIN players p ON p.id = ps.player_id
                              LEFT JOIN ranked_players rp ON rp.player_id = ps.player_id
                              ORDER BY rank;
                    """,
            nativeQuery = true
    )
    List<GlobalStatsResponseProjection> getSeasonPlayerStats(
            @Param("year") Integer year,
            @Param("quarter") Integer quarter,
            @Param("minRankedMatches") int minRankedMatches
    );


    @Query(value = """
                    WITH avg_elo_per_match AS (SELECT avg(kes.elo_before_match) AS avg_elo, kes.match_id, kes.elo_change
                                       FROM kicker_elo AS kes
                                       GROUP BY kes.match_id, kes.elo_change)
            SELECT DISTINCT MAX(CASE WHEN aepm1.elo_change < 0 THEN aepm1.avg_elo END)
                                - MAX(CASE WHEN aepm1.elo_change > 0 THEN aepm1.avg_elo END) AS elo_diff,
                            CASE
                                WHEN km.final_score_team_a = 10 THEN km.final_score_team_b
                                WHEN km.final_score_team_b = 10 THEN km.final_score_team_a
                                END                                                          AS loser_score
            FROM avg_elo_per_match aepm1
                     JOIN avg_elo_per_match AS aepm2 ON aepm1.match_id = aepm2.match_id AND aepm1.elo_change != aepm2.elo_change
                     LEFT JOIN kicker_matches AS km ON aepm1.match_id = km.id
            GROUP BY km.final_score_team_a, km.final_score_team_b, km.id
            ORDER BY elo_diff DESC;
            """,
            nativeQuery = true
    )
    List<LoserScorePerDeltaEloProjection> getLoserScorePerEloDiff();

    @Query("""
            SELECT ke
            FROM KickerEloSeasonal ke
            WHERE ke.id.matchId IN :matchIds
            """)
    List<KickerEloSeasonal> findAllByMatchIdIn(@Param("matchIds") List<UUID> matchIds);

    @Query(value = """
            SELECT
                target.match_id AS matchId,
                target.player_id AS playerId
            FROM kicker_elo_seasonal target
            JOIN kicker_matches target_match ON target_match.id = target.match_id
            WHERE target.match_id IN (:matchIds)
              AND (
                  SELECT COUNT(*)
                  FROM kicker_elo_seasonal previous
                  JOIN kicker_matches previous_match ON previous_match.id = previous.match_id
                  WHERE previous.player_id = target.player_id
                    AND previous.season_year = target.season_year
                    AND previous.season_quarter = target.season_quarter
                    AND previous_match.created_at < target_match.created_at
              ) >= :minRankedMatches
            """, nativeQuery = true)
    List<EloVisibilityProjection> findVisibleSeasonalEloBeforeMatchPairs(
            @Param("matchIds") List<UUID> matchIds,
            @Param("minRankedMatches") int minRankedMatches
    );

    void deleteByMatchCreatedAtAfter(Timestamp date);

    @Query(value = """
            SELECT
                CAST(EXTRACT(YEAR FROM pmf.match_date) AS INTEGER)    AS season_year,
                CAST(EXTRACT(QUARTER FROM pmf.match_date) AS INTEGER) AS season_quarter,
            
                CAST(SUM(CASE WHEN pmf.player_score = 10 THEN 1 ELSE 0 END) AS BIGINT) AS wins,
                CAST(SUM(CASE WHEN pmf.player_score <> 10 THEN 1 ELSE 0 END) AS BIGINT) AS losses
            
            FROM mv_player_match_facts pmf
            WHERE pmf.player_id = :playerId
            
            GROUP BY
                CAST(EXTRACT(YEAR FROM pmf.match_date) AS INTEGER),
                CAST(EXTRACT(QUARTER FROM pmf.match_date) AS INTEGER)
            
            ORDER BY
                season_year DESC,
                season_quarter DESC;
            """, nativeQuery = true)
    List<SeasonalStatsDTO> getSeasonalStats(@Param("playerId") UUID playerId);

    @Query(value = """
            WITH history_player_elo AS (
                SELECT DISTINCT ON (kes.player_id, DATE(km.created_at))
                    DATE(km.created_at) AS match_day,
                    km.created_at AS date,
                    kes.player_id,
                    kes.elo_after_match AS elo
                FROM kicker_elo_seasonal kes
                JOIN kicker_matches km ON km.id = kes.match_id
                WHERE kes.season_year = :year
                  AND kes.season_quarter = :quarter
                ORDER BY kes.player_id, DATE(km.created_at), km.created_at DESC, kes.created_at DESC
            ),
            match_days AS (
                SELECT DISTINCT match_day
                FROM history_player_elo
            ),
            players AS (
                SELECT DISTINCT player_id
                FROM kicker_elo_seasonal
                WHERE season_year = :year
                  AND season_quarter = :quarter
            ),
            daily_player_elo AS (
                SELECT DISTINCT ON (p.player_id, md.match_day)
                    md.match_day,
                    p.player_id,
                    kes.elo_after_match AS elo
                FROM match_days md
                CROSS JOIN players p
                JOIN kicker_elo_seasonal kes ON kes.player_id = p.player_id
                JOIN kicker_matches km ON km.id = kes.match_id
                WHERE kes.season_year = :year
                  AND kes.season_quarter = :quarter
                  AND DATE(km.created_at) <= md.match_day
                ORDER BY p.player_id, md.match_day, km.created_at DESC, kes.created_at DESC
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
            @Param("playerId") UUID playerId,
            @Param("year") int year,
            @Param("quarter") int quarter
    );
}

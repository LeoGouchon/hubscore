package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.dto.EloHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.SeasonalStatsDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloId;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloSeasonal;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.GlobalStatsResponseProjection;
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
                    WITH match_counts AS (
                        SELECT player_id, COUNT(*) AS total_matches
                        FROM kicker_elo_seasonal ke
                                 JOIN kicker_matches m ON m.id = ke.match_id
                        WHERE m.created_at <= :date AND ke.season_quarter = :quarter AND ke.season_year = :year
                        GROUP BY player_id
                    ),
                         latest_elo AS (
                             SELECT
                                 ke.player_id,
                                 ke.match_id,
                                 ke.elo_after_match,
                                 m.created_at,
                                 ROW_NUMBER() OVER (PARTITION BY ke.player_id ORDER BY m.created_at DESC) AS rn
                             FROM kicker_elo_seasonal ke
                                      JOIN kicker_matches m ON m.id = ke.match_id
                             WHERE m.created_at <= :date AND ke.season_quarter = :quarter AND ke.season_year = :year
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
    List<LastKickerEloByDateProjection> getLatestKickerSeasonEloByDate(Timestamp date, Integer year, Integer quarter);

    @Query(
            value = """
                        WITH filtered_matches AS (SELECT *
                                              FROM kicker_matches
                                              WHERE EXTRACT(YEAR FROM created_at) = :year
                                                AND EXTRACT(QUARTER FROM created_at) = :quarter),
                         all_players AS (SELECT player_one_team_a_id AS player_id, final_score_team_a AS score
                                         FROM filtered_matches
                                         UNION ALL
                                         SELECT player_two_team_a_id AS player_id, final_score_team_a AS score
                                         FROM filtered_matches
                                         UNION ALL
                                         SELECT player_one_team_b_id AS player_id, final_score_team_b AS score
                                         FROM filtered_matches
                                         UNION ALL
                                         SELECT player_two_team_b_id AS player_id, final_score_team_b AS score
                                         FROM filtered_matches),
                         player_stats AS (SELECT ap.player_id,
                                                 COUNT(*)                                                                  AS total_matches,
                                                 SUM(CASE WHEN score = 10 THEN 1 ELSE 0 END)                               AS wins,
                                                 SUM(CASE WHEN score != 10 THEN 1 ELSE 0 END)                              AS losses,
                                                 ROUND(SUM(CASE WHEN score = 10 THEN 1 ELSE 0 END)::numeric / COUNT(*), 2) AS win_rate
                                          FROM all_players ap
                                          GROUP BY ap.player_id),
                         last_elo_of_season AS (SELECT kes.player_id,
                                                       kes.elo_after_match as player_season_elo
                                                FROM kicker_elo_seasonal kes
                                                WHERE season_year = :year
                                                  AND season_quarter = :quarter
                                                  AND created_at = (SELECT MAX(created_at)
                                                                    FROM kicker_elo_seasonal
                                                                    WHERE season_year = :year
                                                                      AND season_quarter = :quarter
                                                                      AND player_id = kes.player_id)),
                         ranked_players AS (SELECT ps.player_id,
                                                   RANK() OVER (ORDER BY leos.player_season_elo DESC) AS rank,
                                                   leos.player_season_elo                             as last_season_elo
                                            FROM player_stats ps
                                                     JOIN last_elo_of_season leos ON leos.player_id = ps.player_id
                                            WHERE ps.total_matches >= 10)
                    SELECT ps.player_id                   AS playerId,
                           p.firstname,
                           p.lastname,
                           rp.last_season_elo AS currentElo,
                           ps.total_matches,
                           ps.wins,
                           ps.losses,
                           ps.win_rate,
                           COALESCE(rp.rank, 0)           AS rank
                    FROM player_stats ps
                             JOIN players p ON p.id = ps.player_id
                             JOIN player_kicker_informations pki ON pki.player_id = p.id
                             LEFT JOIN ranked_players rp ON rp.player_id = ps.player_id
                    ORDER BY rank;
                    """,
            nativeQuery = true
    )
    List<GlobalStatsResponseProjection> getSeasonPlayerStats(
            @Param("year") Integer year,
            @Param("quarter") Integer quarter
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

    List<KickerEloSeasonal> findAllByMatchIdIn(List<UUID> matchIds);

    void deleteByMatchCreatedAtAfter(Timestamp date);

    @Query(value = """
            SELECT DISTINCT
                kes.season_year,
                kes.season_quarter,
                SUM(
                    CASE
                        WHEN :playerId IN (km.player_one_team_a_id, km.player_two_team_a_id)
                             AND km.final_score_team_a = 10 THEN 1
                        WHEN :playerId IN (km.player_one_team_b_id, km.player_two_team_b_id)
                             AND km.final_score_team_b = 10 THEN 1
                        ELSE 0
                    END
                ) AS wins,
                SUM(
                    CASE
                        WHEN :playerId IN (km.player_one_team_a_id, km.player_two_team_a_id)
                             AND km.final_score_team_a != 10 THEN 1
                        WHEN :playerId IN (km.player_one_team_b_id, km.player_two_team_b_id)
                             AND km.final_score_team_b != 10 THEN 1
                        ELSE 0
                    END
                ) AS losses
            FROM kicker_elo_seasonal kes
            JOIN kicker_matches km ON km.id = kes.match_id
            WHERE kes.player_id = :playerId
            GROUP BY kes.season_year, kes.season_quarter
            ORDER BY kes.season_year DESC, kes.season_quarter DESC
            """, nativeQuery = true)
    List<SeasonalStatsDTO> getSeasonalStats(@Param("playerId") UUID playerId);

    @Query(value = """
            SELECT kes.created_at AS date, kes.elo_after_match AS elo
            FROM kicker_elo_seasonal kes
            WHERE kes.player_id = :playerId
              AND kes.season_year = :year
              AND kes.season_quarter = :quarter
            ORDER BY kes.created_at
            """, nativeQuery = true)
    List<EloHistoryDTO> getEloHistory(
            @Param("playerId") UUID playerId,
            @Param("year") int year,
            @Param("quarter") int quarter
    );
}

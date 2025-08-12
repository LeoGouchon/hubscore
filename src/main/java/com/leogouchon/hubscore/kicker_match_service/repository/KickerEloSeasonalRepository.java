package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.dto.SeasonStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloId;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloSeasonal;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.SeasonStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

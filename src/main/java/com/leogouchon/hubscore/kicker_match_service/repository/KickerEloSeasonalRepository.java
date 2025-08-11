package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloId;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloSeasonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerElo;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    Optional<KickerElo> findByMatchId(UUID id);

    List<KickerElo> findAllByMatchIdIn(List<UUID> matchIds);
}

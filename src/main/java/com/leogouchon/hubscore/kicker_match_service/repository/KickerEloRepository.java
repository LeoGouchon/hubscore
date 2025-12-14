package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.dto.EloHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerElo;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
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

    List<KickerElo> findAllByMatchIdIn(List<UUID> matchIds);

    void deleteByMatchCreatedAtAfter(Timestamp date);

    @Query(value = """
            SELECT m.created_at AS date, ks.elo_after_match AS elo
            FROM kicker_elo ks
            JOIN kicker_matches m ON ks.match_id = m.id
            WHERE ks.player_id = :playerId
            ORDER BY ks.created_at
            """, nativeQuery = true)
    List<EloHistoryDTO> getEloHistory(
            @Param("playerId") UUID playerId
    );
}

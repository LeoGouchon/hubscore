package com.leogouchon.hubscore.player_service.repository;

import com.leogouchon.hubscore.player_service.entity.Players;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Players, UUID> {
    @Query(value = "SELECT * FROM players p WHERE p.id NOT IN (SELECT u.players_id FROM users u WHERE u.players_id IS NOT NULL)", nativeQuery = true)
    List<Players> findPlayersWithoutUser();
}

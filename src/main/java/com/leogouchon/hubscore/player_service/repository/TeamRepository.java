package com.leogouchon.hubscore.player_service.repository;

import com.leogouchon.hubscore.player_service.entity.Teams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Teams, UUID> {
}

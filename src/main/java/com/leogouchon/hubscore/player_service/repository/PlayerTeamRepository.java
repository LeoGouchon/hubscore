package com.leogouchon.hubscore.player_service.repository;

import com.leogouchon.hubscore.player_service.entity.PlayerTeam;
import com.leogouchon.hubscore.player_service.entity.PlayerTeamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, PlayerTeamId> {
}

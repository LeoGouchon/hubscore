package com.leogouchon.husbcore.player_service.repository;

import com.leogouchon.husbcore.player_service.entity.Players;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Players, UUID>, JpaSpecificationExecutor<Players> {
}

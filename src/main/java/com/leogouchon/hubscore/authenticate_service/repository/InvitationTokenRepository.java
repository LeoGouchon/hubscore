package com.leogouchon.hubscore.authenticate_service.repository;

import com.leogouchon.hubscore.authenticate_service.entity.InvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationTokenRepository extends JpaRepository<InvitationToken, UUID> {
    InvitationToken findByToken(String invitationToken);
    Optional<InvitationToken> findByPlayerId(UUID playerId);
}

package com.leogouchon.hubscore.authenticate_service.repository;

import com.leogouchon.hubscore.authenticate_service.entity.InvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Long> {
    InvitationToken findByToken(String invitationToken);
    Optional<InvitationToken> findByPlayerId(Long playerId);
}

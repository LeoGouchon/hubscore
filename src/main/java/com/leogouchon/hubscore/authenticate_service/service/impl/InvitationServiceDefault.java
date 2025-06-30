package com.leogouchon.hubscore.authenticate_service.service.impl;

import com.leogouchon.hubscore.authenticate_service.entity.InvitationToken;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.user_service.entity.Users;
import com.leogouchon.hubscore.authenticate_service.repository.InvitationTokenRepository;
import com.leogouchon.hubscore.player_service.repository.PlayerRepository;
import com.leogouchon.hubscore.authenticate_service.service.AuthenticateService;
import com.leogouchon.hubscore.authenticate_service.service.InvitationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvitationServiceDefault implements InvitationService {

    private final PlayerRepository playerRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final AuthenticateService authenticateService;

    @Autowired
    public InvitationServiceDefault(
            PlayerRepository playerRepository,
            InvitationTokenRepository invitationTokenRepository,
            AuthenticateService authenticateService
    ) {
        this.playerRepository = playerRepository;
        this.invitationTokenRepository = invitationTokenRepository;
        this.authenticateService = authenticateService;
    }

    @Override
    public String createInvitation(String accessToken, UUID playerId) {
        Users user = authenticateService.getCurrentUser(accessToken);

        if (Boolean.FALSE.equals(user.getIsAdmin())) throw new IllegalArgumentException("User is not admin");

        Players player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));

        Optional<InvitationToken> existingInvitation = invitationTokenRepository.findByPlayerId(player.getId());
        if (existingInvitation.isPresent()) {
            if (Boolean.TRUE.equals(existingInvitation.get().getIsUsed()))
                throw new IllegalArgumentException("The player is already linked to an account");
            if (existingInvitation.get().getExpiryDate().isAfter(LocalDateTime.now()))
                return existingInvitation.get().getToken();
        }
        String token = UUID.randomUUID().toString();
        InvitationToken invitation = new InvitationToken(token, player, LocalDateTime.now().plusDays(3));

        invitationTokenRepository.save(invitation);

        return token;
    }
}
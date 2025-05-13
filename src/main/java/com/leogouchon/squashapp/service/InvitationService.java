package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.model.InvitationToken;
import com.leogouchon.squashapp.model.Players;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.repository.InvitationTokenRepository;
import com.leogouchon.squashapp.repository.PlayerRepository;
import com.leogouchon.squashapp.service.interfaces.IAuthenticateService;
import com.leogouchon.squashapp.service.interfaces.IInvitationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InvitationService implements IInvitationService {

    private final PlayerRepository playerRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final IAuthenticateService authenticateService;

    @Autowired
    public InvitationService(
            PlayerRepository playerRepository,
            InvitationTokenRepository invitationTokenRepository,
            IAuthenticateService authenticateService
    ) {
        this.playerRepository = playerRepository;
        this.invitationTokenRepository = invitationTokenRepository;
        this.authenticateService = authenticateService;
    }

    @Override
    public String createInvitation(String accessToken, Long playerId) {
        Users user = authenticateService.getCurrentUser(accessToken);

        if (Boolean.FALSE.equals(user.getIsAdmin())) throw new IllegalArgumentException("User is not admin");

        Players player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));

        String token = UUID.randomUUID().toString();
        InvitationToken invitation = new InvitationToken();
        invitation.setToken(token);
        invitation.setPlayer(player);
        invitation.setExpiryDate(LocalDateTime.now().plusDays(3));

        invitationTokenRepository.save(invitation);

        return token;
    }
}
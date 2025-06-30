package com.leogouchon.hubscore.authenticate_service.service;

import java.util.UUID;

public interface InvitationService {
    String createInvitation(String accessToken, UUID playerId);
}

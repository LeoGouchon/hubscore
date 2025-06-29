package com.leogouchon.hubscore.authenticate_service.service;

public interface InvitationService {
    String createInvitation(String accessToken, Long playerId);
}

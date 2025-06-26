package com.leogouchon.hubscore.authenticate_service.service;

public interface IInvitationService {
    String createInvitation(String accessToken, Long playerId);
}

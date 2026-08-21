package com.leogouchon.hubscore.user_service.dto;

public record InvitationResponse(String token, String clientId, String applicationName, String invitationUrl) {
}

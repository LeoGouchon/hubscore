package com.leogouchon.hubscore.authenticate_service.service;

import com.leogouchon.hubscore.authenticate_service.dto.AuthenticateRequestDTO;
import com.leogouchon.hubscore.authenticate_service.dto.DoubleTokenDTO;
import com.leogouchon.hubscore.user_service.entity.Users;

import javax.naming.AuthenticationException;

public interface AuthenticateService {
    DoubleTokenDTO login(AuthenticateRequestDTO authenticateRequestDTO) throws AuthenticationException;
    String generateAccessToken(Users user);
    String generateAndSaveRefreshToken(Users user);
    boolean isValidToken(String token);
    Users getUserFromToken(String token);
    void logout(String token);
    String refreshAccessToken(String refreshToken) throws AuthenticationException;
    DoubleTokenDTO signUp(String email, String password, String invitationToken) throws AuthenticationException;
    Users getCurrentUser(String accessToken);
    boolean isUserAdmin(String accessToken);
    void cleanupRevokedRefreshTokens();
}

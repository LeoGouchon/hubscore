package com.leogouchon.hubscore.service.interfaces;

import com.leogouchon.hubscore.dto.AuthenticateRequestDTO;
import com.leogouchon.hubscore.dto.DoubleTokenDTO;
import com.leogouchon.hubscore.model.Users;

import javax.naming.AuthenticationException;

public interface IAuthenticateService {
    DoubleTokenDTO login(AuthenticateRequestDTO authenticateRequestDTO) throws AuthenticationException;
    String generateAccessToken(Users user);
    String generateAndSaveRefreshToken(Users user);
    boolean isValidToken(String token);
    Users getUserFromToken(String token);
    void logout(String token);
    String refreshAccessToken(String refreshToken) throws AuthenticationException;
    DoubleTokenDTO signUp(String email, String password, String invitationToken) throws AuthenticationException;
    Users getCurrentUser(String accessToken);
}

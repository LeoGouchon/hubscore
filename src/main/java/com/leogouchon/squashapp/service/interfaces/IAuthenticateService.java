package com.leogouchon.squashapp.service.interfaces;

import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.dto.AuthenticateResponseDTO;
import com.leogouchon.squashapp.model.Players;
import com.leogouchon.squashapp.model.Users;

import javax.naming.AuthenticationException;

public interface IAuthenticateService {
    AuthenticateResponseDTO login(AuthenticateRequestDTO authRequest) throws AuthenticationException;
    String generateAccessToken(Users user);
    String generateAndSaveRefreshToken(Users user);
    boolean isValidToken(String token);
    Users getUserFromToken(String token);
    void logout(String token);
    String refreshAccessToken(String refreshToken) throws AuthenticationException;
    AuthenticateResponseDTO signIn(String email, String password, Players player) throws AuthenticationException;
}

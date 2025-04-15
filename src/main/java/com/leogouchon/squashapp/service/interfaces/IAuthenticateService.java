package com.leogouchon.squashapp.service.interfaces;

import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.model.Users;

import javax.naming.AuthenticationException;

public interface IAuthenticateService {
    String login(AuthenticateRequestDTO user) throws AuthenticationException;
    void logout(String token);
    boolean isValidToken(String token);
    String generateToken(Users user);
    boolean matchPassword(Users existingUsers, String unhashedPassword);
}

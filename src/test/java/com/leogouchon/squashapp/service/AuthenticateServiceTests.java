package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.repository.UserRepository;
import com.leogouchon.squashapp.utils.UsersMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.naming.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateServiceTests {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Spy
    @InjectMocks
    private AuthenticateService authenticateService;

    @Test
    public void testLoginSuccess() throws AuthenticationException {
        String email = "john.doe@mail.com";
        String password = "P@s$w0rD";
        AuthenticateRequestDTO requestArg = UsersMapper.toAuthenticateRequestDTO(new Users(email, password));
        Users mockExistingUser = new Users(email, password);

        when(userService.getUserByEmail(email)).thenReturn(mockExistingUser);
        doReturn(true).when(authenticateService).matchPassword(any(Users.class), any(String.class));
        doReturn("t0k3N").when(authenticateService).generateToken(mockExistingUser);

        String result = authenticateService.login(requestArg);

        assertNotNull(result);
        verify(userService).getUserByEmail(email);
        verify(authenticateService).matchPassword(mockExistingUser, password);
        verify(authenticateService).generateToken(mockExistingUser);
        verify(userService).updateTokenUser(mockExistingUser);
    }

    @Test
    public void testLoginFail() {
        String email = "john.doe@mail.com";
        String password = "P@s$w0rD";
        AuthenticateRequestDTO requestArg = UsersMapper.toAuthenticateRequestDTO(new Users(email, password));
        when(userService.getUserByEmail(email)).thenReturn(null);

        assertThrows(AuthenticationException.class, () -> authenticateService.login(requestArg));
    }
}

package com.leogouchon.squashapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leogouchon.squashapp.dto.TokenRefreshRequestDTO;
import com.leogouchon.squashapp.dto.TokenRequestDTO;
import com.leogouchon.squashapp.service.UserService;
import com.leogouchon.squashapp.service.interfaces.IAuthenticateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.naming.AuthenticationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticateController.class)
@ExtendWith(SpringExtension.class)
public class AuthenticateControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IAuthenticateService authenticateService;

    @MockitoBean
    private UserService userService;

    @Test
    public void testLogoutSuccess() throws Exception {
        TokenRequestDTO request = new TokenRequestDTO("t0k3nValUe");

        mockMvc.perform(post("/api/authenticate/logout")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testRefreshTokenSuccess() throws Exception {
        TokenRefreshRequestDTO request = new TokenRefreshRequestDTO("refreshTokenValue");

        when(authenticateService.refreshAccessToken(any(String.class))).thenReturn("newAccessToken");

        mockMvc.perform(post("/api/authenticate/refresh-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"newAccessToken\"}"));
    }

    @Test
    public void testRefreshTokenFailure() throws Exception {
        TokenRefreshRequestDTO request = new TokenRefreshRequestDTO("refreshTokenValue");

        when(authenticateService.refreshAccessToken(any(String.class))).thenThrow(new AuthenticationException("Invalid refresh token"));

        mockMvc.perform(post("/api/authenticate/refresh-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

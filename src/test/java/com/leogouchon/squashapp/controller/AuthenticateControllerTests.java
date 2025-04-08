package com.leogouchon.squashapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.service.AuthenticateService;
import com.leogouchon.squashapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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
    private AuthenticateService authenticateService;

    @MockitoBean
    private UserService userService;

    @Test
    public void testLoginSuccess() throws Exception {
        AuthenticateRequestDTO request = new AuthenticateRequestDTO("email@mail.com", "password");

        when(authenticateService.login(any(AuthenticateRequestDTO.class))).thenReturn("t0k3nValUe");

        mockMvc.perform(post("/api/authenticate/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"t0k3nValUe\"}"));

    }

    @Test
    public void testLoginFailure() throws Exception {
        AuthenticateRequestDTO request = new AuthenticateRequestDTO("email@mail.com", "password");

        when(authenticateService.login(any(AuthenticateRequestDTO.class))).thenThrow(new AuthenticationException("user or password incorrect"));

        mockMvc.perform(post("/api/authenticate/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void testSignupSuccess() throws Exception {
        Users user = new Users("john.doe@mail.com", "p4s$w0rD");

        when(userService.createUser(any(Users.class))).thenReturn(user);

        when(authenticateService.generateToken(any(Users.class))).thenReturn("t0k3nValUe");

        mockMvc.perform(post("/api/authenticate/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    @Test
    public void testSignupFailure() throws Exception {
        Users user = new Users("john.doe@mail.com", "p4s$w0rD");

        when(userService.createUser(any(Users.class))).thenThrow(new IllegalArgumentException("Email already exists"));

        mockMvc.perform(post("/api/authenticate/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }
}

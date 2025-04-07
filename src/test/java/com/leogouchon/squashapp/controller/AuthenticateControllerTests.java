package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.service.AuthenticateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import javax.naming.AuthenticationException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticateController.class)
public class AuthenticateControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AuthenticateService authenticateService;

    @InjectMocks
    private AuthenticateController authenticateController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginSuccess() throws Exception {
        AuthenticateRequestDTO request = new AuthenticateRequestDTO("email@mail.com", "password");

        when(authenticateService.login(request)).thenReturn("token");

        mockMvc.perform(post("}/api/authenticate/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"email@mail.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginFailure() throws Exception {
        AuthenticateRequestDTO request = new AuthenticateRequestDTO("email@mail.com", "password");

        when(authenticateService.login(request)).thenThrow(new AuthenticationException("user or password incorrect"));

        mockMvc.perform(post("/authenticate/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"email@mail.com\", \"password\":\"password\"}")
            ).andExpect(status().isUnauthorized());
    }
}

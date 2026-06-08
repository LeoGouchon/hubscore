package com.leogouchon.hubscore.authenticate_service.service.controller;

import com.leogouchon.hubscore.authenticate_service.controller.AuthenticateController;
import com.leogouchon.hubscore.authenticate_service.dto.AuthenticateRequestDTO;
import com.leogouchon.hubscore.authenticate_service.dto.DoubleTokenDTO;
import com.leogouchon.hubscore.authenticate_service.service.AuthenticateService;
import com.leogouchon.hubscore.common.security.JwtAuthorizationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthenticateController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticateControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticateService authenticateService;

    @MockBean
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Test
    void loginReturnsAccessTokenAndRefreshTokenCookie() throws Exception {
        AuthenticateRequestDTO request = new AuthenticateRequestDTO("john.doe@mail.com", "P@s$w0rD");
        when(authenticateService.login(org.mockito.ArgumentMatchers.any(AuthenticateRequestDTO.class)))
                .thenReturn(new DoubleTokenDTO("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/authenticate/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=refresh-token")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/api/v1/authenticate")));
    }

    @Test
    void refreshTokenRotatesCookieAndReturnsNewAccessToken() throws Exception {
        when(authenticateService.refreshTokens("old-refresh-token"))
                .thenReturn(new DoubleTokenDTO("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/api/v1/authenticate/refresh-token")
                        .cookie(new Cookie("refreshToken", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=new-refresh-token")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/api/v1/authenticate")));
    }

    @Test
    void logoutRevokesCurrentRefreshTokenAndClearsCookie() throws Exception {
        mockMvc.perform(post("/api/v1/authenticate/logout")
                        .cookie(new Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/api/v1/authenticate")));

        verify(authenticateService).logout("refresh-token");
    }
}

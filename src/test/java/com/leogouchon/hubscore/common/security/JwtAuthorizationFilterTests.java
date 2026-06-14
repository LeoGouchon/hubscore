package com.leogouchon.hubscore.common.security;

import com.leogouchon.hubscore.user_service.service.UserService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class JwtAuthorizationFilterTests {

    private final JwtAuthorizationFilter filter = new JwtAuthorizationFilter(
            mock(JwtTokenService.class),
            mock(UserService.class)
    );

    @Test
    void logoutIsExcludedFromBearerTokenAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/authenticate/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertTrue(chainCalled.get());
    }

    @Test
    void protectedEndpointWithoutBearerTokenReturnsUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/players");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        assertEquals(MockHttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
}

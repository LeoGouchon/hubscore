package com.leogouchon.hubscore.common.security;

import com.leogouchon.hubscore.authenticate_service.service.AuthenticateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Set;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final AuthenticateService authenticateService;

    private static final Set<String> UNCONDITIONAL_EXCLUDED_PATHS = Set.of(
            "/api/v1/authenticate/login",
            "/api/v1/authenticate/signup",
            "/api/v1/authenticate/refresh-token",
            "/api/v1/ping"
    );

    private static final Set<String> GET_ONLY_EXCLUDED_PATHS = Set.of(
            "/api/v1/kicker/matches",
            "/api/v1/players"
    );

    @Autowired
    public AuthorizationInterceptor(AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true; // Let CORS preflight pass
        }

        if (isExcluded(path, method)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        if (!authenticateService.isValidToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return false;
        }

        // Optionally set user details in request attribute for use downstream
        request.setAttribute("user", authenticateService.getUserFromToken(token));
        return true;
    }

    private boolean isExcluded(String path, String method) {
        if (UNCONDITIONAL_EXCLUDED_PATHS.contains(path)) {
            return true;
        }

        return "GET".equalsIgnoreCase(method) && GET_ONLY_EXCLUDED_PATHS.contains(path);
    }
}

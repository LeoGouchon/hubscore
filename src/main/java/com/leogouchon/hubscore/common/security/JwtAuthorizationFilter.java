package com.leogouchon.hubscore.common.security;

import com.leogouchon.hubscore.user_service.entity.Users;
import com.leogouchon.hubscore.user_service.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    private static final Set<String> UNCONDITIONAL_EXCLUDED_PATHS = Set.of(
            "/api/v1/authenticate/login",
            "/api/v1/authenticate/signup",
            "/api/v1/authenticate/refresh-token",
            "/api/v1/ping"
    );

    private static final List<Pattern> GET_ONLY_EXCLUDED_PATTERNS = List.of(
            Pattern.compile("/api/v1/kicker/matches"),
            Pattern.compile("/api/v1/players"),
            Pattern.compile("/api/v1/kicker/stats/global"),
            Pattern.compile("/api/v1/kicker/stats/season(/.*)?"),
            Pattern.compile("/api/v1/kicker/stats/matrix-score(/.*)?"),
            Pattern.compile("/api/v1/kicker/stats/player(/.*)?")
    );

    public JwtAuthorizationFilter(JwtTokenService jwtTokenService, UserService userService) {
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (isExcluded(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtTokenService.isValid(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        Users user = userService.getUserById(jwtTokenService.extractUserId(token));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        mapAuthorities(user)
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private boolean isExcluded(String path, String method) {
        if (UNCONDITIONAL_EXCLUDED_PATHS.contains(path)) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)) {
            return GET_ONLY_EXCLUDED_PATTERNS.stream()
                    .anyMatch(p -> p.matcher(path).matches());
        }
        return false;
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(Users user) {
        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}

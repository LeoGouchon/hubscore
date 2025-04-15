package com.leogouchon.squashapp.security;

import com.leogouchon.squashapp.service.interfaces.IAuthenticateService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final IAuthenticateService authenticateService;

    @Autowired
    public AuthorizationInterceptor(IAuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        // Maybe it exists cleaner way
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Cookie token = WebUtils.getCookie(request, "token");
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (!authenticateService.isValidToken(token.getValue())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}

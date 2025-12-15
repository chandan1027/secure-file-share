package com.cybersecurex.config;

import com.cybersecurex.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private static final Set<String> OPEN_PATH_PREFIXES = Set.of(
            "/api/auth",
            "/static",
            "/css",
            "/js",
            "/favicon.ico",
            "/error"
    );

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();

        // allow home and login page
        if (path.equals("/") || path.equals("/index") || path.equals("/index.html")) {
            return true;
        }
        for (String open : OPEN_PATH_PREFIXES) {
            if (path.startsWith(open)) {
                return true;
            }
        }

        String token = request.getHeader("X-Auth-Token");
        if (tokenService.isValid(token)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"error\",\"message\":\"Unauthorized: login required\"}");
        return false;
    }
}


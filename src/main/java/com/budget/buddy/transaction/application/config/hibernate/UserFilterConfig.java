package com.budget.buddy.transaction.application.config.hibernate;

import com.budget.buddy.core.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserFilterConfig extends OncePerRequestFilter {

    private final UserContext enableHibernateFilter;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");

            String bearToken = Optional.ofNullable(authHeader).orElse("");

            if (bearToken.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                enableHibernateFilter.setUser(email);
            }

            filterChain.doFilter(request, response);
        } finally {
            enableHibernateFilter.clear();
        }
    }
}


package com.budget.buddy.user.application.config.security;

import jakarta.persistence.EntityManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class HibernateUserFilterConfigurer extends OncePerRequestFilter {

    private final EntityManager entityManager;

    private final UserContext userContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Long userId = userContext.getCurrentUserId();
        if (userId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("userFilter").setParameter("userId", userId);
        }
        filterChain.doFilter(request, response);
    }
}


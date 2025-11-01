package com.budget.buddy.transaction.application.config.aspect;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import static com.budget.buddy.core.utils.ApplicationUtil.getUserIdFromContext;

@Aspect
@Component
@RequiredArgsConstructor
public class HibernateTenantFilterAspect {
    private final EntityManager entityManager;
    private static final String USER_FILTER_NAME = "userFilter";

    @Around("execution(* com.budget.buddy.transaction.domain.service.impl..*(..))")
    public Object aroundTx(ProceedingJoinPoint pjp) throws Throwable {
        var session = entityManager.unwrap(org.hibernate.Session.class);
        var filter = session.getEnabledFilter(USER_FILTER_NAME);
        boolean enabledHere = false;

        try {
            if (filter == null) {
                Long userId = getUserIdFromContext();
                session.enableFilter(USER_FILTER_NAME).setParameter("userId", userId);
                enabledHere = true;
            }
            return pjp.proceed();
        } finally {
            if (enabledHere) {
                session.disableFilter(USER_FILTER_NAME);
            }
        }
    }

}

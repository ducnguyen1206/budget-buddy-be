package com.budget.buddy.transaction.application.config.aspect;

import com.budget.buddy.transaction.application.config.hibernate.UserContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class HibernateTenantFilterAspect {

    private final EntityManager entityManager;
    private final UserContext userContext;

    @Around("execution(* com.budget.buddy.transaction.domain.service.impl..*(..))")
    public Object aroundTx(ProceedingJoinPoint pjp) throws Throwable {
        var session = entityManager.unwrap(org.hibernate.Session.class);
        var filter = session.getEnabledFilter("userFilter");
        boolean enabledHere = false;

        try {
            if (userContext.getUser() != null && filter == null) {
                session.enableFilter("userFilter")
                        .setParameter("userId", userContext.getUser());
                enabledHere = true;
            }
            return pjp.proceed();
        } finally {
            if (enabledHere) {
                session.disableFilter("userFilter");
            }
        }
    }

}

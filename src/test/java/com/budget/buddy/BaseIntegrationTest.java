package com.budget.buddy;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=test-secret",
        "feature.enabled=true",
        "spring.liquibase.enabled=false",
        "spring.email.username=nmd12061999@gmail.com",
        "spring.email.password=tsww ghzr eqih",
        "spring.application.event.publishers.enabled=false"
})
public abstract class BaseIntegrationTest {
}

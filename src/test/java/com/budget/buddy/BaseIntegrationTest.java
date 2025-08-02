package com.budget.buddy;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=test-secret",
        "feature.enabled=true",
        "spring.liquibase.enabled=false"
})
public abstract class BaseIntegrationTest {
}

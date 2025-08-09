package com.budget.buddy;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=kP/UI9gVKCL682ACTLaEmSUzg8omKbRHGtz7AZO7flEbXx4ZJY0G/5ycUoCfJQwkrCbQkZIQbw6K/6dHWDZSYg==",
        "feature.enabled=true",
        "spring.liquibase.enabled=false",
        "spring.email.username=nmd12061999@gmail.com",
        "spring.email.password=tsww ghzr eqih",
        "spring.application.event.publishers.enabled=false"
})
public abstract class BaseIntegrationTest {
}

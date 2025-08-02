package com.budget.buddy;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class BudgetBuddyApplicationTests {

    @Test
    void createApplicationModuleModel() {
        ApplicationModules modules = ApplicationModules.of(BudgetBuddyApplication.class);
        modules.forEach(System.out::println);
        modules.verify();
    }
}

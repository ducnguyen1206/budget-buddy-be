package com.budget.buddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.ApplicationModule;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@ApplicationModule
public class BudgetBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetBuddyApplication.class, args);
    }

}

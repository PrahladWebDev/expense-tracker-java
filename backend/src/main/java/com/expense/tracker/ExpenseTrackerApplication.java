package com.expense.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the Spring Boot application.
 *
 * @SpringBootApplication combines three annotations:
 *   - @Configuration: this class can define beans
 *   - @EnableAutoConfiguration: Spring Boot auto-configures beans based on
 *     the dependencies on the classpath (e.g. sees spring-boot-starter-web,
 *     configures an embedded Tomcat server automatically)
 *   - @ComponentScan: scans this package and sub-packages for classes
 *     annotated with @Component, @Service, @Repository, @Controller, etc.
 *     and registers them as beans in the Spring container (IoC container)
 */
@SpringBootApplication
@EnableScheduling // activates @Scheduled methods app-wide (see RefreshTokenCleanupJob)
public class ExpenseTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}

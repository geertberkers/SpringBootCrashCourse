package com.plcoding.spring_boot_crash_course.env;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("staging")
public class StagingDiagnostics {

    @Bean
    public CommandLineRunner run() {
        return args -> System.out.println("Hello from staging!");
    }
}
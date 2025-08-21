package com.epam.hw.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
public class TrainingTypeDefaultsConfig {

    @Bean
    @Profile("local")
    public List<String> localTrainingTypeDefaults() {
        return List.of("JavaScript", "Python", "C#", "Spring");
    }

    @Bean
    @Profile("dev")
    public List<String> devTrainingTypeDefaults() {
        return List.of("Java", "Go", "Rust");
    }

    @Bean
    @Profile("stg")
    public List<String> stgTrainingTypeDefaults() {
        return List.of("Node.js", "Ruby", "Kotlin");
    }

    @Bean
    @Profile("prod")
    public List<String> prodTrainingTypeDefaults() {
        return List.of("Java", "Spring Boot", "Docker");
    }
}
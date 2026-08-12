package com.edison.scanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ApplicationConfig applicationConfig() {
        return new ApplicationConfig();
    }

}
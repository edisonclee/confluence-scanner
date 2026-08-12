package com.edison.scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point.
 */
@SpringBootApplication
public class ScannerWebApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                ScannerWebApplication.class,
                args);

    }

}
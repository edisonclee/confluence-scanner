package com.edison.scanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration {

	@Bean
	WebMvcConfigurer corsConfigurer() {

		return new WebMvcConfigurer() {

			@Override
			public void addCorsMappings(CorsRegistry registry) {

				registry.addMapping("/**")
		        .allowedOrigins(
		                "http://localhost:5173",
		                "https://confluence-ui-ub41.onrender.com")
		        .allowedMethods("*")
		        .allowedHeaders("*");

			}

		};

	}

}

package com.edison.scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ScannerWebApplication {

	public static void main(String[] args) {

		SpringApplication application = new SpringApplication(ScannerWebApplication.class);

		ConfigurableApplicationContext context = application.run(args);

		if (Boolean.parseBoolean(context.getEnvironment().getProperty("scanner.job.enabled", "false"))) {

			int exitCode = SpringApplication.exit(context);

			System.exit(exitCode);
		}
	}
}

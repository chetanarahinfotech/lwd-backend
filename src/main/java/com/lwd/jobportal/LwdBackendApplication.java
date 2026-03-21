package com.lwd.jobportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LwdBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LwdBackendApplication.class, args);
	}

}

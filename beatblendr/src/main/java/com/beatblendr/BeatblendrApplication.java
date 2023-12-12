package com.beatblendr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BeatblendrApplication {

	public static void main(String[] args) {
		// SpringApplication.run() launches the Spring Boot application.
		// BeatblendrApplication tells spring this is primary class
		SpringApplication.run(BeatblendrApplication.class, args);
	}
}

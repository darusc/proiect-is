package com.example.proiectis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProiectisApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProiectisApplication.class, args);
	}

}

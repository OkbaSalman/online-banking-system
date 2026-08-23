package com.banking.transfers_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransfersServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransfersServiceApplication.class, args);
	}

}

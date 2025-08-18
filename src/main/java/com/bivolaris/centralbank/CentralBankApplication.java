package com.bivolaris.centralbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CentralBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(CentralBankApplication.class, args);
	}

}

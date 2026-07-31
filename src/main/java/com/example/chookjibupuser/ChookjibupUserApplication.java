package com.example.chookjibupuser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ChookjibupUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChookjibupUserApplication.class, args);
	}

}

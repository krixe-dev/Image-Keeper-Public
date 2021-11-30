package com.keeper.image.manager;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class ManagerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManagerServiceApplication.class, args);
	}

	/**
	 * Initializing ModelMapper class for all mapping operations
	 */
	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}

//	/**
//	 * Initializing RestTemplate class for all REST communications
//	 */
//	@Bean
//	public RestTemplate restTemplate(RestTemplateBuilder builder) {
//		return builder
//				.setConnectTimeout(Duration.ofMillis(3000))
//				.setReadTimeout(Duration.ofMillis(3000))
//				.build();
//	}
}

package com.example.springbootapplication;

import com.example.springbootapplication.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringbootapplicationApplication implements CommandLineRunner {

	@Autowired
	private RedisCacheService redisCacheService;

	public static void main(String[] args) {
		SpringApplication.run(SpringbootapplicationApplication.class, args);
		System.out.println("Hello buddy");
	}

	@Override
	public void run(String... args) {
		System.out.println("Started run method...");

		redisCacheService.putSimple("name", "rohit");
		String value = redisCacheService.simpleGet("name");
		System.out.println("name: " + value);
		System.out.println(new Exception().getStackTrace());
	}
}

package me.stky.relaytd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public class RelaytdApplication {

	public static void main(String[] args) {
		SpringApplication.run(RelaytdApplication.class, args);
		System.out.println("http://localhost:8080/");
		System.out.println("http://localhost:8080/swagger-ui/index.html");
		System.out.println("http://localhost:8080/astres/welcome");
	}

}

package com.marche.place.Marche;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.marche.place.Marche.repository")
@EntityScan("com.marche.place.Marche.entity")
@ComponentScan(basePackages = "com.marche.place")  // Scan tous les composants
public class PlaceMarcheApplication {
	public static void main(String[] args) {
		SpringApplication.run( PlaceMarcheApplication.class, args);
	}
}

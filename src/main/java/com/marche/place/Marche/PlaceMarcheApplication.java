package com.marche.place.Marche;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.marche.place.Marche.entity")  // Correction du package des entités
@ComponentScan("com.marche.place.Marche")      // Scan tous les composants
public class PlaceMarcheApplication {
	public static void main(String[] args) {
		SpringApplication.run(PlaceMarcheApplication.class, args);
	}
}

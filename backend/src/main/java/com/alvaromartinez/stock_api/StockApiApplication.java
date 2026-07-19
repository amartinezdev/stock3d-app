package com.alvaromartinez.stock_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque de la app. @SpringBootApplication activa la
 * autoconfiguración y el escaneo de beans (@Component, @Service...)
 * de este paquete hacia abajo.
 */
@SpringBootApplication
public class StockApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockApiApplication.class, args);
	}

}

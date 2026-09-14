package com.bank.mscredit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Microservicio de gestion de creditos del sistema bancario.
 * Expone CRUD completo y otorgamiento de creditos personales (max. 1 por cliente)
 * y empresariales (N por cliente), y publica el evento bank.credit.granted
 * al otorgar un nuevo credito.
 */
@EnableMongoAuditing
@SpringBootApplication
public class MsCreditApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsCreditApplication.class, args);
    }
}

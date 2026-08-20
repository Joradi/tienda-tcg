package com.tiendatcg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TiendaTcgApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiendaTcgApplication.class, args);
    }

}

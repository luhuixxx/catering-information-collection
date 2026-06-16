package com.catering.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.catering")
public class CateringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CateringApplication.class, args);
    }
}

package com.gzy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringCsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringCsApplication.class, args);
    }
}
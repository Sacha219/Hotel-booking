package com.example.hotelbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HotelbookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelbookingApplication.class, args);
    }
}
package com.safesenai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SafeSenAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafeSenAiApplication.class, args);
    }
}


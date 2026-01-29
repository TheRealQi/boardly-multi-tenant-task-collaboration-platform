package com.boardly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BoardlyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardlyBackendApplication.class, args);
    }

}

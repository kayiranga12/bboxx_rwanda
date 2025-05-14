package com.bboxxtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BboxxTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(BboxxTrackApplication.class, args);
    }

}

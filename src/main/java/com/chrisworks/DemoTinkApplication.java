package com.chrisworks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DemoTinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoTinkApplication.class, args);
    }

}
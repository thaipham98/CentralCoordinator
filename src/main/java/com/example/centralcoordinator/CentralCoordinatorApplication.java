package com.example.centralcoordinator;

import Configuration.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@RestController
//@EnableKafka
@EnableConfigurationProperties(ApplicationProperties.class)
//@EnableScheduling
public class CentralCoordinatorApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        System.out.println(args);
        SpringApplication.run(CentralCoordinatorApplication.class, args);
    }
}

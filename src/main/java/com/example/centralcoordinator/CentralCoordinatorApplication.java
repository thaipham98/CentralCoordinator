package com.example.centralcoordinator;

import Configuration.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(ApplicationProperties.class)
public class CentralCoordinatorApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(CentralCoordinatorApplication.class, args);
    }
}

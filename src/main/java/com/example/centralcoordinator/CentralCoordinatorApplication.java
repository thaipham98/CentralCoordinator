package com.example.centralcoordinator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@RestController
public class CentralCoordinatorApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(CentralCoordinatorApplication.class, args);
    }
}

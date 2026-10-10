package com.maxchauo.backendapilanguagelab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Java 17 Spring MVC API server entrypoint. */
@SpringBootApplication
public class Application {
    /** Starts the Spring application. */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

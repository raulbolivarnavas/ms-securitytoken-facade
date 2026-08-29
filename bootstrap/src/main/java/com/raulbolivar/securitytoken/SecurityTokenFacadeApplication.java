package com.raulbolivar.securitytoken;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.raulbolivar.securitytoken")
public class SecurityTokenFacadeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityTokenFacadeApplication.class, args);
    }
}

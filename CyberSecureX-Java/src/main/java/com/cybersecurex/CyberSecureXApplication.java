package com.cybersecurex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CyberSecureXApplication {
    public static void main(String[] args) {
        SpringApplication.run(CyberSecureXApplication.class, args);
        System.out.println("🔐 File sharing app is running");
        System.out.println("Visit your configured port (default http://localhost:8080)");
    }
}
package com.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AuthenticServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticServiceApplication.class, args);
    }

}

package com.auth.auth.config;
import io.grpc.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.auth.auth.config.interceptor.HeaderServerInterceptor;
import com.auth.auth.controller.AuthServiceImpl;
import io.grpc.ServerBuilder;


import java.io.IOException;


@Configuration
public class SecurityConfig {
   
@Bean
public Server grpcServer(AuthServiceImpl authService) throws IOException {

    Server server= ServerBuilder
            .forPort(9091)
            .addService(authService)
            .intercept(new HeaderServerInterceptor())
            .build()
            .start();
            System.out.println("gRPC Server started on port 9092");
    return server;
}




    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
package com.auth.auth.config;
import io.grpc.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
// import net.devh.boot.grpc.server.config.GrpcServerConfigurer;

import com.auth.auth.config.interceptor.HeaderServerInterceptor;
import com.auth.auth.controller.AuthServiceImpl;
import com.auth.auth.service.KeyCloakAuth;
// import com.auth.auth.config.interceptor.HeaderServerInterceptor;
import com.auth.auth.controller.AuthServiceImpl;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
// import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
@Configuration
public class SecurityConfig {
   
@Bean
public Server grpcServer(AuthServiceImpl authService) throws IOException {
    return ServerBuilder
            .forPort(9090)
            .addService(authService)
            .intercept(new HeaderServerInterceptor())
            .build()
            .start();
}




    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
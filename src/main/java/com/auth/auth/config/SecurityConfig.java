package com.auth.auth.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;


import com.fasterxml.jackson.databind.ObjectMapper;

import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;






@Configuration
public class SecurityConfig {
@Bean
public ObjectMapper objectMapper() {
    return new ObjectMapper();
}

//     @GrpcGlobalServerInterceptor
//     HeaderServerInterceptor headerServerInterceptor() {
//         return new HeaderServerInterceptor();
//     }
   
// @Bean
// public Server grpcServer(AuthServiceImpl authService) throws IOException {

//     Server server= ServerBuilder
//             .forPort(9091)
//             .addService(authService)
//             .build()
//             .start();
//             System.out.println("gRPC Server started on port 9091");
//     return server;
// }
    //   private final ApiKeyServerInterceptor apiKeyServerInterceptor;

    // public GrpcServerManual(ApiKeyServerInterceptor apiKeyServerInterceptor) {
    //     this.apiKeyServerInterceptor = apiKeyServerInterceptor;
    // }

    // public void start() throws Exception {
    //     // Build the server and add your interceptor
    //     Server server = ServerBuilder.forPort(8080)
    //         .addService(new YourGrpcServiceImpl()) // Replace with your actual service implementation
    //         .intercept(apiKeyServerInterceptor)
    //         .build()
    //         .start();

    //     System.out.println("Server started, listening on " + server.getPort());
    //     server.awaitTermination();
    // }




    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
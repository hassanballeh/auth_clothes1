package com.auth.auth.config;

import io.grpc.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyServerInterceptor implements ServerInterceptor {

    @Value("${api.gateway-key}")
    private String expectedApiKey;

    private static final Metadata.Key<String> API_KEY_HEADER =
            Metadata.Key.of("X-API-GATEWAY-KEY", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Extract the API key from headers
        String apiKey = headers.get(API_KEY_HEADER);

        // Validate the API key
        if (expectedApiKey != null && expectedApiKey.equals(apiKey)) {
            System.out.println("hassan:");
            // API key is valid, proceed with the call
            return next.startCall(call, headers);
        } else {
            // API key is invalid or missing, reject the call
            call.close(Status.UNAUTHENTICATED
                    .withDescription("Invalid or missing API Gateway Key"), 
                    new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
    }
}
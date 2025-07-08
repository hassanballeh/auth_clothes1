package com.auth.auth.config.interceptor;

import io.grpc.*;

public class HeaderServerInterceptor implements ServerInterceptor {
public static final Context.Key<String> AUTH_TOKEN_KEY = Context.key("authToken");
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        // Extract the Authorization header
        String authHeader = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        // System.out.println("ssss ::"+authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Contexts.interceptCall(Context.current().withValue(AUTH_TOKEN_KEY, authHeader), call, headers, next);
        } else {
            // Handle missing or malformed token as needed
            System.out.println("No valid Bearer token found");
        }
        
        return next.startCall(call, headers);
    }
}

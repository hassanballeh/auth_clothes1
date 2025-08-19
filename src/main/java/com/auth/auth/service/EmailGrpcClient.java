package com.auth.auth.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.email.grpc.*;

import org.springframework.stereotype.Component;

@Component
public class EmailGrpcClient {

    private final EmailServiceGrpc.EmailServiceBlockingStub emailStub;

    public EmailGrpcClient() {
        // 👇 Adjust host/port to where your Email service is running
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        emailStub = EmailServiceGrpc.newBlockingStub(channel);
    }

    public boolean sendEmail(String email, String username) {
        SendEmailRequest request = SendEmailRequest.newBuilder()
                .setEmail(email)
                .setUsername(username)
                .build();

        SendEmailResponse response = emailStub.sendEmail(request);
        return response.getSuccess();
    }
}

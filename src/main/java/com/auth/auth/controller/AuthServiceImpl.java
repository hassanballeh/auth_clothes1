package com.auth.auth.controller;

import org.springframework.grpc.server.service.GrpcService;

import com.auth.auth.dto.LoginDto;
import com.auth.auth.dto.LogoutDto;
import com.auth.auth.dto.RefreshTokenDto;
import com.auth.auth.dto.UserDto;
import com.auth.auth.service.KeyCloakAuth;
import com.auth.grpc.*;
// import org.springframework.beans.factory.annotation.Autowired;
import com.auth.auth.config.interceptor.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


import io.grpc.stub.StreamObserver;
import lombok.NoArgsConstructor;
import io.grpc.ServerCall.Listener;
import io.grpc.*;


@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    
    private final KeyCloakAuth keyCloakAuth;
    
    public AuthServiceImpl(KeyCloakAuth keyCloakAuth) {
        this.keyCloakAuth = keyCloakAuth;
    }
    
    



    @Override
    public void register(User request, StreamObserver<RegisterResponse> responseObserver) {
    
        // Create UserDto from the gRPC User message
        UserDto userDto = new UserDto();
        userDto.setUsername(request.getUsername());
        userDto.setEmail(request.getEmail());
        userDto.setPassword(request.getPassword());
        userDto.setFirstName(request.getFirstName());
        userDto.setLastName(request.getLastName());
        
        try {
            ResponseEntity<?> responseEntity = keyCloakAuth.registerUser(userDto);
            
            RegisterResponse responseBuilder = RegisterResponse.newBuilder().setSuccess(responseEntity.getStatusCode().value()==201?true:false).setMessage(responseEntity.getBody() != null ? responseEntity.getBody().toString() : "No response body").setStatusCode(responseEntity.getStatusCodeValue()).build();
    
            responseObserver.onNext(responseBuilder);
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        
        responseObserver.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
    
        // Create UserDto from the gRPC User message
        LoginDto userDto = new LoginDto();
        userDto.setEmail(request.getEmail());
        userDto.setPassword(request.getPassword());
        
        
        try {
            ResponseEntity<?> responseEntity = keyCloakAuth.login(userDto);
            
            LoginResponse responseBuilder = LoginResponse.newBuilder().setData((responseEntity.getBody().toString())).build();
    
            responseObserver.onNext(responseBuilder);
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        responseObserver.onCompleted();
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
    
        // Create UserDto from the gRPC User message
        LogoutDto userDto = new LogoutDto();
        userDto.setRefreshToken(request.getRefreshToken());
        
        
        try {
            Boolean responseEntity = keyCloakAuth.logout(userDto.getRefreshToken());
            
            LogoutResponse responseBuilder = LogoutResponse.newBuilder().setLogout(responseEntity).build();
    
            responseObserver.onNext(responseBuilder);
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        responseObserver.onCompleted();
    }

    @Override
    public void refreshToken(RefreshRequest request, StreamObserver<RefreshResponse> responseObserver) {
    
        // Create UserDto from the gRPC User message
        RefreshTokenDto userDto = new RefreshTokenDto();
        userDto.setRefreshToken(request.getRefreshToken());
        
        
        try {
            ResponseEntity<?> responseEntity = keyCloakAuth.refreshToken(userDto);
            
            RefreshResponse responseBuilder = RefreshResponse.newBuilder().setData(responseEntity.getBody().toString()).build();
    
            responseObserver.onNext(responseBuilder);
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getUserID(Empty request, StreamObserver<UserIdResponse> responseObserver) {
    
        
        String authToken = HeaderServerInterceptor.AUTH_TOKEN_KEY.get(Context.current());
        // System.out.println("auth: " +authToken);
        try {
            ResponseEntity<?> responseEntity = keyCloakAuth.getUserId(authToken);

            
            UserIdResponse responseBuilder = UserIdResponse.newBuilder().setId(responseEntity.getBody().toString()).build();
    
            responseObserver.onNext(responseBuilder);
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        responseObserver.onCompleted();
    }

}

package com.auth.auth.controller;

import java.util.Map;



import com.auth.auth.dto.LoginDto;
import com.auth.auth.dto.LogoutDto;
import com.auth.auth.dto.RefreshTokenDto;
import com.auth.auth.dto.UserDto;
import com.auth.auth.service.EmailGrpcClient;
import com.auth.auth.service.KeyCloakAuth;
import com.auth.grpc.*;
import com.email.grpc.SendEmailResponse;

import org.springframework.grpc.server.service.GrpcService;
import org.springframework.http.ResponseEntity;


import io.grpc.stub.StreamObserver;
import io.grpc.*;


@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    
    private final KeyCloakAuth keyCloakAuth;
    
    private final EmailGrpcClient emailGrpcClient;
    public AuthServiceImpl(KeyCloakAuth keyCloakAuth,EmailGrpcClient emailGrpcClient) {
        this.keyCloakAuth = keyCloakAuth;
        this.emailGrpcClient=emailGrpcClient;
    }
    @Override
    public void register(User request, StreamObserver<RegisterResponse> responseObserver) {
    
       
        UserDto userDto = new UserDto();
        userDto.setUsername(request.getUsername());
        userDto.setEmail(request.getEmail());
        userDto.setPassword(request.getPassword());
        userDto.setFirstName(request.getFirstName());
        userDto.setLastName(request.getLastName());
        
        try {
            ResponseEntity<?> responseEntity = keyCloakAuth.registerUser(userDto);
            boolean success = responseEntity.getStatusCode().value() == 201;

        if (success) {
            System.out.println("send email");
            Boolean res= emailGrpcClient.sendEmail(userDto.getEmail(), userDto.getUsername());
            System.out.println("res :"+res);
        }
            RegisterResponse responseBuilder = RegisterResponse.newBuilder().setSuccess(responseEntity.getStatusCode().value()==201?true:false).setMessage(responseEntity.getBody() != null ? responseEntity.getBody().toString() : "No response body").setStatusCode(responseEntity.getStatusCodeValue()).build();
            responseObserver.onNext(responseBuilder);
        } catch (Exception e) {
            ResponseEntity<?> responseEntity = keyCloakAuth.registerUser(userDto);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(responseEntity.getBody().toString()).asRuntimeException());
            return;
        }
        
        responseObserver.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        LoginDto userDto = new LoginDto();
        userDto.setEmail(request.getEmail());
        userDto.setPassword(request.getPassword());
        
        
        try {
            ResponseEntity<?> responseEntity = keyCloakAuth.login(userDto);
            Object body = responseEntity.getBody();
            if (!(body instanceof Map)) {
             responseObserver.onError(Status.INTERNAL
                 .withDescription("Unexpected response from Keycloak: " + body)
                 .asRuntimeException());
             return;
            }
        Map<String, Object>  tokenData = (Map<String, Object>) body;
            System.out.println(tokenData);
            
            LoginResponse responseBuilder = LoginResponse.newBuilder().setAccessToken(tokenData.get("access_token").toString()).setExpiresIn((int)tokenData.get("expires_in")).setRefreshExpiresIn((int)tokenData.get("refresh_expires_in")).setRefreshToken(tokenData.get("refresh_token").toString()).setRole(tokenData.get("role").toString()).build();
    
            responseObserver.onNext(responseBuilder);
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        responseObserver.onCompleted();
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
    
        
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
        RefreshTokenDto userDto = new RefreshTokenDto();
        userDto.setRefreshToken(request.getRefreshToken());
        
        
        try {
            ResponseEntity<?> responseEntity = keyCloakAuth.refreshToken(userDto);
            
            Map<String, Object> tokenData = (Map<String, Object>) responseEntity.getBody();
            
            RefreshResponse responseBuilder = RefreshResponse.newBuilder().setAccessToken(tokenData.get("access_token").toString()).setExpiresIn((int)tokenData.get("expires_in")).setRefreshExpiresIn((int)tokenData.get("refresh_expires_in")).setRefreshToken(tokenData.get("refresh_token").toString()).build();
    
            responseObserver.onNext(responseBuilder);
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        responseObserver.onCompleted();
    }

    // @Override
    // public void getUserID(Empty request, StreamObserver<UserIdResponse> responseObserver) {
    
        
    //     // String authToken = HeaderServerInterceptor.AUTH_TOKEN_KEY.get(Context.current());
    //     try {
    //         ResponseEntity<?> responseEntity = keyCloakAuth.getUserId();

            
    //         UserIdResponse responseBuilder = UserIdResponse.newBuilder().setId(responseEntity.getBody().toString()).build();
    
    //         responseObserver.onNext(responseBuilder);
    //     } catch (Exception e) {
    //         responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    //         return;
    //     }
    //     responseObserver.onCompleted();
    // }

    // @Override
    // public void getUserInfo(Empty request, StreamObserver<UserInfoResponse> responseObserver) {
    
        
    //     String authToken = HeaderServerInterceptor.AUTH_TOKEN_KEY.get(Context.current());
    //     try {
    //         ResponseEntity<?> responseEntity = keyCloakAuth.getUserInfo(authToken);
    //         Map <String,Object> data=(Map<String,Object>) responseEntity.getBody();
    //         String id=(String)data.get("sub");
    //         String email=(String)data.get("email");
    //         String username=(String)data.get("username");
    //         UserInfoResponse responseBuilder = UserInfoResponse.newBuilder().setId(id).setEmail(email).setUsername(username).build();
    
    //         responseObserver.onNext(responseBuilder);
    //     } catch (Exception e) {
    //         responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    //         return;
    //     }
    //     responseObserver.onCompleted();
    // }

}

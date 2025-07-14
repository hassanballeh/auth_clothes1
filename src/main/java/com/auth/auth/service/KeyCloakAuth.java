package com.auth.auth.service;

import org.keycloak.admin.client.Keycloak;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.client.RestTemplate;

import jakarta.ws.rs.core.Response;
import com.auth.auth.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.*;

import javax.annotation.CheckForSigned;





@Service
public class KeyCloakAuth {
    private final RestTemplate restTemplate;
    private final  Keycloak keycloakAdmin;
   

    public KeyCloakAuth(Keycloak keycloakAdmin,RestTemplate restTemplate){
        this.keycloakAdmin=keycloakAdmin;
        this.restTemplate=restTemplate;

    }
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.client-id}")
    private String clientId;
    
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public ResponseEntity<?> registerUser(UserDto userDto){
        try {
                // Create user representation
                System.out.println(userDto);
                UserRepresentation user = new UserRepresentation();
                user.setEnabled(true);

                user.setUsername(userDto.getUsername());
                user.setEmail(userDto.getEmail());
                user.setEmailVerified(true);

                // Set password
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(userDto.getPassword());
                credential.setTemporary(false);
                user.setCredentials(Arrays.asList(credential));
                user.setRequiredActions(new ArrayList<>());
                
                // Create user
                Response response = keycloakAdmin.realm(realm).users().create(user);
                
                if (response.getStatus() >= 200 && response.getStatus() < 300) {
                    String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                    
                    // Update the user to ensure all required fields are set
                    UserRepresentation updatedUser = keycloakAdmin.realm(realm).users().get(userId).toRepresentation();
                    keycloakAdmin.realm(realm).users().get(userId).update(updatedUser);
                    
    
                    return ResponseEntity.status(201).body("User registered successfully");
                } else {
                    String errorMessage = response.readEntity(String.class);
                    return ResponseEntity.status(response.getStatus()).body(errorMessage);
                }
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
    }

    public ResponseEntity<?> login(LoginDto loginDto){
        try {
            String email = loginDto.getEmail();
            String password = loginDto.getPassword();
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email and password are required");
        }
            UserRepresentation user = keycloakAdmin.realm(realm)
            .users()
            .searchByEmail(email, true).get(0);
            if (user==null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
            }
            // Call Keycloak token endpoint
            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("scope", "openid"));
            params.add(new BasicNameValuePair("username", user.getUsername()));
            params.add(new BasicNameValuePair("password", password));
            
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = client.execute(post);
            
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            
            Map <String, Object> tokenData = mapper.readValue(responseBody, Map.class);
                
        //    System.out.println(tokenData);
          return ResponseEntity.status(response.getStatusLine().getStatusCode()).body(tokenData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Login error: User Not Found " );
        }
    }

    public Boolean logout(String refreshToken) {
            String logout = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("refresh_token", refreshToken);
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(
                        logout, request, String.class);
                
                // Check if the logout was successful (HTTP 2xx status code)
                return response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                // Log the error
                System.err.println("Error during logout: " + e.getMessage());
                return false;
            }
    }

    public ResponseEntity<?> refreshToken(RefreshTokenDto refreshTokenDto){
        try {    
            // Call Keycloak refresh endpoint
            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token");
            
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("grant_type", "refresh_token"));
            params.add(new BasicNameValuePair("refresh_token", refreshTokenDto.getRefreshToken()));
            
            post.setEntity(new UrlEncodedFormEntity(params));
            
            HttpResponse response = client.execute(post);
            
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            
            Map <String, Object> tokenData = mapper.readValue(responseBody, Map.class);
                return ResponseEntity.ok(tokenData);  // New tokens
            } else {
                // If refresh fails (likely expired)
                return ResponseEntity.status(401).body("Refresh token expired. Please log in again.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token refresh failed: " + e.getMessage());
        }
    }
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
    
    private Map<?,?> getUser(String authorizationHeader){
        String accessToken = extractTokenFromHeader(authorizationHeader);
            
        String introspectionUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("token", accessToken);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        
        try {
            ResponseEntity<?> response = restTemplate.postForEntity(
                    introspectionUrl, request, Map.class);
            Object body = response.getBody();
            Map<?, ?> responseBody = (Map<?, ?>) body;
            return responseBody;

        } catch (Exception e) {
            throw new RuntimeException("Error validating token: " + e.getMessage(), e);
        }
    }
    public ResponseEntity<?> getUserId(String authorizationHeader){
        String userId=(String)(getUser(authorizationHeader).get("sub"));
        if(userId!=null){
                return ResponseEntity.status(200).body(userId);
        }
            return ResponseEntity.notFound().build();
    }
    public ResponseEntity<?> getUserInfo(String authorizationHeader){
        Map<?,?> map=getUser(authorizationHeader);
        if(map==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(200).body(map);
    }
}
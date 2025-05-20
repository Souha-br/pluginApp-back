package com.example.test1.services.jira;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class JiraApiService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String adminAuth;

    public JiraApiService(
            @Value("${jira.api.base-url:http://localhost:8080}") String baseUrl,
            @Value("${jira.api.username:admin}") String adminUsername,
            @Value("${jira.api.token:your-api-token}") String adminToken) {

        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        String auth = adminUsername + ":" + adminToken;
        this.adminAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    /**
     * Récupère les informations d'un utilisateur par son nom d'utilisateur
     */
    public Map<String, Object> getUserByUsername(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", adminAuth);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = baseUrl + "/rest/api/2/user?username=" + username;
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

    /**
     * Récupère les informations d'un utilisateur par son email
     */
    public Map<String, Object> getUserByEmail(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", adminAuth);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = baseUrl + "/rest/api/2/user/search?query=" + email;
        ResponseEntity<Map[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map[].class);

        Map[] users = response.getBody();
        if (users != null && users.length > 0) {
            return users[0];
        }
        return null;
    }

    /**
     * Valide les identifiants d'un utilisateur en essayant d'accéder à l'API avec ses identifiants
     */
    public boolean validateCredentials(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String auth = username + ":" + password;
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/rest/api/2/myself";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
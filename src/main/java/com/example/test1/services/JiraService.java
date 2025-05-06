package com.example.test1.services;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
@Service
public class JiraService {

    private final String JIRA_URL = "http://localhost:8080/rest/api/2/project";
    private final String USERNAME = "Souha";
    private final String PASSWORD = "Jira@000";

    private String getAuthHeader() {
        String auth = USERNAME + ":" + PASSWORD;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    public String getAllProjects() {
        String url = JIRA_URL + "project";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getAuthHeader());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }
}

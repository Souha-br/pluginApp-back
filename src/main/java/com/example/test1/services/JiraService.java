package com.example.test1.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

/**
 * Service pour l'authentification et les opérations de base avec Jira
 */
@Service
public class JiraService {

    private static final Logger logger = LoggerFactory.getLogger(JiraService.class);

    @Value("${jira.server.url}")
    private String jiraServerUrl;

    @Value("${jira.api.username}")
    private String jiraUsername;

    @Value("${jira.api.token}")
    private String jiraApiToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public JiraService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean authenticateUser(String username, String password) {
        logger.info("🔐 Tentative d'authentification pour l'utilisateur: {}", username);

        try {
            String url = jiraServerUrl + "/rest/api/2/myself";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Utilisation de l'authentification Basic avec username:password
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.debug("📡 Appel API Jira pour authentification: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("✅ Authentification réussie pour l'utilisateur: {}", username);
                return true;
            } else {
                logger.warn("⚠️ Authentification échouée pour l'utilisateur: {} - Status: {}",
                        username, response.getStatusCode());
                return false;
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.warn("❌ Identifiants invalides pour l'utilisateur: {}", username);
            } else {
                logger.error("❌ Erreur HTTP lors de l'authentification pour {}: {} - {}",
                        username, e.getStatusCode(), e.getMessage());
            }
            return false;
        } catch (Exception e) {
            logger.error("❌ Erreur inattendue lors de l'authentification pour {}", username, e);
            return false;
        }
    }

    /**
     * Récupère les informations de l'utilisateur connecté
     */
    public JsonNode getCurrentUser(String username, String password) {
        logger.info("👤 Récupération des informations utilisateur pour: {}", username);

        try {
            String url = jiraServerUrl + "/rest/api/2/myself";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode userInfo = objectMapper.readTree(response.getBody());
                logger.info("✅ Informations utilisateur récupérées pour: {}",
                        userInfo.get("displayName").asText());
                return userInfo;
            } else {
                logger.error("❌ Erreur lors de la récupération des informations utilisateur. Status: {}",
                        response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des informations utilisateur pour {}", username, e);
            return null;
        }
    }

    /**
     * Teste la connectivité avec le serveur Jira
     */
    public boolean testJiraConnection() {
        logger.info("🔧 Test de la connectivité avec le serveur Jira");

        try {
            String url = jiraServerUrl + "/rest/api/2/serverInfo";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Utilisation des credentials configurés pour le test
            String auth = jiraUsername + ":" + jiraApiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            boolean isConnected = response.getStatusCode() == HttpStatus.OK;

            if (isConnected) {
                JsonNode serverInfo = objectMapper.readTree(response.getBody());
                logger.info("✅ Connexion Jira réussie - Version: {}, URL: {}",
                        serverInfo.get("version").asText(),
                        serverInfo.get("baseUrl").asText());
            } else {
                logger.error("❌ Échec de la connexion Jira - Status: {}", response.getStatusCode());
            }

            return isConnected;

        } catch (Exception e) {
            logger.error("❌ Erreur lors du test de connexion Jira", e);
            return false;
        }
    }

    /**
     * Récupère les informations du serveur Jira
     */
    public JsonNode getServerInfo() {
        logger.info("ℹ️ Récupération des informations du serveur Jira");

        try {
            String url = jiraServerUrl + "/rest/api/2/serverInfo";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String auth = jiraUsername + ":" + jiraApiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode serverInfo = objectMapper.readTree(response.getBody());
                logger.info("✅ Informations serveur récupérées: {}", serverInfo.get("serverTitle").asText());
                return serverInfo;
            } else {
                logger.error("❌ Erreur lors de la récupération des informations serveur. Status: {}",
                        response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des informations serveur", e);
            return null;
        }
    }
}
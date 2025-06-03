package com.example.test1.services.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class JiraApiService {

    private static final Logger logger = LoggerFactory.getLogger(JiraApiService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String adminAuth;

    public JiraApiService(
            @Value("${jira.api.base-url}") String baseUrl,
            @Value("${jira.api.username}") String adminUsername,
            @Value("${jira.api.token}") String adminToken) {

        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        String auth = adminUsername + ":" + adminToken;
        this.adminAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        logger.info("JiraApiService initialisé avec baseUrl: {}", baseUrl);
        logger.info("Admin username configuré: {}", adminUsername);
    }

    /**
     * Valide les identifiants d'un utilisateur en essayant d'accéder à l'API avec ses identifiants
     * @param username Nom d'utilisateur
     * @param password Mot de passe
     * @return true si les identifiants sont valides, false sinon
     */
    public boolean validateCredentials(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String auth = username + ":" + password;
            String encodedAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", encodedAuth);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/rest/api/2/myself";
            logger.info("Tentative de validation des identifiants pour l'utilisateur: {} avec URL: {}", username, url);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                logger.info("Validation réussie pour l'utilisateur: {}, code de statut: {}", username, response.getStatusCode());
                return response.getStatusCode().is2xxSuccessful();
            } catch (HttpClientErrorException e) {
                logger.error("Erreur lors de la validation des identifiants: {} - {}", e.getStatusCode(), e.getMessage());
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception lors de la validation des identifiants: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Récupère les informations d'un utilisateur par son nom d'utilisateur
     * @param username Nom d'utilisateur
     * @return Informations de l'utilisateur
     */
    public Map<String, Object> getUserByUsername(String username) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", adminAuth);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/rest/api/2/user?username=" + username;
            logger.info("Récupération des informations pour l'utilisateur: {} avec URL: {}", username, url);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("Informations récupérées pour l'utilisateur: {}", username);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Erreur lors de la récupération des informations de l'utilisateur: {} - {}", e.getStatusCode(), e.getMessage());
            return Collections.emptyMap();
        } catch (Exception e) {
            logger.error("Exception lors de la récupération des informations de l'utilisateur: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Récupère les informations d'un utilisateur par son email
     * @param email Email de l'utilisateur
     * @return Informations de l'utilisateur
     */
    public Map<String, Object> getUserByEmail(String email) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", adminAuth);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/rest/api/2/user/search?query=" + email;
            logger.info("Recherche d'utilisateur par email: {} avec URL: {}", email, url);

            ResponseEntity<Map[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map[].class);

            Map[] users = response.getBody();
            if (users != null && users.length > 0) {
                logger.info("Utilisateur trouvé par email: {}", email);
                return users[0];
            } else {
                logger.warn("Aucun utilisateur trouvé pour l'email: {}", email);
                return Collections.emptyMap();
            }
        } catch (HttpClientErrorException e) {
            logger.error("Erreur lors de la recherche d'utilisateur par email: {} - {}", e.getStatusCode(), e.getMessage());
            return Collections.emptyMap();
        } catch (Exception e) {
            logger.error("Exception lors de la recherche d'utilisateur par email: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Récupère tous les projets accessibles à l'utilisateur
     * @param userAuth L'en-tête d'authentification de l'utilisateur
     * @return Liste des projets
     */
    public List<Map<String, Object>> getAllProjects(String userAuth) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", userAuth);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/rest/api/2/project";

            // Logs de diagnostic détaillés
            logger.info("=== DIAGNOSTIC RÉCUPÉRATION PROJETS ===");
            logger.info("URL appelée: {}", url);
            logger.info("En-tête Authorization reçu: {}", userAuth);
            logger.info("Base URL configurée: {}", baseUrl);
            logger.info("Tentative de récupération des projets...");

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            logger.info("✅ Projets récupérés avec succès!");
            logger.info("Code de statut: {}", response.getStatusCode());
            logger.info("Nombre de projets: {}", response.getBody() != null ? response.getBody().size() : 0);

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (HttpClientErrorException e) {
            logger.error("=== ERREUR HTTP LORS DE LA RÉCUPÉRATION DES PROJETS ===");
            logger.error("Code de statut: {}", e.getStatusCode());
            logger.error("Message d'erreur: {}", e.getMessage());
            logger.error("Corps de la réponse: {}", e.getResponseBodyAsString());
            logger.error("En-têtes de la réponse: {}", e.getResponseHeaders());

            // Log spécifique pour l'erreur 403
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.error("❌ ERREUR 403 FORBIDDEN - L'utilisateur n'a pas les permissions pour accéder aux projets");
                logger.error("Vérifiez que l'utilisateur a les bonnes permissions dans Jira");
            }

            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("=== ERREUR GÉNÉRALE LORS DE LA RÉCUPÉRATION DES PROJETS ===");
            logger.error("Type d'exception: {}", e.getClass().getSimpleName());
            logger.error("Message: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Récupère tous les projets en utilisant les identifiants admin (pour diagnostic)
     * @return Liste des projets
     */
    public List<Map<String, Object>> getAllProjectsAsAdmin() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", adminAuth);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/rest/api/2/project";

            logger.info("=== TEST AVEC IDENTIFIANTS ADMIN ===");
            logger.info("URL appelée: {}", url);
            logger.info("Utilisation des identifiants admin pour le test");

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            logger.info("✅ Test admin réussi!");
            logger.info("Code de statut: {}", response.getStatusCode());
            logger.info("Nombre de projets (admin): {}", response.getBody() != null ? response.getBody().size() : 0);

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (HttpClientErrorException e) {
            logger.error("=== ERREUR AVEC IDENTIFIANTS ADMIN ===");
            logger.error("Code de statut: {}", e.getStatusCode());
            logger.error("Message: {}", e.getMessage());
            logger.error("Corps de la réponse: {}", e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.error("❌ ERREUR 401 - Identifiants admin incorrects!");
                logger.error("Vérifiez jira.api.username et jira.api.token dans application.properties");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.error("❌ ERREUR 403 - L'utilisateur admin n'a pas les permissions!");
            }

            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("=== ERREUR GÉNÉRALE AVEC IDENTIFIANTS ADMIN ===");
            logger.error("Message: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Récupère les détails d'un projet spécifique
     * @param projectIdOrKey L'ID ou la clé du projet
     * @param userAuth L'en-tête d'authentification de l'utilisateur
     * @return Les détails du projet
     */
    public Map<String, Object> getProjectDetails(String projectIdOrKey, String userAuth) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", userAuth);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/rest/api/2/project/" + projectIdOrKey;
            logger.info("Récupération des détails du projet {} avec URL: {}", projectIdOrKey, url);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("Détails du projet récupérés avec succès pour: {}", projectIdOrKey);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Erreur lors de la récupération des détails du projet: {} - {}", e.getStatusCode(), e.getMessage());
            logger.error("Corps de la réponse: {}", e.getResponseBodyAsString());
            return Collections.emptyMap();
        } catch (Exception e) {
            logger.error("Exception lors de la récupération des détails du projet: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Teste la connectivité avec l'API Jira
     * @return true si la connexion fonctionne, false sinon
     */
    public boolean testJiraConnection() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", adminAuth);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/rest/api/2/myself";
            logger.info("=== TEST DE CONNECTIVITÉ JIRA ===");
            logger.info("URL de test: {}", url);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            logger.info("✅ Connexion à Jira réussie!");
            logger.info("Code de statut: {}", response.getStatusCode());
            logger.info("Utilisateur connecté: {}", response.getBody());

            return true;
        } catch (Exception e) {
            logger.error("❌ Échec de la connexion à Jira: {}", e.getMessage());
            return false;
        }
    }
}
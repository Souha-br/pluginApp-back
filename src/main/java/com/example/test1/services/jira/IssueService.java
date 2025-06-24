package com.example.test1.services.jira;

import com.example.test1.entities.jira.Issue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour gérer les tickets (issues) Jira
 */
@Service
public class IssueService {

    private static final Logger logger = LoggerFactory.getLogger(IssueService.class);

    @Value("${jira.server.url}")
    private String jiraServerUrl;

    @Value("${jira.api.username}")
    private String jiraUsername;

    @Value("${jira.api.token}")
    private String jiraApiToken;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Récupère tous les tickets accessibles à l'utilisateur
     */
    public List<Issue> getAllIssues() {
        return getAllIssues(0, 50); // Par défaut : 50 premiers tickets
    }

    /**
     * Récupère les tickets avec pagination
     */
    public List<Issue> getAllIssues(int startAt, int maxResults) {
        logger.info("🎫 Récupération des tickets Jira (startAt: {}, maxResults: {})", startAt, maxResults);

        try {
            String url = jiraServerUrl + "/rest/api/2/search?startAt=" + startAt + "&maxResults=" + maxResults;
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.debug("📡 Appel API Jira: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                JsonNode issuesArray = jsonResponse.get("issues");
                List<Issue> issues = new ArrayList<>();

                if (issuesArray != null && issuesArray.isArray()) {
                    for (JsonNode jsonIssue : issuesArray) {
                        Issue issue = mapJsonToIssue(jsonIssue);
                        issues.add(issue);
                    }
                }

                logger.info("✅ {} tickets récupérés avec succès", issues.size());
                return issues;
            } else {
                logger.error("❌ Erreur lors de la récupération des tickets. Status: {}", response.getStatusCode());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des tickets", e);
            throw new RuntimeException("Erreur lors de la récupération des tickets", e);
        }
    }

    /**
     * Récupère les détails d'un ticket spécifique
     */
    public Issue getIssueDetails(String issueKey) {
        logger.info("🔍 Récupération des détails du ticket: {}", issueKey);

        try {
            String url = jiraServerUrl + "/rest/api/2/issue/" + issueKey;
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonIssue = objectMapper.readTree(response.getBody());
                Issue issue = mapJsonToIssue(jsonIssue);

                logger.info("✅ Détails du ticket '{}' récupérés avec succès", issue.getSummary());
                return issue;
            } else {
                throw new RuntimeException("Ticket non trouvé: " + issueKey);
            }

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération du ticket {}", issueKey, e);
            throw new RuntimeException("Erreur lors de la récupération du ticket: " + issueKey, e);
        }
    }

    /**
     * Récupère les tickets d'un projet spécifique
     */
    public List<Issue> getIssuesByProject(String projectKey) {
        logger.info("📂 Récupération des tickets du projet: {}", projectKey);

        try {
            String jql = "project = " + projectKey + " ORDER BY created DESC";
            return searchIssuesByJQL(jql);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des tickets du projet {}", projectKey, e);
            throw new RuntimeException("Erreur lors de la récupération des tickets du projet: " + projectKey, e);
        }
    }

    /**
     * Récupère les tickets assignés à un utilisateur
     */
    public List<Issue> getIssuesByAssignee(String assigneeUsername) {
        logger.info("👤 Récupération des tickets assignés à: {}", assigneeUsername);

        try {
            String jql = "assignee = " + assigneeUsername + " ORDER BY updated DESC";
            return searchIssuesByJQL(jql);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des tickets assignés à {}", assigneeUsername, e);
            throw new RuntimeException("Erreur lors de la récupération des tickets assignés", e);
        }
    }

    /**
     * Récupère les tickets par statut
     */
    public List<Issue> getIssuesByStatus(String status) {
        logger.info("📊 Récupération des tickets avec le statut: {}", status);

        try {
            String jql = "status = \"" + status + "\" ORDER BY updated DESC";
            return searchIssuesByJQL(jql);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des tickets par statut {}", status, e);
            throw new RuntimeException("Erreur lors de la récupération des tickets par statut", e);
        }
    }

    /**
     * Recherche des tickets par JQL (Jira Query Language)
     */
    public List<Issue> searchIssuesByJQL(String jql) {
        logger.info("🔍 Recherche de tickets avec JQL: {}", jql);

        try {
            String encodedJql = java.net.URLEncoder.encode(jql, "UTF-8");
            String url = jiraServerUrl + "/rest/api/2/search?jql=" + encodedJql + "&maxResults=100";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                JsonNode issuesArray = jsonResponse.get("issues");
                List<Issue> issues = new ArrayList<>();

                if (issuesArray != null && issuesArray.isArray()) {
                    for (JsonNode jsonIssue : issuesArray) {
                        Issue issue = mapJsonToIssue(jsonIssue);
                        issues.add(issue);
                    }
                }

                logger.info("✅ {} tickets trouvés pour la requête JQL", issues.size());
                return issues;
            } else {
                logger.error("❌ Erreur lors de la recherche JQL. Status: {}", response.getStatusCode());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche JQL", e);
            throw new RuntimeException("Erreur lors de la recherche JQL", e);
        }
    }

    /**
     * Recherche simple de tickets par texte
     */
    public List<Issue> searchIssues(String searchTerm) {
        logger.info("🔍 Recherche de tickets avec le terme: '{}'", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllIssues();
        }

        try {
            String jql = "text ~ \"" + searchTerm + "\" ORDER BY updated DESC";
            return searchIssuesByJQL(jql);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche de tickets", e);
            throw new RuntimeException("Erreur lors de la recherche de tickets", e);
        }
    }

    /**
     * Mappe un JsonNode Jira vers notre entité Issue simplifiée
     */
    private Issue mapJsonToIssue(JsonNode jsonIssue) {
        Issue issue = new Issue();

        // Propriétés de base
        issue.setId(getJsonString(jsonIssue, "id"));
        issue.setKey(getJsonString(jsonIssue, "key"));

        // Champs (fields)
        JsonNode fields = jsonIssue.get("fields");
        if (fields != null) {
            // Summary et Description
            issue.setSummary(getJsonString(fields, "summary"));
            issue.setDescription(getJsonString(fields, "description"));

            // Status
            JsonNode status = fields.get("status");
            if (status != null) {
                issue.setStatus(getJsonString(status, "name"));
            }

            // Priority
            JsonNode priority = fields.get("priority");
            if (priority != null) {
                issue.setPriority(getJsonString(priority, "name"));
            }

            // Issue Type
            JsonNode issueType = fields.get("issuetype");
            if (issueType != null) {
                issue.setIssueType(getJsonString(issueType, "name"));
            }

            // Assignee
            JsonNode assignee = fields.get("assignee");
            if (assignee != null && !assignee.isNull()) {
                issue.setAssignee(getJsonString(assignee, "displayName"));
            }

            // Reporter
            JsonNode reporter = fields.get("reporter");
            if (reporter != null) {
                issue.setReporter(getJsonString(reporter, "displayName"));
            }

            // Project
            JsonNode project = fields.get("project");
            if (project != null) {
                issue.setProjectKey(getJsonString(project, "key"));
                issue.setProjectName(getJsonString(project, "name"));
            }

            // Dates
            issue.setCreated(getJsonString(fields, "created"));
            issue.setUpdated(getJsonString(fields, "updated"));

            // Resolution
            JsonNode resolution = fields.get("resolution");
            if (resolution != null && !resolution.isNull()) {
                issue.setResolution(getJsonString(resolution, "name"));
            }
        }

        return issue;
    }

    /**
     * Utilitaire pour extraire une chaîne d'un JsonNode
     */
    private String getJsonString(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    /**
     * Crée les en-têtes d'authentification pour les appels API Jira
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = jiraUsername + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Accept", "application/json");

        return headers;
    }

    /**
     * Valide la configuration Jira pour les issues
     */
    public boolean validateJiraConfiguration() {
        try {
            String url = jiraServerUrl + "/rest/api/2/search?maxResults=1";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la validation de la configuration Jira pour les issues", e);
            return false;
        }
    }
}
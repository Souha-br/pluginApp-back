package com.example.test1.controllers;

import com.example.test1.entities.jira.Project;
import com.example.test1.entities.jira.Issue;
import com.example.test1.services.jira.ProjectService;
import com.example.test1.services.jira.IssueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class JiraController {

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private IssueService issueService;

    // ========== ENDPOINTS PROJETS (existants) ==========

    @GetMapping("/jira-projects")
    public String getJiraProjects() {
        return projectService.getAllProjects().toString();
    }

    @GetMapping("/projects")
    public ResponseEntity<?> getAllProjects() {
        logger.info("📋 Demande de récupération de tous les projets");

        try {
            List<Project> projects = projectService.getAllProjects();

            List<Map<String, Object>> projectMaps = projects.stream()
                    .map(this::convertProjectToMap)
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Projets récupérés avec succès");
            response.put("count", projectMaps.size());
            response.put("projects", projectMaps);

            logger.info("✅ {} projets retournés avec succès", projectMaps.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des projets", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération des projets");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/projects-admin")
    public ResponseEntity<List<Map<String, Object>>> getAllProjectsAsAdmin() {
        List<Project> projects = projectService.getAllProjects();

        List<Map<String, Object>> projectMaps = projects.stream()
                .map(this::convertProjectToMap)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(projectMaps);
    }

    @GetMapping("/projects/health")
    public ResponseEntity<?> projectsHealthCheck() {
        logger.info("🏥 Vérification de la santé de la connexion Jira pour les projets");

        try {
            boolean isHealthy = projectService.validateJiraConfiguration();

            Map<String, Object> response = new HashMap<>();
            response.put("success", isHealthy);
            response.put("message", isHealthy ? "Configuration Jira valide pour les projets" : "Configuration Jira invalide");
            response.put("timestamp", System.currentTimeMillis());

            HttpStatus status = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la vérification de santé des projets", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la vérification de santé");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/issues")
    public ResponseEntity<?> getAllIssues(@RequestParam(defaultValue = "0") int startAt,
                                          @RequestParam(defaultValue = "50") int maxResults) {
        logger.info("🎫 Demande de récupération de tous les tickets (startAt: {}, maxResults: {})", startAt, maxResults);

        try {
            List<Issue> issues = issueService.getAllIssues(startAt, maxResults);

            List<Map<String, Object>> issueMaps = issues.stream()
                    .map(this::convertIssueToMap)
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tickets récupérés avec succès");
            response.put("count", issueMaps.size());
            response.put("startAt", startAt);
            response.put("maxResults", maxResults);
            response.put("issues", issueMaps);

            logger.info("✅ {} tickets retournés avec succès", issueMaps.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des tickets", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération des tickets");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/issues/{issueKey}")
    public ResponseEntity<?> getIssueDetails(@PathVariable String issueKey) {
        logger.info("🔍 Demande de récupération des détails du ticket: {}", issueKey);

        try {
            Issue issue = issueService.getIssueDetails(issueKey);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Détails du ticket récupérés avec succès");
            response.put("issue", convertIssueToMap(issue));

            logger.info("✅ Détails du ticket '{}' retournés avec succès", issue.getSummary());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération du ticket {}: {}", issueKey, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("issueKey", issueKey);

            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            if (e.getMessage().contains("non trouvé")) {
                status = HttpStatus.NOT_FOUND;
            }

            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @GetMapping("/issues/project/{projectKey}")
    public ResponseEntity<?> getIssuesByProject(@PathVariable String projectKey) {
        logger.info("📂 Demande de récupération des tickets du projet: {}", projectKey);

        try {
            List<Issue> issues = issueService.getIssuesByProject(projectKey);

            List<Map<String, Object>> issueMaps = issues.stream()
                    .map(this::convertIssueToMap)
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tickets du projet récupérés avec succès");
            response.put("projectKey", projectKey);
            response.put("count", issueMaps.size());
            response.put("issues", issueMaps);

            logger.info("✅ {} tickets retournés pour le projet '{}'", issueMaps.size(), projectKey);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des tickets du projet {}", projectKey, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération des tickets du projet");
            errorResponse.put("projectKey", projectKey);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/issues/assignee/{username}")
    public ResponseEntity<?> getIssuesByAssignee(@PathVariable String username) {
        logger.info("👤 Demande de récupération des tickets assignés à: {}", username);

        try {
            List<Issue> issues = issueService.getIssuesByAssignee(username);

            List<Map<String, Object>> issueMaps = issues.stream()
                    .map(this::convertIssueToMap)
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tickets assignés récupérés avec succès");
            response.put("assignee", username);
            response.put("count", issueMaps.size());
            response.put("issues", issueMaps);

            logger.info("✅ {} tickets retournés assignés à '{}'", issueMaps.size(), username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des tickets assignés à {}", username, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération des tickets assignés");
            errorResponse.put("assignee", username);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/issues/status/{status}")
    public ResponseEntity<?> getIssuesByStatus(@PathVariable String status) {
        logger.info("📊 Demande de récupération des tickets avec le statut: {}", status);

        try {
            List<Issue> issues = issueService.getIssuesByStatus(status);

            List<Map<String, Object>> issueMaps = issues.stream()
                    .map(this::convertIssueToMap)
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tickets par statut récupérés avec succès");
            response.put("status", status);
            response.put("count", issueMaps.size());
            response.put("issues", issueMaps);

            logger.info("✅ {} tickets retournés avec le statut '{}'", issueMaps.size(), status);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des tickets par statut {}", status, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération des tickets par statut");
            errorResponse.put("status", status);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/issues/search")
    public ResponseEntity<?> searchIssues(@RequestParam(value = "q", required = false) String searchTerm) {
        logger.info("🔍 Recherche de tickets avec le terme: '{}'", searchTerm);

        try {
            List<Issue> issues = issueService.searchIssues(searchTerm);

            List<Map<String, Object>> issueMaps = issues.stream()
                    .map(this::convertIssueToMap)
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recherche de tickets effectuée avec succès");
            response.put("searchTerm", searchTerm);
            response.put("count", issueMaps.size());
            response.put("issues", issueMaps);

            logger.info("✅ {} tickets trouvés pour la recherche '{}'", issueMaps.size(), searchTerm);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche de tickets", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la recherche de tickets");
            errorResponse.put("searchTerm", searchTerm);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/issues/jql")
    public ResponseEntity<?> searchIssuesByJQL(@RequestParam(value = "query") String jqlQuery) {
        logger.info("🔍 Recherche JQL de tickets: {}", jqlQuery);

        try {
            List<Issue> issues = issueService.searchIssuesByJQL(jqlQuery);

            List<Map<String, Object>> issueMaps = issues.stream()
                    .map(this::convertIssueToMap)
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recherche JQL effectuée avec succès");
            response.put("jqlQuery", jqlQuery);
            response.put("count", issueMaps.size());
            response.put("issues", issueMaps);

            logger.info("✅ {} tickets trouvés pour la requête JQL", issueMaps.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche JQL", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la recherche JQL");
            errorResponse.put("jqlQuery", jqlQuery);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/issues/health")
    public ResponseEntity<?> issuesHealthCheck() {
        logger.info("🏥 Vérification de la santé de la connexion Jira pour les tickets");

        try {
            boolean isHealthy = issueService.validateJiraConfiguration();

            Map<String, Object> response = new HashMap<>();
            response.put("success", isHealthy);
            response.put("message", isHealthy ? "Configuration Jira valide pour les tickets" : "Configuration Jira invalide");
            response.put("timestamp", System.currentTimeMillis());

            HttpStatus status = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la vérification de santé des tickets", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la vérification de santé");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    private Map<String, Object> convertProjectToMap(Project project) {
        Map<String, Object> projectMap = new HashMap<>();
        projectMap.put("id", project.getId());
        projectMap.put("key", project.getKey());
        projectMap.put("name", project.getName());
        projectMap.put("description", project.getDescription());
        projectMap.put("url", project.getUrl());
        projectMap.put("avatarUrl", project.getAvatarUrl());
        projectMap.put("categoryName", project.getCategoryName());
        projectMap.put("leadName", project.getLeadName());
        return projectMap;
    }
    private Map<String, Object> convertIssueToMap(Issue issue) {
        Map<String, Object> issueMap = new HashMap<>();
        issueMap.put("id", issue.getId());
        issueMap.put("key", issue.getKey());
        issueMap.put("summary", issue.getSummary());
        issueMap.put("description", issue.getDescription());
        issueMap.put("status", issue.getStatus());
        issueMap.put("priority", issue.getPriority());
        issueMap.put("issueType", issue.getIssueType());
        issueMap.put("assignee", issue.getAssignee());
        issueMap.put("reporter", issue.getReporter());
        issueMap.put("projectKey", issue.getProjectKey());
        issueMap.put("projectName", issue.getProjectName());
        issueMap.put("created", issue.getCreated());
        issueMap.put("updated", issue.getUpdated());
        issueMap.put("resolution", issue.getResolution());
        return issueMap;
    }
}
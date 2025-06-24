package com.example.test1.services.jira;

import com.example.test1.entities.jira.Project;
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


@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

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

    public List<Project> getAllProjects() {
        logger.info("🔍 Récupération de tous les projets Jira pour l'utilisateur: {}", jiraUsername);

        try {
            String url = jiraServerUrl + "/rest/api/2/project";
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
                JsonNode jsonArray = objectMapper.readTree(response.getBody());
                List<Project> projects = new ArrayList<>();

                for (JsonNode jsonProject : jsonArray) {
                    Project project = mapJsonToProject(jsonProject);
                    projects.add(project);
                }

                logger.info("✅ {} projets récupérés avec succès", projects.size());
                return projects;
            } else {
                logger.error("❌ Erreur lors de la récupération des projets. Status: {}", response.getStatusCode());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des projets", e);
            throw new RuntimeException("Erreur lors de la récupération des projets", e);
        }
    }

    public Project getProjectDetails(String projectKey) {
        logger.info("🔍 Récupération des détails du projet: {}", projectKey);

        try {
            String url = jiraServerUrl + "/rest/api/2/project/" + projectKey;
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonProject = objectMapper.readTree(response.getBody());
                Project project = mapJsonToProject(jsonProject);

                logger.info("✅ Détails du projet '{}' récupérés avec succès", project.getName());
                return project;
            } else {
                throw new RuntimeException("Projet non trouvé: " + projectKey);
            }

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération du projet {}", projectKey, e);
            throw new RuntimeException("Erreur lors de la récupération du projet: " + projectKey, e);
        }
    }

    public List<Project> searchProjects(String searchTerm) {
        logger.info("🔍 Recherche de projets avec le terme: '{}'", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProjects();
        }

        List<Project> allProjects = getAllProjects();
        String searchTermLower = searchTerm.toLowerCase().trim();

        List<Project> filteredProjects = allProjects.stream()
                .filter(project ->
                        (project.getName() != null && project.getName().toLowerCase().contains(searchTermLower)) ||
                                (project.getKey() != null && project.getKey().toLowerCase().contains(searchTermLower)) ||
                                (project.getDescription() != null && project.getDescription().toLowerCase().contains(searchTermLower))
                )
                .collect(Collectors.toList());

        logger.info("✅ {} projets trouvés pour la recherche '{}'", filteredProjects.size(), searchTerm);
        return filteredProjects;
    }


    public boolean validateJiraConfiguration() {
        try {
            String url = jiraServerUrl + "/rest/api/2/serverInfo";
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
            logger.error("❌ Erreur lors de la validation de la configuration Jira", e);
            return false;
        }
    }


    private Project mapJsonToProject(JsonNode jsonProject) {
        Project project = new Project();

        project.setId(getJsonString(jsonProject, "id"));
        project.setKey(getJsonString(jsonProject, "key"));
        project.setName(getJsonString(jsonProject, "name"));
        project.setDescription(getJsonString(jsonProject, "description"));
        project.setUrl(getJsonString(jsonProject, "self"));

        // Avatar URL
        JsonNode avatarUrls = jsonProject.get("avatarUrls");
        if (avatarUrls != null && avatarUrls.has("48x48")) {
            project.setAvatarUrl(avatarUrls.get("48x48").asText());
        }

        // Catégorie
        JsonNode category = jsonProject.get("projectCategory");
        if (category != null && category.has("name")) {
            project.setCategoryName(category.get("name").asText());
        }

        // Chef de projet
        JsonNode lead = jsonProject.get("lead");
        if (lead != null) {
            if (lead.has("displayName")) {
                project.setLeadName(lead.get("displayName").asText());
            } else if (lead.has("name")) {
                project.setLeadName(lead.get("name").asText());
            }
        }

        return project;
    }

    private String getJsonString(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = jiraUsername + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Accept", "application/json");

        return headers;
    }
}
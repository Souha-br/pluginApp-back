package com.example.test1.controllers;

import com.example.test1.entities.jira.JiraUser;
import com.example.test1.repositories.jira.JiraUserRepository;
import com.example.test1.services.JiraService;
import com.example.test1.services.UserSyncService;
import com.example.test1.services.jira.JiraApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jira")
public class JiraController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private UserSyncService userSyncService;

    @Autowired
    private JiraUserRepository jiraUserRepository;

    @Autowired
    private JiraApiService jiraApiService;

    @GetMapping("/jira-projects")
    public String getJiraProjects() {
        return jiraService.getAllProjects();
    }

    @GetMapping("/projects")
    public ResponseEntity<List<Map<String, Object>>> getAllProjects(@RequestHeader("Authorization") String authHeader) {
        List<Map<String, Object>> projects = jiraApiService.getAllProjects(authHeader);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/projects-admin")
    public ResponseEntity<List<Map<String, Object>>> getAllProjectsAsAdmin() {
        List<Map<String, Object>> projects = jiraApiService.getAllProjectsAsAdmin();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        boolean isConnected = jiraApiService.testJiraConnection();

        Map<String, Object> result = new HashMap<>();
        result.put("connected", isConnected);
        result.put("message", isConnected ? "Connexion à Jira réussie" : "Échec de la connexion à Jira");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/projects/{projectIdOrKey}")
    public ResponseEntity<Map<String, Object>> getProjectDetails(
            @PathVariable String projectIdOrKey,
            @RequestHeader("Authorization") String authHeader) {

        Map<String, Object> projectDetails = jiraApiService.getProjectDetails(projectIdOrKey, authHeader);

        if (projectDetails.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(projectDetails);
    }

    @GetMapping("/users")
    public List<JiraUser> getAllJiraUsers() {
        return jiraUserRepository.findAll();
    }

    @GetMapping("/sync")
    public String syncUsers() {
        int imported = userSyncService.syncUsersFromJira();
        return "User sync completed. Imported " + imported + " users.";
    }
}
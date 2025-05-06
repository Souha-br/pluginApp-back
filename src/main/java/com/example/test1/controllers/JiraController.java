package com.example.test1.controllers;

import com.example.test1.entities.jira.JiraUser;
import com.example.test1.repositories.jira.JiraUserRepository;
import com.example.test1.services.JiraService;
import com.example.test1.services.UserSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jira")
public class JiraController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private UserSyncService userSyncService;

    @Autowired
    private JiraUserRepository jiraUserRepository;

    @GetMapping("/jira-projects")
    public String getJiraProjects() {
        return jiraService.getAllProjects();
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

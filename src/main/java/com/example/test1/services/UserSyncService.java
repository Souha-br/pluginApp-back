package com.example.test1.services;

import com.example.test1.entities.jira.JiraUser;
import com.example.test1.entities.primary.User;
import com.example.test1.repositories.jira.JiraUserRepository;
import com.example.test1.repositories.primary.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserSyncService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JiraUserRepository jiraUserRepository;

    @Transactional("jiraTransactionManager")
    public void syncUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            JiraUser jiraUser = new JiraUser();
            jiraUser.setUsername(user.getUsername());
            jiraUser.setActive(true);
            jiraUserRepository.save(jiraUser);
        }
    }

    @Transactional("jiraTransactionManager")
    public int syncUsersFromJira() {
        List<JiraUser> activeJiraUsers = jiraUserRepository.findByActive(true);
        int imported = 0;

        for (JiraUser jiraUser : activeJiraUsers) {
            User user = new User();
            user.setUsername(jiraUser.getUsername());
            userRepository.save(user);
            imported++;
        }

        return imported;
    }
}

package com.example.test1.services.jwt;

import com.example.test1.services.jira.JiraApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class JiraUserDetailsService implements UserDetailsService {

    private final JiraApiService jiraApiService;

    @Autowired
    public JiraUserDetailsService(JiraApiService jiraApiService) {
        this.jiraApiService = jiraApiService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Map<String, Object> userInfo = jiraApiService.getUserByUsername(username);

        if (userInfo == null) {
            userInfo = jiraApiService.getUserByEmail(username);
        }

        if (userInfo == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé: " + username);
        }

        Boolean active = (Boolean) userInfo.get("active");
        if (active != null && !active) {
            throw new UsernameNotFoundException("Utilisateur inactif: " + username);
        }

        String name = (String) userInfo.get("name");

        return new User(
                name,
                "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
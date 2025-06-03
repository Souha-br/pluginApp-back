package com.example.test1.authentication;

import com.example.test1.services.jira.JiraApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JiraAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final JiraApiService jiraApiService;

    @Autowired
    public JiraAuthenticationProvider(
            @Qualifier("jiraUserDetailsService") UserDetailsService userDetailsService,
            JiraApiService jiraApiService) {
        this.userDetailsService = userDetailsService;
        this.jiraApiService = jiraApiService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        if (jiraApiService.validateCredentials(username, password)) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(
                    userDetails, password, userDetails.getAuthorities());
        } else {
            throw new BadCredentialsException("Identifiants invalides");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
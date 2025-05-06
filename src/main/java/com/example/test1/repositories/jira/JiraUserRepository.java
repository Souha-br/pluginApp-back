package com.example.test1.repositories.jira;

import com.example.test1.entities.jira.JiraUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JiraUserRepository extends JpaRepository<JiraUser, Long> {
    List<JiraUser> findByActive(boolean active);
}

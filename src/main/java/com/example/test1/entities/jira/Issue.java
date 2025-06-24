package com.example.test1.entities.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Entité simplifiée représentant un ticket (issue) Jira
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {

    private String id;
    private String key;
    private String summary;
    private String description;

    @JsonProperty("fields")
    private IssueFields fields;

    // Propriétés simplifiées extraites des fields
    private String status;
    private String priority;
    private String issueType;
    private String assignee;
    private String reporter;
    private String projectKey;
    private String projectName;
    private String created;
    private String updated;
    private String resolution;

    // Constructeurs
    public Issue() {}

    public Issue(String id, String key, String summary) {
        this.id = id;
        this.key = key;
        this.summary = summary;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueFields getFields() {
        return fields;
    }

    public void setFields(IssueFields fields) {
        this.fields = fields;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    // Méthodes utilitaires
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public boolean isAssigned() {
        return assignee != null && !assignee.trim().isEmpty();
    }

    public boolean isResolved() {
        return resolution != null && !resolution.trim().isEmpty();
    }

    public String getDisplayName() {
        return summary != null ? summary : key;
    }

    @Override
    public String toString() {
        return "Issue{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", summary='" + summary + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", assignee='" + assignee + '\'' +
                ", projectKey='" + projectKey + '\'' +
                '}';
    }

    /**
     * Classe interne pour les champs complexes de l'issue
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueFields {
        private String summary;
        private String description;
        private Map<String, Object> status;
        private Map<String, Object> priority;
        private Map<String, Object> issuetype;
        private Map<String, Object> assignee;
        private Map<String, Object> reporter;
        private Map<String, Object> project;
        private String created;
        private String updated;
        private Map<String, Object> resolution;

        // Getters et Setters
        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> getStatus() {
            return status;
        }

        public void setStatus(Map<String, Object> status) {
            this.status = status;
        }

        public Map<String, Object> getPriority() {
            return priority;
        }

        public void setPriority(Map<String, Object> priority) {
            this.priority = priority;
        }

        public Map<String, Object> getIssuetype() {
            return issuetype;
        }

        public void setIssuetype(Map<String, Object> issuetype) {
            this.issuetype = issuetype;
        }

        public Map<String, Object> getAssignee() {
            return assignee;
        }

        public void setAssignee(Map<String, Object> assignee) {
            this.assignee = assignee;
        }

        public Map<String, Object> getReporter() {
            return reporter;
        }

        public void setReporter(Map<String, Object> reporter) {
            this.reporter = reporter;
        }

        public Map<String, Object> getProject() {
            return project;
        }

        public void setProject(Map<String, Object> project) {
            this.project = project;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getUpdated() {
            return updated;
        }

        public void setUpdated(String updated) {
            this.updated = updated;
        }

        public Map<String, Object> getResolution() {
            return resolution;
        }

        public void setResolution(Map<String, Object> resolution) {
            this.resolution = resolution;
        }
    }
}
package com.example.test1.entities.jira;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    private String id;
    private String key;
    private String name;
    private String description;
    private String url;

    @JsonProperty("avatarUrls")
    private String avatarUrl;

    @JsonProperty("projectCategory")
    private String categoryName;

    @JsonProperty("lead")
    private String leadName;

    // Constructeurs
    public Project() {}

    public Project(String id, String key, String name) {
        this.id = id;
        this.key = key;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getLeadName() {
        return leadName;
    }

    public void setLeadName(String leadName) {
        this.leadName = leadName;
    }

    // Méthodes utilitaires
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public boolean hasCategory() {
        return categoryName != null && !categoryName.trim().isEmpty();
    }

    public boolean hasLead() {
        return leadName != null && !leadName.trim().isEmpty();
    }

    public String getDisplayName() {
        return name != null ? name : key;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", leadName='" + leadName + '\'' +
                '}';
    }
}
package com.example.test1.dto;


public class JwtResponse {

    private String jwtToken;
    private String username;
    private String message;

    public JwtResponse() {}

    public JwtResponse(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public JwtResponse(String jwtToken, String username, String message) {
        this.jwtToken = jwtToken;
        this.username = username;
        this.message = message;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "JwtResponse{" +
                "jwtToken='[PROTECTED]'" +
                ", username='" + username + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

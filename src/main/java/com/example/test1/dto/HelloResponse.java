package com.example.test1.dto;

import java.util.Objects;

public class HelloResponse {
    private final String message;

    public HelloResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HelloResponse)) return false;
        HelloResponse that = (HelloResponse) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "HelloResponse{" +
                "message='" + message + '\'' +
                '}';
    }
}

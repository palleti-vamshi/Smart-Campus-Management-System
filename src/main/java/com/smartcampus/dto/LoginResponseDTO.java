package com.smartcampus.dto;

import com.smartcampus.entity.Role;

public class LoginResponseDTO {

    private String message;
    private String token;
    private Long userId;
    private String username;
    private Role role;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(
            String message,
            String token,
            Long userId,
            String username,
            Role role) {

        this.message = message;
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
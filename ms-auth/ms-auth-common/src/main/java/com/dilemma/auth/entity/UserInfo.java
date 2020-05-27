package com.dilemma.auth.entity;

public class UserInfo {
    private Long id;
    private String username;

    public Long getId() {
        return id;
    }
    public UserInfo() {
    }

    public UserInfo(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

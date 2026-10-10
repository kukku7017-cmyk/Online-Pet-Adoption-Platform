package com.petadoption.model;

import java.time.LocalDateTime;

public class Conversation {
    private Long id;
    private Long applicationId;
    private Long shelterId;
    private String shelterName;
    private Long adopterId;
    private String adopterName;
    private String petName;
    private String lastMessageSnippet;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    public Conversation() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getShelterId() {
        return shelterId;
    }

    public void setShelterId(Long shelterId) {
        this.shelterId = shelterId;
    }

    public String getShelterName() {
        return shelterName;
    }

    public void setShelterName(String shelterName) {
        this.shelterName = shelterName;
    }

    public Long getAdopterId() {
        return adopterId;
    }

    public void setAdopterId(Long adopterId) {
        this.adopterId = adopterId;
    }

    public String getAdopterName() {
        return adopterName;
    }

    public void setAdopterName(String adopterName) {
        this.adopterName = adopterName;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getLastMessageSnippet() {
        return lastMessageSnippet;
    }

    public void setLastMessageSnippet(String lastMessageSnippet) {
        this.lastMessageSnippet = lastMessageSnippet;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}

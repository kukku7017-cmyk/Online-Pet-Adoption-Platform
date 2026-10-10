package com.petadoption.model;

import java.time.LocalDateTime;

public class Pet {

    public enum PetStatus {
        PENDING_REVIEW, AVAILABLE, REJECTED, ADOPTED;

        public static PetStatus fromString(String str) {
            if (str == null) return null;
            String upper = str.trim().toUpperCase();
            if ("APPROVED".equals(upper)) return AVAILABLE;
            try {
                return PetStatus.valueOf(upper);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum Gender {
        MALE, FEMALE, UNKNOWN;

        public static Gender fromString(String str) {
            if (str == null) return UNKNOWN;
            try {
                return Gender.valueOf(str.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    public enum PetSize {
        SMALL, MEDIUM, LARGE, EXTRA_LARGE;

        public static PetSize fromString(String str) {
            if (str == null) return MEDIUM;
            try {
                return PetSize.valueOf(str.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return MEDIUM;
            }
        }
    }

    private Long id;
    private Long shelterId;
    private String shelterName;
    private String name;
    private String species;
    private String breed;
    private Integer ageMonths;
    private Gender gender = Gender.UNKNOWN;
    private PetSize size = PetSize.MEDIUM;
    private String location;
    private String description;
    private String healthStatus;
    private String vaccinationStatus;
    private Boolean spayedNeutered = false;
    private String imageUrl;
    private PetStatus status = PetStatus.PENDING_REVIEW;
    private String rejectionReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Pet() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAgeMonths() {
        return ageMonths;
    }

    public void setAgeMonths(Integer ageMonths) {
        this.ageMonths = ageMonths;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender != null ? gender : Gender.UNKNOWN;
    }

    public PetSize getSize() {
        return size;
    }

    public void setSize(PetSize size) {
        this.size = size != null ? size : PetSize.MEDIUM;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getVaccinationStatus() {
        return vaccinationStatus;
    }

    public void setVaccinationStatus(String vaccinationStatus) {
        this.vaccinationStatus = vaccinationStatus;
    }

    public Boolean getSpayedNeutered() {
        return spayedNeutered;
    }

    public void setSpayedNeutered(Boolean spayedNeutered) {
        this.spayedNeutered = spayedNeutered != null ? spayedNeutered : false;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public PetStatus getStatus() {
        return status;
    }

    public void setStatus(PetStatus status) {
        this.status = status != null ? status : PetStatus.PENDING_REVIEW;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFormattedAge() {
        if (ageMonths == null) return "Unknown";
        if (ageMonths < 12) {
            return ageMonths + (ageMonths == 1 ? " month" : " months");
        }
        int years = ageMonths / 12;
        int rem = ageMonths % 12;
        if (rem == 0) {
            return years + (years == 1 ? " year" : " years");
        }
        return years + " yr " + rem + " mo";
    }
}

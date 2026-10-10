package com.petadoption.model;

import java.time.LocalDateTime;

public class AdoptionApplication {

    public enum ApplicationStatus {
        PENDING, UNDER_REVIEW, APPROVED, REJECTED;

        public static ApplicationStatus fromString(String str) {
            if (str == null) return null;
            try {
                return ApplicationStatus.valueOf(str.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private Long id;
    private Long petId;
    private String petName;
    private String petSpecies;
    private String petImageUrl;
    private Long shelterId;

    private Long adopterId;
    private String adopterName;
    private String adopterEmail;
    private String adopterPhone;

    private String livingArrangement;
    private String homeType;
    private String petExperience;
    private Integer householdMembers = 1;
    private String adoptionReason;
    private String careCommitment;
    private Boolean agreementTerms = false;

    private ApplicationStatus status = ApplicationStatus.PENDING;
    private String reviewNotes;
    private LocalDateTime reviewedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    public AdoptionApplication() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetSpecies() {
        return petSpecies;
    }

    public void setPetSpecies(String petSpecies) {
        this.petSpecies = petSpecies;
    }

    public String getPetImageUrl() {
        return petImageUrl;
    }

    public void setPetImageUrl(String petImageUrl) {
        this.petImageUrl = petImageUrl;
    }

    public Long getShelterId() {
        return shelterId;
    }

    public void setShelterId(Long shelterId) {
        this.shelterId = shelterId;
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

    public String getAdopterEmail() {
        return adopterEmail;
    }

    public void setAdopterEmail(String adopterEmail) {
        this.adopterEmail = adopterEmail;
    }

    public String getAdopterPhone() {
        return adopterPhone;
    }

    public void setAdopterPhone(String adopterPhone) {
        this.adopterPhone = adopterPhone;
    }

    public String getLivingArrangement() {
        return livingArrangement;
    }

    public void setLivingArrangement(String livingArrangement) {
        this.livingArrangement = livingArrangement;
    }

    public String getHomeType() {
        return homeType;
    }

    public void setHomeType(String homeType) {
        this.homeType = homeType;
    }

    public String getPetExperience() {
        return petExperience;
    }

    public void setPetExperience(String petExperience) {
        this.petExperience = petExperience;
    }

    public Integer getHouseholdMembers() {
        return householdMembers;
    }

    public void setHouseholdMembers(Integer householdMembers) {
        this.householdMembers = householdMembers;
    }

    public String getAdoptionReason() {
        return adoptionReason;
    }

    public void setAdoptionReason(String adoptionReason) {
        this.adoptionReason = adoptionReason;
    }

    public String getCareCommitment() {
        return careCommitment;
    }

    public void setCareCommitment(String careCommitment) {
        this.careCommitment = careCommitment;
    }

    public Boolean getAgreementTerms() {
        return agreementTerms;
    }

    public void setAgreementTerms(Boolean agreementTerms) {
        this.agreementTerms = agreementTerms != null ? agreementTerms : false;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status != null ? status : ApplicationStatus.PENDING;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

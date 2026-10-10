package com.petadoption.model;

import java.util.Map;

public class PlatformAnalytics {
    private long totalUsers;
    private long totalShelters;
    private long totalAdopters;
    private long totalPets;
    private long availablePets;
    private long pendingPetListings;
    private long pendingApplications;
    private long approvedAdoptions;
    private Map<String, Long> petsBySpecies;
    private Map<String, Long> applicationsByStatus;

    public PlatformAnalytics() {}

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalShelters() {
        return totalShelters;
    }

    public void setTotalShelters(long totalShelters) {
        this.totalShelters = totalShelters;
    }

    public long getTotalAdopters() {
        return totalAdopters;
    }

    public void setTotalAdopters(long totalAdopters) {
        this.totalAdopters = totalAdopters;
    }

    public long getTotalPets() {
        return totalPets;
    }

    public void setTotalPets(long totalPets) {
        this.totalPets = totalPets;
    }

    public long getAvailablePets() {
        return availablePets;
    }

    public void setAvailablePets(long availablePets) {
        this.availablePets = availablePets;
    }

    public long getPendingPetListings() {
        return pendingPetListings;
    }

    public void setPendingPetListings(long pendingPetListings) {
        this.pendingPetListings = pendingPetListings;
    }

    public long getPendingApplications() {
        return pendingApplications;
    }

    public void setPendingApplications(long pendingApplications) {
        this.pendingApplications = pendingApplications;
    }

    public long getApprovedAdoptions() {
        return approvedAdoptions;
    }

    public void setApprovedAdoptions(long approvedAdoptions) {
        this.approvedAdoptions = approvedAdoptions;
    }

    public Map<String, Long> getPetsBySpecies() {
        return petsBySpecies;
    }

    public void setPetsBySpecies(Map<String, Long> petsBySpecies) {
        this.petsBySpecies = petsBySpecies;
    }

    public Map<String, Long> getApplicationsByStatus() {
        return applicationsByStatus;
    }

    public void setApplicationsByStatus(Map<String, Long> applicationsByStatus) {
        this.applicationsByStatus = applicationsByStatus;
    }
}

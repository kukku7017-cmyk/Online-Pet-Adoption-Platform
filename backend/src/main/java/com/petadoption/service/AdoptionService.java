package com.petadoption.service;

import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.User;

import java.util.List;

public interface AdoptionService {
    AdoptionApplication submitApplication(AdoptionApplication application, User adopterUser);
    void reviewApplication(Long applicationId, String statusStr, String reviewNotes, User shelterUser);
    List<AdoptionApplication> getAdopterApplications(Long adopterId);
    List<AdoptionApplication> getShelterApplications(Long shelterId);
    AdoptionApplication getApplicationById(Long applicationId, User currentUser);
}

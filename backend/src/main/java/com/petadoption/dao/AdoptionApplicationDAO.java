package com.petadoption.dao;

import com.petadoption.model.AdoptionApplication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AdoptionApplicationDAO {
    Optional<AdoptionApplication> findById(Long id);
    AdoptionApplication create(AdoptionApplication application);
    void updateStatus(Long applicationId, AdoptionApplication.ApplicationStatus status, String reviewNotes);
    void approveApplicationAndAdoptPet(Long applicationId, Long petId, String reviewNotes);
    boolean existsByPetAndAdopter(Long petId, Long adopterId);
    List<AdoptionApplication> findByAdopterId(Long adopterId);
    List<AdoptionApplication> findByShelterId(Long shelterId);
    List<AdoptionApplication> findByPetId(Long petId);
    long countTotal();
    long countByStatus(AdoptionApplication.ApplicationStatus status);
    long countByShelterAndStatus(Long shelterId, AdoptionApplication.ApplicationStatus status);
    Map<String, Long> countGroupByStatus();
}

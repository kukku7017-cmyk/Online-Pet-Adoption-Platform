package com.petadoption.dao;

import com.petadoption.model.Pet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PetDAO {
    Optional<Pet> findById(Long id);
    Pet create(Pet pet);
    void update(Pet pet);
    void updateStatus(Long petId, Pet.PetStatus status, String rejectionReason, Long reviewedBy);
    List<Pet> findByShelterId(Long shelterId);
    List<Pet> findByStatus(Pet.PetStatus status);
    List<Pet> searchPublicPets(String species, String breed, String gender, String size, String query, int offset, int limit);
    long countPublicPets(String species, String breed, String gender, String size, String query);
    long countTotal();
    long countByStatus(Pet.PetStatus status);
    Map<String, Long> countGroupBySpecies();
}

package com.petadoption.service;

import com.petadoption.model.Pet;
import com.petadoption.model.User;

import java.util.List;
import java.util.Map;

public interface PetService {
    Pet createPet(Pet pet, User shelterUser);
    Pet updatePet(Pet pet, User shelterUser);
    void moderatePet(Long petId, String statusStr, String rejectionReason, User adminUser);
    Map<String, Object> searchPublicPets(String species, String breed, String gender, String size, String query, int page, int limit);
    Pet getPetById(Long id);
    List<Pet> getShelterPets(Long shelterId);
    List<Pet> getPendingPets();
}

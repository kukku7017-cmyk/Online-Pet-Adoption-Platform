package com.petadoption.service;

import com.petadoption.dao.AuditLogDAO;
import com.petadoption.dao.AuditLogDAOImpl;
import com.petadoption.dao.PetDAO;
import com.petadoption.dao.PetDAOImpl;
import com.petadoption.exception.AuthorizationException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.AuditLog;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.util.ValidationUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetServiceImpl implements PetService {

    private final PetDAO petDAO;
    private final AuditLogDAO auditLogDAO;
    private final NotificationService notificationService;

    public PetServiceImpl() {
        this(new PetDAOImpl(), new AuditLogDAOImpl(), NotificationService.getInstance());
    }

    public PetServiceImpl(PetDAO petDAO, AuditLogDAO auditLogDAO, NotificationService notificationService) {
        this.petDAO = petDAO;
        this.auditLogDAO = auditLogDAO;
        this.notificationService = notificationService;
    }

    @Override
    public Pet createPet(Pet pet, User shelterUser) {
        if (shelterUser.getRole() != User.Role.SHELTER) {
            throw new AuthorizationException("Only registered shelters can list pets for adoption.");
        }

        validatePetInput(pet);

        pet.setShelterId(shelterUser.getId());
        pet.setStatus(Pet.PetStatus.PENDING_REVIEW);
        pet.setRejectionReason(null);

        Pet created = petDAO.create(pet);

        notificationService.submitAsyncTask(() -> {
            auditLogDAO.log(new AuditLog(
                shelterUser.getId(),
                "PET_CREATED",
                "PET",
                created.getId(),
                "Shelter listed pet: " + created.getName() + " (" + created.getSpecies() + ")",
                "127.0.0.1"
            ));
        });

        return created;
    }

    @Override
    public Pet updatePet(Pet pet, User shelterUser) {
        if (pet.getId() == null) {
            throw new ValidationException("Pet ID is required for updating.");
        }

        Pet existing = getPetById(pet.getId());

        // Strict Ownership Check (Mitigates IDOR)
        if (!existing.getShelterId().equals(shelterUser.getId()) && shelterUser.getRole() != User.Role.ADMIN) {
            throw new AuthorizationException("You do not have permission to modify another shelter's pet listing.");
        }

        validatePetInput(pet);

        existing.setName(ValidationUtil.sanitize(pet.getName()));
        existing.setSpecies(ValidationUtil.sanitize(pet.getSpecies()));
        existing.setBreed(ValidationUtil.sanitize(pet.getBreed()));
        existing.setAgeMonths(pet.getAgeMonths());
        existing.setGender(pet.getGender());
        existing.setSize(pet.getSize());
        existing.setLocation(ValidationUtil.sanitize(pet.getLocation()));
        existing.setDescription(ValidationUtil.sanitize(pet.getDescription()));
        existing.setHealthStatus(ValidationUtil.sanitize(pet.getHealthStatus()));
        existing.setVaccinationStatus(ValidationUtil.sanitize(pet.getVaccinationStatus()));
        existing.setSpayedNeutered(pet.getSpayedNeutered());
        if (pet.getImageUrl() != null && !pet.getImageUrl().isBlank()) {
            existing.setImageUrl(pet.getImageUrl().trim());
        }

        // If a previously rejected pet is edited by the shelter, re-queue it for moderation
        if (existing.getStatus() == Pet.PetStatus.REJECTED) {
            existing.setStatus(Pet.PetStatus.PENDING_REVIEW);
            existing.setRejectionReason(null);
        }

        petDAO.update(existing);

        notificationService.submitAsyncTask(() -> {
            auditLogDAO.log(new AuditLog(
                shelterUser.getId(),
                "PET_UPDATED",
                "PET",
                existing.getId(),
                "Updated listing for pet: " + existing.getName(),
                "127.0.0.1"
            ));
        });

        return existing;
    }

    @Override
    public void moderatePet(Long petId, String statusStr, String rejectionReason, User adminUser) {
        if (adminUser.getRole() != User.Role.ADMIN) {
            throw new AuthorizationException("Only platform administrators can moderate pet listings.");
        }

        Pet.PetStatus status = Pet.PetStatus.fromString(statusStr);
        if (status != Pet.PetStatus.AVAILABLE && status != Pet.PetStatus.REJECTED) {
            throw new ValidationException("Moderation status must be either AVAILABLE or REJECTED.");
        }

        if (status == Pet.PetStatus.REJECTED) {
            ValidationUtil.requireNonBlank(rejectionReason, "Rejection reason");
        }

        Pet pet = getPetById(petId);
        petDAO.updateStatus(petId, status, rejectionReason != null ? rejectionReason.trim() : null, adminUser.getId());

        notificationService.submitAsyncTask(() -> {
            auditLogDAO.log(new AuditLog(
                adminUser.getId(),
                "PET_MODERATED",
                "PET",
                petId,
                "Admin marked pet " + pet.getName() + " as " + status.name() + (rejectionReason != null ? " (" + rejectionReason + ")" : ""),
                "127.0.0.1"
            ));
        });
    }

    @Override
    public Map<String, Object> searchPublicPets(String species, String breed, String gender, String size, String query, int page, int limit) {
        if (page < 1) page = 1;
        if (limit < 1 || limit > 50) limit = 9;

        int offset = (page - 1) * limit;
        List<Pet> pets = petDAO.searchPublicPets(species, breed, gender, size, query, offset, limit);
        long totalRecords = petDAO.countPublicPets(species, breed, gender, size, query);
        int totalPages = (int) Math.ceil((double) totalRecords / limit);

        Map<String, Object> result = new HashMap<>();
        result.put("pets", pets);
        result.put("currentPage", page);
        result.put("totalPages", totalPages > 0 ? totalPages : 1);
        result.put("totalRecords", totalRecords);
        result.put("limit", limit);
        return result;
    }

    @Override
    public Pet getPetById(Long id) {
        return petDAO.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pet listing not found with ID: " + id));
    }

    @Override
    public List<Pet> getShelterPets(Long shelterId) {
        return petDAO.findByShelterId(shelterId);
    }

    @Override
    public List<Pet> getPendingPets() {
        return petDAO.findByStatus(Pet.PetStatus.PENDING_REVIEW);
    }

    private void validatePetInput(Pet pet) {
        ValidationUtil.requireNonBlank(pet.getName(), "Pet Name");
        ValidationUtil.requireNonBlank(pet.getSpecies(), "Species");
        ValidationUtil.requireNonBlank(pet.getBreed(), "Breed");
        ValidationUtil.requireNonBlank(pet.getLocation(), "Location");
        ValidationUtil.requireNonBlank(pet.getDescription(), "Description");
        if (pet.getAgeMonths() == null || pet.getAgeMonths() < 0) {
            throw new ValidationException("Age in months must be a non-negative integer.");
        }
    }
}

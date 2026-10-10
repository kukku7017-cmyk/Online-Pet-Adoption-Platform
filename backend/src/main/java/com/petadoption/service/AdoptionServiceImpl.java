package com.petadoption.service;

import com.petadoption.dao.*;
import com.petadoption.exception.AuthorizationException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.AuditLog;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.util.ValidationUtil;

import java.util.List;

public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionApplicationDAO applicationDAO;
    private final PetDAO petDAO;
    private final MessageDAO messageDAO;
    private final AuditLogDAO auditLogDAO;
    private final NotificationService notificationService;

    public AdoptionServiceImpl() {
        this(new AdoptionApplicationDAOImpl(), new PetDAOImpl(), new MessageDAOImpl(), new AuditLogDAOImpl(), NotificationService.getInstance());
    }

    public AdoptionServiceImpl(AdoptionApplicationDAO applicationDAO, PetDAO petDAO, MessageDAO messageDAO,
                               AuditLogDAO auditLogDAO, NotificationService notificationService) {
        this.applicationDAO = applicationDAO;
        this.petDAO = petDAO;
        this.messageDAO = messageDAO;
        this.auditLogDAO = auditLogDAO;
        this.notificationService = notificationService;
    }

    @Override
    public AdoptionApplication submitApplication(AdoptionApplication app, User adopterUser) {
        if (adopterUser.getRole() != User.Role.ADOPTER) {
            throw new AuthorizationException("Only registered adopters can submit adoption applications.");
        }

        if (app.getPetId() == null) {
            throw new ValidationException("Target pet ID is required.");
        }

        Pet pet = petDAO.findById(app.getPetId())
            .orElseThrow(() -> new ResourceNotFoundException("Pet not found with ID: " + app.getPetId()));

        if (pet.getStatus() != Pet.PetStatus.AVAILABLE) {
            throw new ValidationException("This pet is not currently available for adoption (Status: " + pet.getStatus() + ").");
        }

        // Check for duplicate application
        if (applicationDAO.existsByPetAndAdopter(app.getPetId(), adopterUser.getId())) {
            throw new ValidationException("You have already submitted an active application for " + pet.getName() + ".");
        }

        // Server-side form validation
        ValidationUtil.requireNonBlank(app.getLivingArrangement(), "Living Arrangement");
        ValidationUtil.requireNonBlank(app.getHomeType(), "Home Type");
        ValidationUtil.requireNonBlank(app.getPetExperience(), "Pet Experience");
        ValidationUtil.requireNonBlank(app.getAdoptionReason(), "Reason for Adoption");
        ValidationUtil.requireNonBlank(app.getCareCommitment(), "Care Commitment");

        if (app.getAgreementTerms() == null || !app.getAgreementTerms()) {
            throw new ValidationException("You must agree to the adoption terms and ethical care agreement.");
        }

        app.setAdopterId(adopterUser.getId());
        app.setStatus(AdoptionApplication.ApplicationStatus.PENDING);
        app.setLivingArrangement(ValidationUtil.sanitize(app.getLivingArrangement()));
        app.setHomeType(ValidationUtil.sanitize(app.getHomeType()));
        app.setPetExperience(ValidationUtil.sanitize(app.getPetExperience()));
        app.setAdoptionReason(ValidationUtil.sanitize(app.getAdoptionReason()));
        app.setCareCommitment(ValidationUtil.sanitize(app.getCareCommitment()));

        AdoptionApplication created = applicationDAO.create(app);

        // Auto-create linked conversation for shelter-adopter communication
        messageDAO.findOrCreateConversation(created.getId(), pet.getShelterId(), adopterUser.getId());

        // Asynchronous non-blocking notification and audit logging
        notificationService.submitAsyncTask(() -> {
            auditLogDAO.log(new AuditLog(
                adopterUser.getId(),
                "APPLICATION_SUBMITTED",
                "ADOPTION_APPLICATION",
                created.getId(),
                "Adopter " + adopterUser.getEmail() + " applied for pet " + pet.getName(),
                "127.0.0.1"
            ));
        });

        return created;
    }

    @Override
    public void reviewApplication(Long applicationId, String statusStr, String reviewNotes, User shelterUser) {
        AdoptionApplication app = applicationDAO.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        // Ownership verification: Only the shelter managing the pet (or an admin) can review
        if (app.getShelterId() != null && !app.getShelterId().equals(shelterUser.getId()) && shelterUser.getRole() != User.Role.ADMIN) {
            throw new AuthorizationException("You do not have permission to review applications for other shelters.");
        }

        AdoptionApplication.ApplicationStatus newStatus = AdoptionApplication.ApplicationStatus.fromString(statusStr);
        if (newStatus == null) {
            throw new ValidationException("Invalid application status: " + statusStr);
        }

        String notes = reviewNotes != null ? ValidationUtil.sanitize(reviewNotes) : null;

        if (newStatus == AdoptionApplication.ApplicationStatus.APPROVED) {
            // Trigger ACID multi-step atomic transaction
            applicationDAO.approveApplicationAndAdoptPet(app.getId(), app.getPetId(), notes);
        } else {
            applicationDAO.updateStatus(applicationId, newStatus, notes);
        }

        notificationService.submitAsyncTask(() -> {
            auditLogDAO.log(new AuditLog(
                shelterUser.getId(),
                "APPLICATION_REVIEWED",
                "ADOPTION_APPLICATION",
                applicationId,
                "Updated application status to " + newStatus.name() + " for pet: " + app.getPetName(),
                "127.0.0.1"
            ));
        });
    }

    @Override
    public List<AdoptionApplication> getAdopterApplications(Long adopterId) {
        return applicationDAO.findByAdopterId(adopterId);
    }

    @Override
    public List<AdoptionApplication> getShelterApplications(Long shelterId) {
        return applicationDAO.findByShelterId(shelterId);
    }

    @Override
    public AdoptionApplication getApplicationById(Long applicationId, User currentUser) {
        AdoptionApplication app = applicationDAO.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        // Strict authorization check
        boolean isApplicant = app.getAdopterId() != null && app.getAdopterId().equals(currentUser.getId());
        boolean isShelterOwner = app.getShelterId() != null && app.getShelterId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;

        if (!isApplicant && !isShelterOwner && !isAdmin) {
            throw new AuthorizationException("Access denied: You do not have permission to view this application.");
        }

        return app;
    }
}

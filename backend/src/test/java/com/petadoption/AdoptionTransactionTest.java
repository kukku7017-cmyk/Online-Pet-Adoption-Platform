package com.petadoption;

import com.petadoption.config.DatabaseConfig;
import com.petadoption.dao.*;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.service.AdoptionService;
import com.petadoption.service.AdoptionServiceImpl;
import com.petadoption.service.NotificationService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdoptionTransactionTest {

    private static AdoptionService adoptionService;
    private static AdoptionApplicationDAO appDAO;
    private static PetDAO petDAO;
    private static User adopterSarah;
    private static User adopterDavid;
    private static User shelterUser;

    @BeforeAll
    public static void setUp() {
        System.setProperty("app.environment", "h2");
        DatabaseConfig.initialize();

        appDAO = new AdoptionApplicationDAOImpl();
        petDAO = new PetDAOImpl();
        MessageDAO messageDAO = new MessageDAOImpl();
        AuditLogDAO auditLogDAO = new AuditLogDAOImpl();
        adoptionService = new AdoptionServiceImpl(appDAO, petDAO, messageDAO, auditLogDAO, NotificationService.getInstance());

        UserDAO userDAO = new UserDAOImpl();
        shelterUser = userDAO.findById(2L).orElseThrow();
        adopterSarah = userDAO.findById(4L).orElseThrow();
        adopterDavid = userDAO.findById(5L).orElseThrow();
    }

    @Test
    @Order(1)
    @DisplayName("APP-01: Adopter successfully submits application for an available pet")
    public void testSubmitApplication() {
        // Create a new available pet for this test
        Pet pet = new Pet();
        pet.setShelterId(shelterUser.getId());
        pet.setName("Cooper");
        pet.setSpecies("Dog");
        pet.setBreed("Labrador");
        pet.setAgeMonths(18);
        pet.setLocation("Austin, TX");
        pet.setDescription("Gentle family dog");
        pet.setStatus(Pet.PetStatus.AVAILABLE);
        Pet savedPet = petDAO.create(pet);

        AdoptionApplication app = new AdoptionApplication();
        app.setPetId(savedPet.getId());
        app.setHomeType("Single Family Home");
        app.setLivingArrangement("Fenced suburban yard");
        app.setPetExperience("Grew up with labradors");
        app.setHouseholdMembers(4);
        app.setAdoptionReason("Loving companion for the family");
        app.setCareCommitment("2 hours daily walk and annual vet coverage");
        app.setAgreementTerms(true);

        AdoptionApplication submitted = adoptionService.submitApplication(app, adopterSarah);
        assertNotNull(submitted.getId());
        assertEquals(AdoptionApplication.ApplicationStatus.PENDING, submitted.getStatus());
        assertEquals(adopterSarah.getId(), submitted.getAdopterId());
    }

    @Test
    @Order(2)
    @DisplayName("APP-02: Prevent duplicate application for the same pet by same adopter")
    public void testPreventDuplicateApplication() {
        // Pet 2 (Milo) already has an application from Adopter 5 (David) in seed data
        AdoptionApplication dupApp = new AdoptionApplication();
        dupApp.setPetId(2L);
        dupApp.setHomeType("Apartment");
        dupApp.setLivingArrangement("Lease approved");
        dupApp.setPetExperience("Prior pets");
        dupApp.setHouseholdMembers(2);
        dupApp.setAdoptionReason("Duplicate test");
        dupApp.setCareCommitment("Daily care");
        dupApp.setAgreementTerms(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            adoptionService.submitApplication(dupApp, adopterDavid);
        });
        assertTrue(ex.getMessage().contains("already submitted an active application"));
    }

    @Test
    @Order(3)
    @DisplayName("APP-03: Atomic Adoption Approval (Adoption completes, Pet marked ADOPTED, competing applications rejected)")
    public void testAtomicAdoptionApproval() {
        // Step 1: Create a pet
        Pet pet = new Pet();
        pet.setShelterId(shelterUser.getId());
        pet.setName("MultiApplicantPet");
        pet.setSpecies("Cat");
        pet.setBreed("Domestic Shorthair");
        pet.setAgeMonths(12);
        pet.setLocation("Austin, TX");
        pet.setDescription("Looking for a cozy home");
        pet.setStatus(Pet.PetStatus.AVAILABLE);
        Pet testPet = petDAO.create(pet);

        // Step 2: Sarah submits application
        AdoptionApplication app1 = new AdoptionApplication();
        app1.setPetId(testPet.getId());
        app1.setHomeType("House");
        app1.setLivingArrangement("Own Home");
        app1.setPetExperience("Cat lover");
        app1.setHouseholdMembers(2);
        app1.setAdoptionReason("Companion");
        app1.setCareCommitment("Full care");
        app1.setAgreementTerms(true);
        AdoptionApplication sarahApp = adoptionService.submitApplication(app1, adopterSarah);

        // Step 3: David submits application for the same pet
        AdoptionApplication app2 = new AdoptionApplication();
        app2.setPetId(testPet.getId());
        app2.setHomeType("Apartment");
        app2.setLivingArrangement("Cat safe");
        app2.setPetExperience("First cat");
        app2.setHouseholdMembers(1);
        app2.setAdoptionReason("Study buddy");
        app2.setCareCommitment("Full care");
        app2.setAgreementTerms(true);
        AdoptionApplication davidApp = adoptionService.submitApplication(app2, adopterDavid);

        // Step 4: Shelter reviews and APPROVES Sarah's application
        adoptionService.reviewApplication(sarahApp.getId(), "APPROVED", "Approved! Perfect home.", shelterUser);

        // Verify Step 4 results:
        // A. Sarah's application must be APPROVED
        AdoptionApplication updatedSarahApp = appDAO.findById(sarahApp.getId()).orElseThrow();
        assertEquals(AdoptionApplication.ApplicationStatus.APPROVED, updatedSarahApp.getStatus());

        // B. The pet must now be marked ADOPTED
        Pet updatedPet = petDAO.findById(testPet.getId()).orElseThrow();
        assertEquals(Pet.PetStatus.ADOPTED, updatedPet.getStatus(), "Pet status must become ADOPTED atomically");

        // C. Competing application from David must automatically be marked REJECTED
        AdoptionApplication updatedDavidApp = appDAO.findById(davidApp.getId()).orElseThrow();
        assertEquals(AdoptionApplication.ApplicationStatus.REJECTED, updatedDavidApp.getStatus(),
            "Competing applications must be automatically rejected to prevent double adoption");
        assertTrue(updatedDavidApp.getReviewNotes().contains("adopted by another applicant"));
    }
}

package com.petadoption;

import com.petadoption.config.DatabaseConfig;
import com.petadoption.dao.AuditLogDAOImpl;
import com.petadoption.dao.PetDAO;
import com.petadoption.dao.PetDAOImpl;
import com.petadoption.dao.UserDAOImpl;
import com.petadoption.exception.AuthorizationException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.service.NotificationService;
import com.petadoption.service.PetService;
import com.petadoption.service.PetServiceImpl;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetServiceTest {

    private static PetService petService;
    private static PetDAO petDAO;
    private static User shelterUser;
    private static User otherShelterUser;
    private static User adminUser;

    @BeforeAll
    public static void setUp() {
        System.setProperty("app.environment", "h2");
        DatabaseConfig.initialize();
        petDAO = new PetDAOImpl();
        petService = new PetServiceImpl(petDAO, new AuditLogDAOImpl(), NotificationService.getInstance());

        UserDAOImpl userDAO = new UserDAOImpl();
        adminUser = userDAO.findById(1L).orElseThrow();
        shelterUser = userDAO.findById(2L).orElseThrow();
        otherShelterUser = userDAO.findById(3L).orElseThrow();
    }

    @Test
    @Order(1)
    @DisplayName("PET-01: Shelter lists pet, status initially PENDING_REVIEW")
    public void testCreatePetListing() {
        Pet pet = new Pet();
        pet.setName("Barnaby");
        pet.setSpecies("Dog");
        pet.setBreed("Beagle Mix");
        pet.setAgeMonths(14);
        pet.setGender(Pet.Gender.MALE);
        pet.setSize(Pet.PetSize.MEDIUM);
        pet.setLocation("Austin, TX");
        pet.setDescription("Playful and energetic puppy looking for active family.");
        pet.setHealthStatus("Vaccinated and dewormed");
        pet.setVaccinationStatus("Up to date");
        pet.setSpayedNeutered(true);

        Pet created = petService.createPet(pet, shelterUser);
        assertNotNull(created.getId());
        assertEquals(Pet.PetStatus.PENDING_REVIEW, created.getStatus());
        assertEquals(shelterUser.getId(), created.getShelterId());
    }

    @Test
    @Order(2)
    @DisplayName("RBAC-02: Shelter cannot modify another shelter's pet listing (IDOR Prevention)")
    public void testIDORPreventionOnPetUpdate() {
        // Pet ID 1 belongs to shelter ID 2 (Happy Paws)
        Pet petOfShelter2 = petDAO.findById(1L).orElseThrow();
        assertEquals(2L, petOfShelter2.getShelterId());

        petOfShelter2.setName("Malicious Renaming");

        // Shelter ID 3 (otherShelterUser) tries to update it
        AuthorizationException ex = assertThrows(AuthorizationException.class, () -> {
            petService.updatePet(petOfShelter2, otherShelterUser);
        });
        assertTrue(ex.getMessage().contains("do not have permission to modify another shelter's pet"));
    }

    @Test
    @Order(3)
    @DisplayName("PET-03: Admin approves pending pet listing to AVAILABLE")
    public void testAdminApprovePet() {
        Pet pet = new Pet();
        pet.setName("Mittens");
        pet.setSpecies("Cat");
        pet.setBreed("Tabby");
        pet.setAgeMonths(8);
        pet.setLocation("Austin, TX");
        pet.setDescription("Sweet indoor kitten.");

        Pet created = petService.createPet(pet, shelterUser);
        assertEquals(Pet.PetStatus.PENDING_REVIEW, created.getStatus());

        // Admin approves
        petService.moderatePet(created.getId(), "AVAILABLE", null, adminUser);

        Pet updated = petService.getPetById(created.getId());
        assertEquals(Pet.PetStatus.AVAILABLE, updated.getStatus());
        assertEquals(adminUser.getId(), updated.getReviewedBy());
    }

    @Test
    @Order(4)
    @DisplayName("PET-04: Admin rejects pet listing with mandatory feedback reason")
    public void testAdminRejectPetWithReason() {
        Pet pet = new Pet();
        pet.setName("IncompleteListing");
        pet.setSpecies("Rabbit");
        pet.setBreed("Lop");
        pet.setAgeMonths(5);
        pet.setLocation("Denver, CO");
        pet.setDescription("Missing photo and vet documentation.");

        Pet created = petService.createPet(pet, otherShelterUser);

        // Rejecting without reason should fail validation
        assertThrows(ValidationException.class, () -> {
            petService.moderatePet(created.getId(), "REJECTED", "", adminUser);
        });

        // Reject with valid reason
        petService.moderatePet(created.getId(), "REJECTED", "Please upload a clear photograph and specify vaccination details.", adminUser);

        Pet rejected = petService.getPetById(created.getId());
        assertEquals(Pet.PetStatus.REJECTED, rejected.getStatus());
        assertEquals("Please upload a clear photograph and specify vaccination details.", rejected.getRejectionReason());
    }

    @Test
    @Order(5)
    @DisplayName("PET-02: Public search returns only approved AVAILABLE pets")
    public void testPublicSearchOnlyReturnsAvailable() {
        Map<String, Object> result = petService.searchPublicPets("Dog", null, "ALL", "ALL", null, 1, 10);
        assertNotNull(result.get("pets"));
        @SuppressWarnings("unchecked")
        java.util.List<Pet> pets = (java.util.List<Pet>) result.get("pets");

        assertFalse(pets.isEmpty());
        for (Pet p : pets) {
            assertEquals(Pet.PetStatus.AVAILABLE, p.getStatus(), "All public catalog pets must be AVAILABLE");
            assertEquals("Dog", p.getSpecies());
        }
    }
}

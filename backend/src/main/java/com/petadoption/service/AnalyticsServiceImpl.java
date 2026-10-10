package com.petadoption.service;

import com.petadoption.dao.*;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.Pet;
import com.petadoption.model.PlatformAnalytics;
import com.petadoption.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserDAO userDAO;
    private final PetDAO petDAO;
    private final AdoptionApplicationDAO applicationDAO;

    public AnalyticsServiceImpl() {
        this(new UserDAOImpl(), new PetDAOImpl(), new AdoptionApplicationDAOImpl());
    }

    public AnalyticsServiceImpl(UserDAO userDAO, PetDAO petDAO, AdoptionApplicationDAO applicationDAO) {
        this.userDAO = userDAO;
        this.petDAO = petDAO;
        this.applicationDAO = applicationDAO;
    }

    @Override
    public PlatformAnalytics getPlatformAnalytics() {
        PlatformAnalytics analytics = new PlatformAnalytics();
        analytics.setTotalUsers(userDAO.countTotal());
        analytics.setTotalShelters(userDAO.countByRole(User.Role.SHELTER));
        analytics.setTotalAdopters(userDAO.countByRole(User.Role.ADOPTER));

        analytics.setTotalPets(petDAO.countTotal());
        analytics.setAvailablePets(petDAO.countByStatus(Pet.PetStatus.AVAILABLE));
        analytics.setPendingPetListings(petDAO.countByStatus(Pet.PetStatus.PENDING_REVIEW));

        analytics.setPendingApplications(applicationDAO.countByStatus(AdoptionApplication.ApplicationStatus.PENDING));
        analytics.setApprovedAdoptions(applicationDAO.countByStatus(AdoptionApplication.ApplicationStatus.APPROVED));

        analytics.setPetsBySpecies(petDAO.countGroupBySpecies());
        analytics.setApplicationsByStatus(applicationDAO.countGroupByStatus());

        return analytics;
    }

    @Override
    public Map<String, Object> getShelterAnalytics(Long shelterId) {
        List<Pet> shelterPets = petDAO.findByShelterId(shelterId);

        long totalPets = shelterPets.size();
        long availablePets = shelterPets.stream().filter(p -> p.getStatus() == Pet.PetStatus.AVAILABLE).count();
        long pendingListings = shelterPets.stream().filter(p -> p.getStatus() == Pet.PetStatus.PENDING_REVIEW).count();
        long adoptedPets = shelterPets.stream().filter(p -> p.getStatus() == Pet.PetStatus.ADOPTED).count();

        long pendingApps = applicationDAO.countByShelterAndStatus(shelterId, AdoptionApplication.ApplicationStatus.PENDING);
        long underReviewApps = applicationDAO.countByShelterAndStatus(shelterId, AdoptionApplication.ApplicationStatus.UNDER_REVIEW);
        long approvedApps = applicationDAO.countByShelterAndStatus(shelterId, AdoptionApplication.ApplicationStatus.APPROVED);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPets", totalPets);
        stats.put("availablePets", availablePets);
        stats.put("pendingListings", pendingListings);
        stats.put("adoptedPets", adoptedPets);
        stats.put("pendingApplications", pendingApps);
        stats.put("underReviewApplications", underReviewApps);
        stats.put("approvedApplications", approvedApps);

        return stats;
    }
}

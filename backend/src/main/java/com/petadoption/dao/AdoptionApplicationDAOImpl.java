package com.petadoption.dao;

import com.petadoption.config.DatabaseConfig;
import com.petadoption.exception.DatabaseException;
import com.petadoption.model.AdoptionApplication;

import java.sql.*;
import java.util.*;

public class AdoptionApplicationDAOImpl implements AdoptionApplicationDAO {

    @Override
    public Optional<AdoptionApplication> findById(Long id) {
        String sql = """
            SELECT a.*, p.name AS pet_name, p.species AS pet_species, p.image_url AS pet_image_url, p.shelter_id,
                   u.full_name AS adopter_name, u.email AS adopter_email, u.phone AS adopter_phone
            FROM adoption_applications a
            JOIN pets p ON a.pet_id = p.id
            JOIN users u ON a.adopter_id = u.id
            WHERE a.id = ?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToApplication(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding application by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public AdoptionApplication create(AdoptionApplication app) {
        String sql = """
            INSERT INTO adoption_applications
            (pet_id, adopter_id, living_arrangement, home_type, pet_experience, household_members,
             adoption_reason, care_commitment, agreement_terms, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, app.getPetId());
            stmt.setLong(2, app.getAdopterId());
            stmt.setString(3, app.getLivingArrangement());
            stmt.setString(4, app.getHomeType());
            stmt.setString(5, app.getPetExperience());
            stmt.setInt(6, app.getHouseholdMembers() != null ? app.getHouseholdMembers() : 1);
            stmt.setString(7, app.getAdoptionReason());
            stmt.setString(8, app.getCareCommitment());
            stmt.setBoolean(9, app.getAgreementTerms() != null && app.getAgreementTerms());
            stmt.setString(10, app.getStatus() != null ? app.getStatus().name() : AdoptionApplication.ApplicationStatus.PENDING.name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating application failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    app.setId(generatedKeys.getLong(1));
                }
            }
            return app;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating adoption application for pet ID: " + app.getPetId(), e);
        }
    }

    @Override
    public void updateStatus(Long applicationId, AdoptionApplication.ApplicationStatus status, String reviewNotes) {
        String sql = """
            UPDATE adoption_applications SET
                status = ?, review_notes = ?, reviewed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, reviewNotes);
            stmt.setLong(3, applicationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating application status for ID: " + applicationId, e);
        }
    }

    /**
     * Executes atomic multi-step JDBC transaction:
     * 1. Marks selected application as APPROVED.
     * 2. Marks the associated pet as ADOPTED.
     * 3. Automatically rejects other pending applications for the same pet.
     */
    @Override
    public void approveApplicationAndAdoptPet(Long applicationId, Long petId, String reviewNotes) {
        String updateAppSql = """
            UPDATE adoption_applications SET
                status = 'APPROVED', review_notes = ?, reviewed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        String updatePetSql = """
            UPDATE pets SET
                status = 'ADOPTED', updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        String rejectOthersSql = """
            UPDATE adoption_applications SET
                status = 'REJECTED',
                review_notes = 'Pet has been adopted by another applicant.',
                reviewed_at = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            WHERE pet_id = ? AND id != ? AND status IN ('PENDING', 'UNDER_REVIEW')
        """;

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // Step 1: Approve selected application
            try (PreparedStatement stmtApp = conn.prepareStatement(updateAppSql)) {
                stmtApp.setString(1, reviewNotes);
                stmtApp.setLong(2, applicationId);
                stmtApp.executeUpdate();
            }

            // Step 2: Mark pet as ADOPTED
            try (PreparedStatement stmtPet = conn.prepareStatement(updatePetSql)) {
                stmtPet.setLong(1, petId);
                stmtPet.executeUpdate();
            }

            // Step 3: Reject other pending applications for this pet
            try (PreparedStatement stmtOthers = conn.prepareStatement(rejectOthersSql)) {
                stmtOthers.setLong(1, petId);
                stmtOthers.setLong(2, applicationId);
                stmtOthers.executeUpdate();
            }

            conn.commit(); // Commit atomic transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on failure
                } catch (SQLException ex) {
                    // Suppressed
                }
            }
            throw new DatabaseException("Transaction failed: could not approve application and adopt pet", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    // Suppressed
                }
            }
        }
    }

    @Override
    public boolean existsByPetAndAdopter(Long petId, Long adopterId) {
        String sql = "SELECT COUNT(*) FROM adoption_applications WHERE pet_id = ? AND adopter_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, petId);
            stmt.setLong(2, adopterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking existing application", e);
        }
        return false;
    }

    @Override
    public List<AdoptionApplication> findByAdopterId(Long adopterId) {
        String sql = """
            SELECT a.*, p.name AS pet_name, p.species AS pet_species, p.image_url AS pet_image_url, p.shelter_id,
                   u.full_name AS adopter_name, u.email AS adopter_email, u.phone AS adopter_phone
            FROM adoption_applications a
            JOIN pets p ON a.pet_id = p.id
            JOIN users u ON a.adopter_id = u.id
            WHERE a.adopter_id = ?
            ORDER BY a.submitted_at DESC
        """;
        List<AdoptionApplication> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, adopterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToApplication(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving applications for adopter ID: " + adopterId, e);
        }
        return list;
    }

    @Override
    public List<AdoptionApplication> findByShelterId(Long shelterId) {
        String sql = """
            SELECT a.*, p.name AS pet_name, p.species AS pet_species, p.image_url AS pet_image_url, p.shelter_id,
                   u.full_name AS adopter_name, u.email AS adopter_email, u.phone AS adopter_phone
            FROM adoption_applications a
            JOIN pets p ON a.pet_id = p.id
            JOIN users u ON a.adopter_id = u.id
            WHERE p.shelter_id = ?
            ORDER BY a.submitted_at DESC
        """;
        List<AdoptionApplication> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, shelterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToApplication(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving applications for shelter ID: " + shelterId, e);
        }
        return list;
    }

    @Override
    public List<AdoptionApplication> findByPetId(Long petId) {
        String sql = """
            SELECT a.*, p.name AS pet_name, p.species AS pet_species, p.image_url AS pet_image_url, p.shelter_id,
                   u.full_name AS adopter_name, u.email AS adopter_email, u.phone AS adopter_phone
            FROM adoption_applications a
            JOIN pets p ON a.pet_id = p.id
            JOIN users u ON a.adopter_id = u.id
            WHERE a.pet_id = ?
            ORDER BY a.submitted_at DESC
        """;
        List<AdoptionApplication> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, petId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToApplication(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving applications for pet ID: " + petId, e);
        }
        return list;
    }

    @Override
    public long countTotal() {
        String sql = "SELECT COUNT(*) FROM adoption_applications";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Error counting total applications", e);
        }
        return 0;
    }

    @Override
    public long countByStatus(AdoptionApplication.ApplicationStatus status) {
        String sql = "SELECT COUNT(*) FROM adoption_applications WHERE status = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting applications by status: " + status, e);
        }
        return 0;
    }

    @Override
    public long countByShelterAndStatus(Long shelterId, AdoptionApplication.ApplicationStatus status) {
        String sql = """
            SELECT COUNT(*) FROM adoption_applications a
            JOIN pets p ON a.pet_id = p.id
            WHERE p.shelter_id = ? AND a.status = ?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, shelterId);
            stmt.setString(2, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting shelter applications by status", e);
        }
        return 0;
    }

    @Override
    public Map<String, Long> countGroupByStatus() {
        String sql = "SELECT status, COUNT(*) FROM adoption_applications GROUP BY status";
        Map<String, Long> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getLong(2));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error grouping applications by status", e);
        }
        return map;
    }

    private AdoptionApplication mapResultSetToApplication(ResultSet rs) throws SQLException {
        AdoptionApplication app = new AdoptionApplication();
        app.setId(rs.getLong("id"));
        app.setPetId(rs.getLong("pet_id"));
        try {
            app.setPetName(rs.getString("pet_name"));
            app.setPetSpecies(rs.getString("pet_species"));
            app.setPetImageUrl(rs.getString("pet_image_url"));
            app.setShelterId(rs.getLong("shelter_id"));
        } catch (SQLException ignored) {}

        app.setAdopterId(rs.getLong("adopter_id"));
        try {
            app.setAdopterName(rs.getString("adopter_name"));
            app.setAdopterEmail(rs.getString("adopter_email"));
            app.setAdopterPhone(rs.getString("adopter_phone"));
        } catch (SQLException ignored) {}

        app.setLivingArrangement(rs.getString("living_arrangement"));
        app.setHomeType(rs.getString("home_type"));
        app.setPetExperience(rs.getString("pet_experience"));
        app.setHouseholdMembers(rs.getInt("household_members"));
        app.setAdoptionReason(rs.getString("adoption_reason"));
        app.setCareCommitment(rs.getString("care_commitment"));
        app.setAgreementTerms(rs.getBoolean("agreement_terms"));
        app.setStatus(AdoptionApplication.ApplicationStatus.fromString(rs.getString("status")));
        app.setReviewNotes(rs.getString("review_notes"));

        Timestamp revAt = rs.getTimestamp("reviewed_at");
        if (revAt != null) app.setReviewedAt(revAt.toLocalDateTime());

        Timestamp subAt = rs.getTimestamp("submitted_at");
        if (subAt != null) app.setSubmittedAt(subAt.toLocalDateTime());

        Timestamp upAt = rs.getTimestamp("updated_at");
        if (upAt != null) app.setUpdatedAt(upAt.toLocalDateTime());

        return app;
    }
}

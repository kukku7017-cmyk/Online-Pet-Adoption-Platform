package com.petadoption.dao;

import com.petadoption.config.DatabaseConfig;
import com.petadoption.exception.DatabaseException;
import com.petadoption.model.Pet;

import java.sql.*;
import java.util.*;

public class PetDAOImpl implements PetDAO {

    @Override
    public Optional<Pet> findById(Long id) {
        String sql = """
            SELECT p.*, u.full_name AS shelter_name
            FROM pets p
            JOIN users u ON p.shelter_id = u.id
            WHERE p.id = ?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding pet by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Pet create(Pet pet) {
        String sql = """
            INSERT INTO pets (shelter_id, name, species, breed, age_months, gender, size, location,
                              description, health_status, vaccination_status, spayed_neutered, image_url, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, pet.getShelterId());
            stmt.setString(2, pet.getName());
            stmt.setString(3, pet.getSpecies());
            stmt.setString(4, pet.getBreed());
            stmt.setInt(5, pet.getAgeMonths() != null ? pet.getAgeMonths() : 0);
            stmt.setString(6, pet.getGender() != null ? pet.getGender().name() : Pet.Gender.UNKNOWN.name());
            stmt.setString(7, pet.getSize() != null ? pet.getSize().name() : Pet.PetSize.MEDIUM.name());
            stmt.setString(8, pet.getLocation());
            stmt.setString(9, pet.getDescription());
            stmt.setString(10, pet.getHealthStatus());
            stmt.setString(11, pet.getVaccinationStatus());
            stmt.setBoolean(12, pet.getSpayedNeutered() != null && pet.getSpayedNeutered());
            stmt.setString(13, pet.getImageUrl());
            stmt.setString(14, pet.getStatus() != null ? pet.getStatus().name() : Pet.PetStatus.PENDING_REVIEW.name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating pet failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pet.setId(generatedKeys.getLong(1));
                }
            }
            return pet;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating pet: " + pet.getName(), e);
        }
    }

    @Override
    public void update(Pet pet) {
        String sql = """
            UPDATE pets SET
                name = ?, species = ?, breed = ?, age_months = ?, gender = ?, size = ?,
                location = ?, description = ?, health_status = ?, vaccination_status = ?,
                spayed_neutered = ?, image_url = ?, status = ?, rejection_reason = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getSpecies());
            stmt.setString(3, pet.getBreed());
            stmt.setInt(4, pet.getAgeMonths() != null ? pet.getAgeMonths() : 0);
            stmt.setString(5, pet.getGender() != null ? pet.getGender().name() : Pet.Gender.UNKNOWN.name());
            stmt.setString(6, pet.getSize() != null ? pet.getSize().name() : Pet.PetSize.MEDIUM.name());
            stmt.setString(7, pet.getLocation());
            stmt.setString(8, pet.getDescription());
            stmt.setString(9, pet.getHealthStatus());
            stmt.setString(10, pet.getVaccinationStatus());
            stmt.setBoolean(11, pet.getSpayedNeutered() != null && pet.getSpayedNeutered());
            stmt.setString(12, pet.getImageUrl());
            stmt.setString(13, pet.getStatus() != null ? pet.getStatus().name() : Pet.PetStatus.PENDING_REVIEW.name());
            stmt.setString(14, pet.getRejectionReason());
            stmt.setLong(15, pet.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating pet ID: " + pet.getId(), e);
        }
    }

    @Override
    public void updateStatus(Long petId, Pet.PetStatus status, String rejectionReason, Long reviewedBy) {
        String sql = """
            UPDATE pets SET
                status = ?, rejection_reason = ?, reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, rejectionReason);
            if (reviewedBy != null) {
                stmt.setLong(3, reviewedBy);
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            stmt.setLong(4, petId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating pet status for ID: " + petId, e);
        }
    }

    @Override
    public List<Pet> findByShelterId(Long shelterId) {
        String sql = """
            SELECT p.*, u.full_name AS shelter_name
            FROM pets p
            JOIN users u ON p.shelter_id = u.id
            WHERE p.shelter_id = ?
            ORDER BY p.created_at DESC
        """;
        List<Pet> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, shelterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving pets for shelter ID: " + shelterId, e);
        }
        return list;
    }

    @Override
    public List<Pet> findByStatus(Pet.PetStatus status) {
        String sql = """
            SELECT p.*, u.full_name AS shelter_name
            FROM pets p
            JOIN users u ON p.shelter_id = u.id
            WHERE p.status = ?
            ORDER BY p.created_at DESC
        """;
        List<Pet> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving pets by status: " + status, e);
        }
        return list;
    }

    @Override
    public List<Pet> searchPublicPets(String species, String breed, String gender, String size, String query, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
            SELECT p.*, u.full_name AS shelter_name
            FROM pets p
            JOIN users u ON p.shelter_id = u.id
            WHERE p.status = 'AVAILABLE'
        """);

        List<Object> params = new ArrayList<>();
        appendSearchConditions(sql, params, species, breed, gender, size, query);

        sql.append(" ORDER BY p.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Pet> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching public pets", e);
        }
        return list;
    }

    @Override
    public long countPublicPets(String species, String breed, String gender, String size, String query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM pets p WHERE p.status = 'AVAILABLE'");
        List<Object> params = new ArrayList<>();
        appendSearchConditions(sql, params, species, breed, gender, size, query);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting public pets", e);
        }
        return 0;
    }

    private void appendSearchConditions(StringBuilder sql, List<Object> params,
                                        String species, String breed, String gender, String size, String query) {
        if (species != null && !species.isBlank() && !"ALL".equalsIgnoreCase(species)) {
            sql.append(" AND LOWER(p.species) = LOWER(?)");
            params.add(species.trim());
        }
        if (breed != null && !breed.isBlank()) {
            sql.append(" AND LOWER(p.breed) LIKE LOWER(?)");
            params.add("%" + breed.trim() + "%");
        }
        if (gender != null && !gender.isBlank() && !"ALL".equalsIgnoreCase(gender)) {
            sql.append(" AND p.gender = ?");
            params.add(gender.trim().toUpperCase());
        }
        if (size != null && !size.isBlank() && !"ALL".equalsIgnoreCase(size)) {
            sql.append(" AND p.size = ?");
            params.add(size.trim().toUpperCase());
        }
        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(p.name) LIKE LOWER(?) OR LOWER(p.breed) LIKE LOWER(?) OR LOWER(p.location) LIKE LOWER(?) OR LOWER(p.description) LIKE LOWER(?))");
            String q = "%" + query.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }
    }

    @Override
    public long countTotal() {
        String sql = "SELECT COUNT(*) FROM pets";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Error counting total pets", e);
        }
        return 0;
    }

    @Override
    public long countByStatus(Pet.PetStatus status) {
        String sql = "SELECT COUNT(*) FROM pets WHERE status = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting pets by status: " + status, e);
        }
        return 0;
    }

    @Override
    public Map<String, Long> countGroupBySpecies() {
        String sql = "SELECT species, COUNT(*) FROM pets WHERE status = 'AVAILABLE' GROUP BY species";
        Map<String, Long> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getLong(2));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error grouping pets by species", e);
        }
        return map;
    }

    private Pet mapResultSetToPet(ResultSet rs) throws SQLException {
        Pet pet = new Pet();
        pet.setId(rs.getLong("id"));
        pet.setShelterId(rs.getLong("shelter_id"));
        try {
            pet.setShelterName(rs.getString("shelter_name"));
        } catch (SQLException ignored) {}
        pet.setName(rs.getString("name"));
        pet.setSpecies(rs.getString("species"));
        pet.setBreed(rs.getString("breed"));
        pet.setAgeMonths(rs.getInt("age_months"));
        pet.setGender(Pet.Gender.fromString(rs.getString("gender")));
        pet.setSize(Pet.PetSize.fromString(rs.getString("size")));
        pet.setLocation(rs.getString("location"));
        pet.setDescription(rs.getString("description"));
        pet.setHealthStatus(rs.getString("health_status"));
        pet.setVaccinationStatus(rs.getString("vaccination_status"));
        pet.setSpayedNeutered(rs.getBoolean("spayed_neutered"));
        pet.setImageUrl(rs.getString("image_url"));
        pet.setStatus(Pet.PetStatus.fromString(rs.getString("status")));
        pet.setRejectionReason(rs.getString("rejection_reason"));

        long reviewedBy = rs.getLong("reviewed_by");
        if (!rs.wasNull()) pet.setReviewedBy(reviewedBy);

        Timestamp revAt = rs.getTimestamp("reviewed_at");
        if (revAt != null) pet.setReviewedAt(revAt.toLocalDateTime());

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) pet.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) pet.setUpdatedAt(updated.toLocalDateTime());

        return pet;
    }
}

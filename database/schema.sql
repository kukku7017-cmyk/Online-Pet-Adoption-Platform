-- =============================================================================
-- Database Schema for Online Pet Adoption Platform
-- Dialect: MySQL 8.0+ / InnoDB
-- Compatibility: Jakarta EE 10 / Tomcat 10.1 / Java 17+
-- =============================================================================

CREATE DATABASE IF NOT EXISTS pet_adoption_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pet_adoption_db;

-- 1. Users Table (Admin, Shelter, Adopter)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    role ENUM('ADMIN', 'SHELTER', 'ADOPTER') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role),
    INDEX idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Pets Table
CREATE TABLE IF NOT EXISTS pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shelter_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(100) NOT NULL,
    age_months INT NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',
    size ENUM('SMALL', 'MEDIUM', 'LARGE', 'EXTRA_LARGE') NOT NULL DEFAULT 'MEDIUM',
    location VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    health_status TEXT,
    vaccination_status VARCHAR(100),
    spayed_neutered BOOLEAN NOT NULL DEFAULT FALSE,
    image_url VARCHAR(500),
    status ENUM('PENDING_REVIEW', 'AVAILABLE', 'REJECTED', 'ADOPTED') NOT NULL DEFAULT 'PENDING_REVIEW',
    rejection_reason TEXT,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shelter_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_pet_status (status),
    INDEX idx_pet_species (species),
    INDEX idx_pet_shelter (shelter_id),
    INDEX idx_pet_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Adoption Applications Table
CREATE TABLE IF NOT EXISTS adoption_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    adopter_id BIGINT NOT NULL,
    living_arrangement VARCHAR(100) NOT NULL,
    home_type VARCHAR(100) NOT NULL,
    pet_experience TEXT NOT NULL,
    household_members INT NOT NULL DEFAULT 1,
    adoption_reason TEXT NOT NULL,
    care_commitment TEXT NOT NULL,
    agreement_terms BOOLEAN NOT NULL DEFAULT FALSE,
    status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    review_notes TEXT,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (adopter_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uq_pet_adopter_application (pet_id, adopter_id),
    INDEX idx_app_status (status),
    INDEX idx_app_adopter (adopter_id),
    INDEX idx_app_pet (pet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Conversations Table (Linked to an Adoption Application)
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    shelter_id BIGINT NOT NULL,
    adopter_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES adoption_applications(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (shelter_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (adopter_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uq_app_conversation (application_id),
    INDEX idx_conv_shelter (shelter_id),
    INDEX idx_conv_adopter (adopter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Messages Table
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_msg_conv (conversation_id),
    INDEX idx_msg_recipient_read (recipient_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. System Settings Table (Dynamic Configuration)
CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    description VARCHAR(255),
    is_editable BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Audit Logs Table (Administrative and Security Event Auditing)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

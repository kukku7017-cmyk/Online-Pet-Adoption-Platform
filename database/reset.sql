-- =============================================================================
-- WARNING: DESTRUCTIVE SCRIPT
-- This script permanently drops all tables in the 'pet_adoption_db' schema.
-- Intended ONLY for development testing and clean resets.
-- =============================================================================

USE pet_adoption_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS conversations;
DROP TABLE IF EXISTS adoption_applications;
DROP TABLE IF EXISTS pets;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

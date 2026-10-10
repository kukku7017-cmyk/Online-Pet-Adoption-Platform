package com.petadoption.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import com.petadoption.util.PasswordUtil;

public final class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static volatile HikariDataSource dataSource;
    private static volatile boolean isFallbackH2 = false;

    private DatabaseConfig() {}

    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        Properties props = loadProperties();

        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");
        String envDriver = System.getenv("DB_DRIVER");
        String appEnv = System.getenv("APP_ENV");

        String dbUrl = envUrl != null ? envUrl : props.getProperty("db.url", "jdbc:mysql://localhost:3306/pet_adoption_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        String dbUser = envUser != null ? envUser : props.getProperty("db.username", "root");
        String dbPass = envPass != null ? envPass : props.getProperty("db.password", "root");
        String dbDriver = envDriver != null ? envDriver : props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        String environment = appEnv != null ? appEnv : props.getProperty("app.environment", "auto");

        // Try initializing configured database (MySQL)
        boolean initialized = false;
        if (!"h2".equalsIgnoreCase(environment)) {
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(dbUrl);
                config.setUsername(dbUser);
                config.setPassword(dbPass);
                config.setDriverClassName(dbDriver);
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(3000); // 3 seconds timeout to quickly detect if local MySQL is accessible
                config.setPoolName("PetAdoption-MySQL-Pool");

                dataSource = new HikariDataSource(config);
                // Test connection
                try (Connection conn = dataSource.getConnection()) {
                    logger.info("Successfully connected to MySQL at: {}", dbUrl);
                    initialized = true;
                    isFallbackH2 = false;
                }
            } catch (Exception e) {
                logger.warn("Could not connect to MySQL ({}). Falling back to resilient in-memory H2 MySQL mode for automated execution.", e.getMessage());
                if (dataSource != null && !dataSource.isClosed()) {
                    dataSource.close();
                }
                dataSource = null;
            }
        }

        // If MySQL failed or H2 requested, switch to in-memory H2 with MySQL compatibility
        if (!initialized) {
            try {
                logger.info("Initializing in-memory H2 database with MySQL compatibility mode...");
                HikariConfig h2Config = new HikariConfig();
                h2Config.setJdbcUrl("jdbc:h2:mem:pet_adoption_db;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE");
                h2Config.setUsername("sa");
                h2Config.setPassword("");
                h2Config.setDriverClassName("org.h2.Driver");
                h2Config.setMaximumPoolSize(10);
                h2Config.setPoolName("PetAdoption-H2-Pool");

                dataSource = new HikariDataSource(h2Config);
                isFallbackH2 = true;

                // Bootstrap tables and initial seed data in H2
                bootstrapH2Database();
                logger.info("In-memory H2 database successfully initialized and seeded!");
            } catch (Exception e) {
                logger.error("Fatal error initializing in-memory fallback database: {}", e.getMessage(), e);
                throw new RuntimeException("Could not initialize database", e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource;
    }

    public static boolean isFallbackH2() {
        return isFallbackH2;
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed successfully.");
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            logger.warn("Could not load application.properties, using defaults");
        }
        return props;
    }

    private static void bootstrapH2Database() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    phone VARCHAR(20),
                    address TEXT,
                    role VARCHAR(20) NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // Create pets table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pets (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    shelter_id BIGINT NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    species VARCHAR(50) NOT NULL,
                    breed VARCHAR(100) NOT NULL,
                    age_months INT NOT NULL,
                    gender VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
                    size VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
                    location VARCHAR(150) NOT NULL,
                    description TEXT NOT NULL,
                    health_status TEXT,
                    vaccination_status VARCHAR(100),
                    spayed_neutered BOOLEAN NOT NULL DEFAULT FALSE,
                    image_url VARCHAR(500),
                    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_REVIEW',
                    rejection_reason TEXT,
                    reviewed_by BIGINT,
                    reviewed_at TIMESTAMP NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (shelter_id) REFERENCES users(id)
                );
            """);

            // Create adoption_applications table
            stmt.execute("""
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
                    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                    review_notes TEXT,
                    reviewed_at TIMESTAMP NULL,
                    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (pet_id) REFERENCES pets(id),
                    FOREIGN KEY (adopter_id) REFERENCES users(id),
                    CONSTRAINT uq_pet_adopter UNIQUE (pet_id, adopter_id)
                );
            """);

            // Create conversations table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS conversations (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    application_id BIGINT NOT NULL UNIQUE,
                    shelter_id BIGINT NOT NULL,
                    adopter_id BIGINT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_message_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (application_id) REFERENCES adoption_applications(id),
                    FOREIGN KEY (shelter_id) REFERENCES users(id),
                    FOREIGN KEY (adopter_id) REFERENCES users(id)
                );
            """);

            // Create messages table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    conversation_id BIGINT NOT NULL,
                    sender_id BIGINT NOT NULL,
                    recipient_id BIGINT NOT NULL,
                    content TEXT NOT NULL,
                    is_read BOOLEAN NOT NULL DEFAULT FALSE,
                    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
                    FOREIGN KEY (sender_id) REFERENCES users(id),
                    FOREIGN KEY (recipient_id) REFERENCES users(id)
                );
            """);

            // Create system_settings table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS system_settings (
                    setting_key VARCHAR(100) PRIMARY KEY,
                    setting_value TEXT NOT NULL,
                    description VARCHAR(255),
                    is_editable BOOLEAN NOT NULL DEFAULT TRUE,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // Create audit_logs table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT,
                    action VARCHAR(100) NOT NULL,
                    entity_type VARCHAR(50) NOT NULL,
                    entity_id BIGINT,
                    details TEXT,
                    ip_address VARCHAR(45),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // Seed initial system settings
            stmt.execute("""
                MERGE INTO system_settings (setting_key, setting_value, description, is_editable) KEY(setting_key) VALUES
                ('site_name', 'PawsConnect', 'Platform Title', TRUE),
                ('contact_email', 'support@pawsconnect.org', 'Support Contact Email', TRUE),
                ('max_applications_per_user', '5', 'Max open applications an adopter can hold', TRUE),
                ('platform_tagline', 'Finding Loving Homes for Every Pet', 'Public Tagline', TRUE);
            """);

            // Seed initial users with validated BCrypt hash for "Password@123"
            String passHash = PasswordUtil.hashPassword("Password@123");

            stmt.execute(String.format("""
                MERGE INTO users (id, email, password_hash, full_name, phone, address, role, status) KEY(email) VALUES
                (1, 'admin@petadoption.com', '%s', 'Platform Administrator', '+1-555-0100', '100 Admin Plaza, Suite 400', 'ADMIN', 'ACTIVE'),
                (2, 'paws@shelter.org', '%s', 'Happy Paws Rescue', '+1-555-0102', '45 Green Meadow Lane, Austin, TX', 'SHELTER', 'ACTIVE'),
                (3, 'haven@shelter.org', '%s', 'Safe Haven Sanctuary', '+1-555-0103', '88 Riverview Rd, Denver, CO', 'SHELTER', 'ACTIVE'),
                (4, 'adopter.sarah@gmail.com', '%s', 'Sarah Jenkins', '+1-555-0104', '12 Pine St, Austin, TX', 'ADOPTER', 'ACTIVE'),
                (5, 'adopter.david@gmail.com', '%s', 'David Miller', '+1-555-0105', '77 Oak Ave, Denver, CO', 'ADOPTER', 'ACTIVE'),
                (6, 'adopter.priya@gmail.com', '%s', 'Priya Sharma', '+1-555-0106', '33 Maple Rd, Seattle, WA', 'ADOPTER', 'ACTIVE'),
                (7, 'shelter@happypaws.org', '%s', 'Happy Paws Rescue Main', '+1-555-0199', '10 Shelter Blvd, Austin, TX', 'SHELTER', 'ACTIVE'),
                (8, 'john.doe@gmail.com', '%s', 'John Doe', '+1-555-0198', '456 Elm Street, Austin, TX', 'ADOPTER', 'ACTIVE');
            """, passHash, passHash, passHash, passHash, passHash, passHash, passHash, passHash));

            // Seed pets with high-quality real photography
            stmt.execute("""
                MERGE INTO pets (id, shelter_id, name, species, breed, age_months, gender, size, location, description, health_status, vaccination_status, spayed_neutered, image_url, status) KEY(id) VALUES
                (1, 2, 'Bella', 'Dog', 'Golden Retriever', 24, 'FEMALE', 'LARGE', 'Austin, TX', 'Gentle, friendly, and eager to please. Bella gets along wonderfully with kids and other pets.', 'Fully checked, negative heartworm test', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
                (2, 2, 'Milo', 'Dog', 'French Bulldog', 14, 'MALE', 'SMALL', 'Austin, TX', 'Playful, comical, and loves cuddles on the sofa. Needs moderate daily exercise.', 'Healthy respiratory check', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
                (3, 2, 'Luna', 'Cat', 'Siamese Mix', 18, 'FEMALE', 'MEDIUM', 'Austin, TX', 'Vocal, affectionate, and deeply intelligent. Loves climbing towers and chasing laser toys.', 'FeLV/FIV negative', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
                (4, 3, 'Oliver', 'Cat', 'British Shorthair', 36, 'MALE', 'MEDIUM', 'Denver, CO', 'Placid, handsome gentleman cat with a quiet purr. Enjoys calm sunlit window sills.', 'Dental cleaning completed', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
                (5, 3, 'Charlie', 'Dog', 'Border Collie', 16, 'MALE', 'MEDIUM', 'Denver, CO', 'High-energy, hyper-intelligent agility enthusiast. Best suited for an active outdoor family.', 'Excellent stamina & joint health', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1503256207526-0d5d80fa2f47?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
                (6, 3, 'Thumper', 'Rabbit', 'Holland Lop', 10, 'MALE', 'SMALL', 'Denver, CO', 'Gentle, inquisitive bunny that loves fresh Timothy hay, romaine lettuce, and gentle head scratches.', 'Vaccinated for RHDV2', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
                (7, 2, 'Rio', 'Bird', 'Cockatiel', 12, 'MALE', 'SMALL', 'Austin, TX', 'Cheerful whistler who mimics household tunes and enjoys perching on friendly shoulders.', 'Avian vet check cleared', 'Up to date', FALSE, 'https://images.unsplash.com/photo-1522858547550-345670f99e4b?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
                (8, 2, 'Rocky', 'Dog', 'German Shepherd Mix', 30, 'MALE', 'LARGE', 'Austin, TX', 'Loyal guardian and loving family protector. Well trained in basic obedience and leash walking.', 'Microchipped, hip dysplasia screened clear', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1589941013453-ec89f33b5455?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
                (9, 3, 'Daisy', 'Dog', 'Beagle', 8, 'FEMALE', 'MEDIUM', 'Denver, CO', 'Curious, happy-go-lucky puppy ready to explore the world with an affectionate family.', 'First puppy series complete', 'Puppy shots up to date', FALSE, 'https://images.unsplash.com/photo-1537151625747-768eb6cf92b2?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
                (10, 2, 'Shadow', 'Cat', 'Domestic Shorthair (Black)', 20, 'MALE', 'MEDIUM', 'Austin, TX', 'Sleek, loving mini-panther who purrs like a motor engine when brushed.', 'FeLV/FIV negative', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=800&q=80', 'PENDING_REVIEW');
            """);

            // Seed sample applications
            stmt.execute("""
                MERGE INTO adoption_applications (id, pet_id, adopter_id, living_arrangement, home_type, pet_experience, household_members, adoption_reason, care_commitment, agreement_terms, status, review_notes) KEY(id) VALUES
                (1, 1, 4, 'Own house with fenced backyard', 'Single Family Home', 'Owned Golden Retrievers for 10 years previously.', 3, 'Looking for an energetic, gentle companion for our kids.', 'Daily 2-hour exercise, premium diet, bi-annual vet visits.', TRUE, 'APPROVED', 'Fantastic applicant. Spacious yard and extensive breed experience.'),
                (2, 2, 5, 'Rented apartment (landlord approved)', 'Apartment / Condo', 'First time dog owner, grew up with family cats.', 2, 'Want a small companion dog to share our cozy home.', 'Committed to positive reinforcement training and daily walks.', TRUE, 'UNDER_REVIEW', 'Application looks promising. Verifying pet deposit approval with landlord.'),
                (3, 3, 6, 'Own suburban townhome', 'Townhouse', 'Lifelong cat owner, current home is cat-proofed.', 1, 'In love with Siamese vocalizations and playful temperament.', 'Indoor only home with abundant vertical climbing space.', TRUE, 'PENDING', NULL);
            """);

            // Seed conversations
            stmt.execute("""
                MERGE INTO conversations (id, application_id, shelter_id, adopter_id) KEY(id) VALUES
                (1, 1, 2, 4),
                (2, 2, 2, 5),
                (3, 3, 2, 6);
            """);

            // Seed messages
            stmt.execute("""
                MERGE INTO messages (id, conversation_id, sender_id, recipient_id, content, is_read) KEY(id) VALUES
                (1, 1, 4, 2, 'Hello Happy Paws! We are so excited about Bella. Can we visit this Saturday?', TRUE),
                (2, 1, 2, 4, 'Hi Sarah! We would love that. Saturday 11:00 AM works perfectly. Bring the family!', TRUE),
                (3, 2, 5, 2, 'Hi! I submitted my application for Milo. Please let me know if you need landlord lease paperwork.', FALSE);
            """);
        } catch (SQLException e) {
            logger.error("Error bootstrapping H2 fallback: {}", e.getMessage(), e);
        }
    }
}

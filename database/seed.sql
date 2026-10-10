-- =============================================================================
-- Seed Demonstration Data for Online Pet Adoption Platform
-- Dialect: MySQL 8.0+
-- Demo Credentials:
--   Admin:   admin@petadoption.com / Password@123
--   Shelter: paws@shelter.org      / Password@123
--   Shelter: haven@shelter.org     / Password@123
--   Adopter: adopter.sarah@gmail.com / Password@123
--   Adopter: adopter.david@gmail.com / Password@123
--   Adopter: adopter.priya@gmail.com / Password@123
-- =============================================================================

USE pet_adoption_db;

-- 1. System Settings
INSERT INTO system_settings (setting_key, setting_value, description, is_editable) VALUES
('site_name', 'PawsConnect', 'Platform Title', TRUE),
('contact_email', 'support@pawsconnect.org', 'Support Contact Email', TRUE),
('max_applications_per_user', '5', 'Max open applications an adopter can hold', TRUE),
('platform_tagline', 'Finding Loving Homes for Every Rescued Pet', 'Public Tagline', TRUE)
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);

-- 2. Users (Password is 'Password@123' hashed with BCrypt)
SET @pwd_hash = '$2a$12$K1rS2mZ.7lP2L2lqE2cEdeiR2i.2687u3v36f2NsqeS2yHhYwJ7yG';

INSERT INTO users (id, email, password_hash, full_name, phone, address, role, status) VALUES
(1, 'admin@petadoption.com', @pwd_hash, 'Platform Administrator', '+1-555-0100', '100 Admin Plaza, Suite 400', 'ADMIN', 'ACTIVE'),
(2, 'paws@shelter.org', @pwd_hash, 'Happy Paws Rescue', '+1-555-0102', '45 Green Meadow Lane, Austin, TX', 'SHELTER', 'ACTIVE'),
(3, 'haven@shelter.org', @pwd_hash, 'Safe Haven Sanctuary', '+1-555-0103', '88 Riverview Rd, Denver, CO', 'SHELTER', 'ACTIVE'),
(4, 'adopter.sarah@gmail.com', @pwd_hash, 'Sarah Jenkins', '+1-555-0104', '12 Pine St, Austin, TX', 'ADOPTER', 'ACTIVE'),
(5, 'adopter.david@gmail.com', @pwd_hash, 'David Miller', '+1-555-0105', '77 Oak Ave, Denver, CO', 'ADOPTER', 'ACTIVE'),
(6, 'adopter.priya@gmail.com', @pwd_hash, 'Priya Sharma', '+1-555-0106', '33 Maple Rd, Seattle, WA', 'ADOPTER', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- 3. Pets (High quality, curated pet photos with Unsplash CDN)
INSERT INTO pets (id, shelter_id, name, species, breed, age_months, gender, size, location, description, health_status, vaccination_status, spayed_neutered, image_url, status) VALUES
(1, 2, 'Bella', 'Dog', 'Golden Retriever', 24, 'FEMALE', 'LARGE', 'Austin, TX', 'Gentle, friendly, and eager to please. Bella gets along wonderfully with kids and other pets.', 'Fully checked, negative heartworm test', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
(2, 2, 'Milo', 'Dog', 'French Bulldog', 14, 'MALE', 'SMALL', 'Austin, TX', 'Playful, comical, and loves cuddles on the sofa. Needs moderate daily exercise.', 'Healthy respiratory check', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
(3, 2, 'Luna', 'Cat', 'Siamese Mix', 18, 'FEMALE', 'MEDIUM', 'Austin, TX', 'Vocal, affectionate, and deeply intelligent. Loves climbing towers and chasing laser toys.', 'FeLV/FIV negative', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
(4, 3, 'Oliver', 'Cat', 'British Shorthair', 36, 'MALE', 'MEDIUM', 'Denver, CO', 'Placid, handsome gentleman cat with a quiet purr. Enjoys calm sunlit window sills.', 'Dental cleaning completed', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
(5, 3, 'Charlie', 'Dog', 'Border Collie', 16, 'MALE', 'MEDIUM', 'Denver, CO', 'High-energy, hyper-intelligent agility enthusiast. Best suited for an active outdoor family.', 'Excellent stamina & joint health', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1503256207526-0d5d80fa2f47?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
(6, 3, 'Thumper', 'Rabbit', 'Holland Lop', 10, 'MALE', 'SMALL', 'Denver, CO', 'Gentle, inquisitive bunny that loves fresh Timothy hay, romaine lettuce, and gentle head scratches.', 'Vaccinated for RHDV2', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
(7, 2, 'Rio', 'Bird', 'Cockatiel', 12, 'MALE', 'SMALL', 'Austin, TX', 'Cheerful whistler who mimics household tunes and enjoys perching on friendly shoulders.', 'Avian vet check cleared', 'Up to date', FALSE, 'https://images.unsplash.com/photo-1522858547550-345670f99e4b?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
(8, 2, 'Rocky', 'Dog', 'German Shepherd Mix', 30, 'MALE', 'LARGE', 'Austin, TX', 'Loyal guardian and loving family protector. Well trained in basic obedience and leash walking.', 'Microchipped, hip dysplasia screened clear', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1589941013453-ec89f33b5455?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
(9, 3, 'Daisy', 'Dog', 'Beagle', 8, 'FEMALE', 'MEDIUM', 'Denver, CO', 'Curious, happy-go-lucky puppy ready to explore the world with an affectionate family.', 'First puppy series complete', 'Puppy shots up to date', FALSE, 'https://images.unsplash.com/photo-1537151625747-768eb6cf92b2?auto=format&fit=crop&w=800&q=80', 'AVAILABLE'),
(10, 2, 'Shadow', 'Cat', 'Domestic Shorthair', 20, 'MALE', 'MEDIUM', 'Austin, TX', 'Sleek, loving mini-panther who purrs like a motor engine when brushed.', 'FeLV/FIV negative', 'Up to date', TRUE, 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=800&q=80', 'PENDING_REVIEW')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 4. Adoption Applications
INSERT INTO adoption_applications (id, pet_id, adopter_id, living_arrangement, home_type, pet_experience, household_members, adoption_reason, care_commitment, agreement_terms, status, review_notes) VALUES
(1, 1, 4, 'Own house with fenced backyard', 'Single Family Home', 'Owned Golden Retrievers for 10 years previously.', 3, 'Looking for an energetic, gentle companion for our kids.', 'Daily 2-hour exercise, premium diet, bi-annual vet visits.', TRUE, 'APPROVED', 'Fantastic applicant. Spacious yard and extensive breed experience.'),
(2, 2, 5, 'Rented apartment (landlord approved)', 'Apartment / Condo', 'First time dog owner, grew up with family cats.', 2, 'Want a small companion dog to share our cozy home.', 'Committed to positive reinforcement training and daily walks.', TRUE, 'UNDER_REVIEW', 'Application looks promising. Verifying pet deposit approval with landlord.'),
(3, 3, 6, 'Own suburban townhome', 'Townhouse', 'Lifelong cat owner, current home is cat-proofed.', 1, 'In love with Siamese vocalizations and playful temperament.', 'Indoor only home with abundant vertical climbing space.', TRUE, 'PENDING', NULL)
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- 5. Conversations
INSERT INTO conversations (id, application_id, shelter_id, adopter_id) VALUES
(1, 1, 2, 4),
(2, 2, 2, 5),
(3, 3, 2, 6)
ON DUPLICATE KEY UPDATE shelter_id = VALUES(shelter_id);

-- 6. Messages
INSERT INTO messages (id, conversation_id, sender_id, recipient_id, content, is_read) VALUES
(1, 1, 4, 2, 'Hello Happy Paws! We are so excited about Bella. Can we visit this Saturday?', TRUE),
(2, 1, 2, 4, 'Hi Sarah! We would love that. Saturday 11:00 AM works perfectly. Bring the family!', TRUE),
(3, 2, 5, 2, 'Hi! I submitted my application for Milo. Please let me know if you need landlord lease paperwork.', FALSE)
ON DUPLICATE KEY UPDATE content = VALUES(content);

# Entity-Relationship (ER) Diagram
## Online Pet Adoption Platform

The following diagram illustrates the relational data model, cardinalities, primary keys (PK), and foreign keys (FK) implemented in `database/schema.sql`.

```mermaid
erDiagram
    USERS ||--o{ PETS : "shelter lists"
    USERS ||--o{ PETS : "admin reviews"
    USERS ||--o{ ADOPTION_APPLICATIONS : "adopter submits"
    PETS ||--o{ ADOPTION_APPLICATIONS : "receives"
    ADOPTION_APPLICATIONS ||--|| CONVERSATIONS : "has"
    USERS ||--o{ CONVERSATIONS : "shelter participates"
    USERS ||--o{ CONVERSATIONS : "adopter participates"
    CONVERSATIONS ||--o{ MESSAGES : "contains"
    USERS ||--o{ MESSAGES : "sends"
    USERS ||--o{ MESSAGES : "receives"
    USERS ||--o{ AUDIT_LOGS : "triggers"

    USERS {
        BIGINT id PK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR full_name
        VARCHAR phone
        TEXT address
        ENUM role "ADMIN, SHELTER, ADOPTER"
        ENUM status "ACTIVE, INACTIVE"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    PETS {
        BIGINT id PK
        BIGINT shelter_id FK
        VARCHAR name
        VARCHAR species "Dog, Cat, Rabbit, Bird, Other"
        VARCHAR breed
        INT age_months
        ENUM gender "MALE, FEMALE, UNKNOWN"
        ENUM size "SMALL, MEDIUM, LARGE, EXTRA_LARGE"
        VARCHAR location
        TEXT description
        TEXT health_status
        VARCHAR vaccination_status
        BOOLEAN spayed_neutered
        VARCHAR image_url
        ENUM status "PENDING_REVIEW, AVAILABLE, REJECTED, ADOPTED"
        TEXT rejection_reason
        BIGINT reviewed_by FK
        TIMESTAMP reviewed_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    ADOPTION_APPLICATIONS {
        BIGINT id PK
        BIGINT pet_id FK
        BIGINT adopter_id FK
        VARCHAR living_arrangement
        VARCHAR home_type
        TEXT pet_experience
        INT household_members
        TEXT adoption_reason
        TEXT care_commitment
        BOOLEAN agreement_terms
        ENUM status "PENDING, UNDER_REVIEW, APPROVED, REJECTED"
        TEXT review_notes
        TIMESTAMP reviewed_at
        TIMESTAMP submitted_at
        TIMESTAMP updated_at
    }

    CONVERSATIONS {
        BIGINT id PK
        BIGINT application_id FK, UK
        BIGINT shelter_id FK
        BIGINT adopter_id FK
        TIMESTAMP created_at
        TIMESTAMP last_message_at
    }

    MESSAGES {
        BIGINT id PK
        BIGINT conversation_id FK
        BIGINT sender_id FK
        BIGINT recipient_id FK
        TEXT content
        BOOLEAN is_read
        TIMESTAMP sent_at
    }

    SYSTEM_SETTINGS {
        VARCHAR setting_key PK
        TEXT setting_value
        VARCHAR description
        BOOLEAN is_editable
        TIMESTAMP updated_at
    }

    AUDIT_LOGS {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR action
        VARCHAR entity_type
        BIGINT entity_id
        TEXT details
        VARCHAR ip_address
        TIMESTAMP created_at
    }
```

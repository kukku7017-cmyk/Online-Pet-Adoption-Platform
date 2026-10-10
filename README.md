# Online Pet Adoption Platform (PawsConnect)
### Review 1 (Pre-Midterm Submission) — Full-Stack Java Web Application

A full-stack, enterprise-grade Java web application designed for ethical, transparent animal rescue adoption. Features role-based access for Administrators, Shelters, and Adopters, backed by Jakarta Servlets 6.0, JDBC, MySQL 8+, and a modern Neumorphic UI design language.

---

## 1. Technology Stack

- **Frontend:** HTML5, CSS3, Vanilla JavaScript (ES6+), Fetch API, Modern Neumorphism design system, mobile-first responsive design.
- **Backend:** Java 17+, Jakarta EE 10 / Jakarta Servlets 6.0 (`jakarta.servlet.*`), JDBC, HikariCP Connection Pooling, jBCrypt 0.4, Jackson 2.17 JSON.
- **Web Server:** Apache Tomcat 10.1+ (Deployable WAR or embedded via Maven runner).
- **Database:** MySQL 8.0+ (InnoDB, Foreign Key Constraints, Transactions, Parameterized Queries) + In-memory H2 fallback for instant automated test execution.
- **Build System:** Apache Maven 3.9+.

---

## 2. Review 1 Rubric Compliance Checklist

| Rubric Criterion | Target | Demonstrated Implementation & Source Files | Status |
|---|:---:|---|:---:|
| **A. Problem Understanding & Solution Design** | **8 / 8** | Requirements document ([requirements.md](file:///d:/Pet%20Adoption/docs/requirements.md)), System architecture ([system-design.md](file:///d:/Pet%20Adoption/docs/system-design.md)), Mermaid ER Diagram ([er-diagram.md](file:///d:/Pet%20Adoption/docs/er-diagram.md)), Use Case & Sequence Diagrams ([use-case-diagram.md](file:///d:/Pet%20Adoption/docs/use-case-diagram.md)), REST API Documentation ([api-documentation.md](file:///d:/Pet%20Adoption/docs/api-documentation.md)). | ✅ **Verified** |
| **B. Core Java Concepts** | **10 / 10** | **OOP:** Domain models ([User.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/model/User.java), [Pet.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/model/Pet.java), [AdoptionApplication.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/model/AdoptionApplication.java)), DAO interfaces & implementations.<br>**Collections:** `List`, `Map`, `Set`, stream filtering.<br>**Exception Handling:** Hierarchical custom exceptions ([AppException](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/exception/AppException.java), [AuthenticationException](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/exception/AuthenticationException.java), [ValidationException](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/exception/ValidationException.java)), try-with-resources.<br>**Concurrency:** Bounded `ThreadPoolExecutor` in [NotificationService.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/service/NotificationService.java) for asynchronous non-blocking logging & notifications. | ✅ **Verified** |
| **C. Database Integration (JDBC)** | **8 / 8** | 100% Parameterized queries via `PreparedStatement` in [PetDAOImpl.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/dao/PetDAOImpl.java), [UserDAOImpl.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/dao/UserDAOImpl.java).<br>ACID Multi-table atomic transaction in [AdoptionApplicationDAOImpl.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/dao/AdoptionApplicationDAOImpl.java) (`approveApplicationAndAdoptPet`).<br>Production MySQL schema ([schema.sql](file:///d:/Pet%20Adoption/database/schema.sql)), Seed data ([seed.sql](file:///d:/Pet%20Adoption/database/seed.sql)), Reset script ([reset.sql](file:///d:/Pet%20Adoption/database/reset.sql)). | ✅ **Verified** |
| **D. Servlets & Web Integration** | **7 / 7** | Dedicated Jakarta Servlet controllers: [AuthServlet.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/controller/AuthServlet.java), [PetServlet.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/controller/PetServlet.java), [ApplicationServlet.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/controller/ApplicationServlet.java), [AdminServlet.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/controller/AdminServlet.java), [MessageServlet.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/controller/MessageServlet.java).<br>[AuthFilter.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/filter/AuthFilter.java) for RBAC, [CsrfFilter.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/filter/CsrfFilter.java) for CSRF defense, [EncodingFilter.java](file:///d:/Pet%20Adoption/backend/src/main/java/com/petadoption/filter/EncodingFilter.java) for UTF-8.<br>Unified client integration ([api.js](file:///d:/Pet%20Adoption/frontend/js/api.js)). | ✅ **Verified** |

---

## 3. Project Directory Structure

```
OnlinePetAdoptionPlatform/
├── frontend/                     # Modern Neumorphic Web Frontend
│   ├── index.html                # Landing page with featured pets, filters, and stats
│   ├── login.html                # Neumorphic login with credentials helper
│   ├── register.html             # Role-based registration (Adopter / Shelter)
│   ├── pets.html                 # Search catalog with multi-dimensional filters
│   ├── pet-details.html          # Medical disclosures & application trigger
│   ├── about.html                # Mission & architectural principles
│   ├── contact.html              # Support & FAQ
│   ├── dashboards/
│   │   ├── admin.html            # User management, moderation queue, analytics, settings
│   │   ├── shelter.html          # Inventory management, application reviews, chat
│   │   └── adopter.html          # Application tracker, profile, and shelter messages
│   ├── css/
│   │   ├── style.css             # Base styles, typography tokens, layouts
│   │   ├── neumorphism.css       # Tactile raised cards, inset inputs, extruded buttons
│   │   └── responsive.css        # Breakpoints & collapsible dashboard layouts
│   └── js/
│       ├── api.js                # Fetch API wrapper with CSRF token interceptor
│       ├── auth.js               # Session verification & RBAC redirection
│       ├── ui.js                 # Toasts, modals, formatting helpers
│       ├── validation.js         # Client-side input validation
│       ├── pets.js               # Dynamic catalog search & pagination
│       ├── applications.js       # Questionnaire form handler
│       ├── admin.js              # Admin portal operations
│       ├── shelter.js            # Shelter portal operations
│       └── adopter.js            # Adopter portal operations
├── backend/
│   ├── pom.xml                   # Maven build descriptor (Java 17, Jakarta 6.0)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/petadoption/
│   │   │   │   ├── config/       # DatabaseConfig, AppContextListener, ApplicationLauncher
│   │   │   │   ├── controller/   # AuthServlet, PetServlet, ApplicationServlet, AdminServlet...
│   │   │   │   ├── dao/          # UserDAO, PetDAO, AdoptionApplicationDAO, MessageDAO...
│   │   │   │   ├── exception/    # Custom exception hierarchy
│   │   │   │   ├── filter/       # AuthFilter, CsrfFilter, EncodingFilter
│   │   │   │   ├── model/        # Domain entities (User, Pet, Application, Message...)
│   │   │   │   ├── service/      # Business logic & NotificationService (ExecutorService)
│   │   │   │   └── util/         # PasswordUtil (BCrypt), JsonUtil, ValidationUtil, SecurityUtil
│   │   │   ├── resources/
│   │   │   │   ├── application.properties
│   │   │   │   └── application.properties.example
│   │   │   └── webapp/
│   │   │       ├── WEB-INF/web.xml # Jakarta Servlet 6.0 deployment descriptor
│   │   │       └── ... (Synchronized frontend static assets)
│   │   └── test/
│   │       └── java/com/petadoption/ # Automated JUnit 5 integration & unit test suite
├── database/
│   ├── schema.sql                # MySQL 8+ Relational Schema
│   ├── seed.sql                  # Demonstration seed records with BCrypt passwords
│   └── reset.sql                 # Safe development reset script
├── docs/
│   ├── requirements.md           # Software Requirements Specification (SRS)
│   ├── system-design.md          # Architecture & Core Java rubric documentation
│   ├── er-diagram.md             # Mermaid Entity-Relationship diagram
│   ├── use-case-diagram.md       # Mermaid Use Case and Sequence diagrams
│   ├── api-documentation.md      # RESTful API specifications
│   └── test-cases.md             # Test cases matrix
├── README.md
└── .gitignore
```

---

## 4. Demonstration Credentials

All seeded demonstration accounts use the password: `Password@123` (hashed with BCrypt).

| Role | Email | Password | Description |
|---|---|---|---|
| **Administrator** | `admin@petadoption.com` | `Password@123` | Full administrative control, pet listing moderation, user management, analytics. |
| **Shelter Partner** | `paws@shelter.org` | `Password@123` | Happy Paws Rescue. Manages pet listings, reviews applications, chats with adopters. |
| **Shelter Partner** | `haven@shelter.org` | `Password@123` | Safe Haven Sanctuary. Additional shelter account. |
| **Adopter** | `adopter.sarah@gmail.com` | `Password@123` | Sarah Jenkins. Has approved adoption for Bella. |
| **Adopter** | `adopter.david@gmail.com` | `Password@123` | David Miller. Has under-review application for Milo. |
| **Adopter** | `adopter.priya@gmail.com` | `Password@123` | Priya Sharma. Has pending application for Luna. |

---

## 5. Setup & Running Instructions

### Prerequisites
- **JDK 17 or later** (Java 24 installed and verified).
- **Apache Maven 3.9+** (installed and verified).
- **MySQL 8.0+** (Optional: the backend includes an automated in-memory H2 fallback for instant standalone evaluation).

### Option A: Running Standalone via Maven (Recommended)
This starts an embedded Apache Tomcat 10.1 server on port 8080 serving both the frontend and Jakarta Servlets with zero CORS configuration:

```powershell
cd "d:\Pet Adoption\backend"
mvn compile exec:java
```

Once started, open your browser and navigate to:
```
http://localhost:8080/
```

### Option B: Deploying to Standalone Tomcat 10.1
To build the deployable `.war` file:

```powershell
cd "d:\Pet Adoption\backend"
mvn package
```

The resulting WAR file is generated at:
```
d:\Pet Adoption\backend\target\pet-adoption-platform.war
```
Copy this file into your Apache Tomcat 10.1 `webapps/` directory and start Tomcat.

### Option C: Database Setup in MySQL (Production)
1. Log in to MySQL:
   ```bash
   mysql -u root -p
   ```
2. Execute the schema script:
   ```sql
   SOURCE d:/Pet Adoption/database/schema.sql;
   ```
3. Populate seed demonstration data:
   ```sql
   SOURCE d:/Pet Adoption/database/seed.sql;
   ```
4. Configure database credentials in `backend/src/main/resources/application.properties` or set environment variables:
   - `DB_URL=jdbc:mysql://localhost:3306/pet_adoption_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `DB_USER=root`
   - `DB_PASSWORD=your_password`

---

## 6. Running Automated Tests

Run the full JUnit 5 integration and unit test suite:

```powershell
cd "d:\Pet Adoption\backend"
mvn test
```

### Verified Test Results
- **15 Tests Run, 0 Failures, 0 Errors, 0 Skipped.**
  1. `UserDaoAndServiceTest`: Registration, duplicate email check, BCrypt hash verification, admin guard.
  2. `PetServiceTest`: Listing creation, moderation approval/rejection with notes, public filtering, IDOR check.
  3. `AdoptionTransactionTest`: Application submission, duplicate prevention, atomic multi-step adoption approval.
  4. `ConcurrencyNotificationTest`: Bounded `ThreadPoolExecutor` async task execution and graceful shutdown.

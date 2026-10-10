# Software Requirements Specification (SRS)
## Online Pet Adoption Platform — Review 1 (Pre-Midterm Submission)

### 1. Problem Statement
Pet adoption in modern communities is often fragmented across dislocated animal shelters, informal social media posts, and physical paperwork. This causes:
- Low visibility for rescued animals needing urgent adoption.
- Disjointed communication between prospective adopters and shelter rescue managers.
- Inefficient, error-prone manual screening of applicants.
- Lack of centralized moderation, verification, and ethical safeguards against fraudulent listings.

The **Online Pet Adoption Platform** provides a centralized, secure, full-stack Java web platform connecting Shelters, Adopters, and Platform Administrators under a unified, role-based ecosystem.

---

### 2. Stakeholders & Their Needs

1. **Adopters (Public Users & Registered Pet Seekers)**
   - Need transparent search & filter options across species, breed, age, gender, size, and location.
   - Require verified health, vaccination, and spay/neuter disclosures before applying.
   - Need a structured, standardized adoption application form with living arrangement and pet experience details.
   - Demand real-time visibility into application status transitions (`PENDING`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`).
   - Require direct, record-kept communication with shelter managers regarding their applications.

2. **Shelters (Animal Welfare Organizations & Rescues)**
   - Need dedicated portal to list, update, and manage rescued animals with photos and medical history.
   - Require a streamlined dashboard to review adoption applications, assess suitability, and prevent duplicate applications.
   - Need atomic adoption approval workflow to prevent race conditions (approving one applicant marks the pet adopted and automatically resolves conflicting applications).
   - Demand private, traceable communication channels with prospective adopters.
   - Need platform metrics on listings, active reviews, and completed adoptions.

3. **Platform Administrators**
   - Require oversight over pet listings via a moderation queue (Approve / Reject with logged reasons).
   - Need user lifecycle management (activate, deactivate, role inspection, guard against deleting the last admin).
   - Require platform-wide analytics derived directly from MySQL database transactions.
   - Need system settings configuration (site name, contact email, application thresholds).

---

### 3. Functional Requirements (FR)

#### FR-1: Authentication & Authorization
- **FR-1.1**: Public user registration for `ADOPTER` and `SHELTER` roles. Disallow self-registration as `ADMIN`.
- **FR-1.2**: Secure authentication using BCrypt salted password hashing.
- **FR-1.3**: Role-Based Access Control (RBAC) enforced via Jakarta Servlet Filters on all protected `/api/*` endpoints.
- **FR-1.4**: Session-based state management with automatic timeout and secure session invalidation upon logout.
- **FR-1.5**: In-flight CSRF token validation on state-modifying requests (`POST`, `PUT`, `DELETE`).

#### FR-2: Pet Management & Browsing
- **FR-2.1**: Public browsing with multi-dimensional filtering (species, breed, age group, gender, size, location).
- **FR-2.2**: Only approved (`AVAILABLE`) pets appear in public browsing.
- **FR-2.3**: Shelters can create, update, and monitor their own pet listings with photo links/uploads.
- **FR-2.4**: Admin listing moderation workflow: `PENDING_REVIEW` -> `AVAILABLE` or `REJECTED` (with mandatory rejection reason).
- **FR-2.5**: Shelters can resubmit rejected pets after modifying details.

#### FR-3: Adoption Application Workflow
- **FR-3.1**: Registered adopters can submit an adoption application with comprehensive questions (housing, experience, reason, care commitment, legal terms).
- **FR-3.2**: Enforce unique constraint: an adopter cannot submit duplicate applications for the same pet.
- **FR-3.3**: Shelters review applications with status transitions: `PENDING` -> `UNDER_REVIEW` -> `APPROVED` / `REJECTED`.
- **FR-3.4**: Atomic Transaction: Approving an application automatically marks the pet status as `ADOPTED` and rejects other pending applications for that pet.

#### FR-4: Communication System
- **FR-4.1**: Automatic creation of a dedicated conversation linked to each submitted adoption application.
- **FR-4.2**: Message exchange between the pet's shelter and the applying adopter with timestamping and unread flags.
- **FR-4.3**: Strict backend ownership verification to prevent Insecure Direct Object References (IDOR).

#### FR-5: Administration, Analytics & Settings
- **FR-5.1**: User management with activation/deactivation toggles and last-admin protection.
- **FR-5.2**: Database-derived analytics (total users, shelters, adopters, pets by status, adoption success rates).
- **FR-5.3**: Editable platform settings persisted in MySQL (`system_settings`).
- **FR-5.4**: Comprehensive audit logging for sensitive administrative actions.

---

### 4. Non-Functional Requirements (NFR)

- **NFR-1 (Security)**: Passwords hashed with BCrypt. PreparedStatement for 100% of SQL interactions to prevent SQL Injection. XSS output sanitization. In-memory CSRF token tokens.
- **NFR-2 (Performance)**: Connection pooling via HikariCP for sub-50ms database connection latency. Indexed queries for filtering and foreign key joins.
- **NFR-3 (Reliability & Concurrency)**: ACID transaction management for multi-table state transitions. Bounded thread pool (`ExecutorService`) for non-blocking notification and audit logging tasks.
- **NFR-4 (UI/UX Aesthetics)**: Modern Neumorphic design language, responsive mobile-first layout, rich tactile cards, subtle inset inputs, and WCAG AA contrast compliance.
- **NFR-5 (Maintainability)**: Clear separation of concerns: Model-DAO-Service-Controller architecture adhering to strict Object-Oriented Principles.

---

### 5. Role-Wise Permissions Matrix

| Feature / Resource | Public Guest | Adopter | Shelter | Administrator |
|---|:---:|:---:|:---:|:---:|
| Browse Approved Pets | ✅ | ✅ | ✅ | ✅ |
| View Pet Details | ✅ | ✅ | ✅ | ✅ |
| Register Account (Adopter/Shelter) | ✅ | ❌ | ❌ | ❌ |
| Login / Logout | ✅ | ✅ | ✅ | ✅ |
| Submit Adoption Application | ❌ | ✅ | ❌ | ❌ |
| Track My Applications | ❌ | ✅ | ❌ | ❌ |
| Manage Adopter Profile | ❌ | ✅ | ❌ | ❌ |
| Create / Edit Own Pets | ❌ | ❌ | ✅ | ❌ |
| View / Review Received Applications | ❌ | ❌ | ✅ (Own Pets) | ❌ |
| Application Messaging | ❌ | ✅ (Own Apps) | ✅ (Own Pets) | ❌ |
| Moderate Pet Listings | ❌ | ❌ | ❌ | ✅ |
| Manage Users (Activate/Deactivate) | ❌ | ❌ | ❌ | ✅ |
| View Platform Analytics | ❌ | ❌ | ❌ | ✅ |
| Edit System Settings | ❌ | ❌ | ❌ | ✅ |

---

### 6. Main Workflows

1. **Adoption Journey**:
   Guest registers as Adopter -> Browses available pets -> Fills out comprehensive adoption questionnaire -> Application enters `PENDING` -> Shelter reviews questionnaire (`UNDER_REVIEW`) -> Shelter & Adopter message in application chat -> Shelter approves application -> Pet becomes `ADOPTED` and other pending applications for that pet are automatically closed.

2. **Listing & Moderation Journey**:
   Shelter adds new pet -> Pet enters `PENDING_REVIEW` -> Admin reviews listing -> Admin approves -> Pet becomes `AVAILABLE` in public catalog. If rejected, Admin enters rejection reason -> Shelter reviews feedback and resubmits.

3. **Administrative User Management Journey**:
   Admin accesses User Management -> Filters by role/status -> Can deactivate spam/misbehaving accounts -> Prevented from disabling the sole active Admin account.

---

### 7. Assumptions & Limitations
- **Assumptions**: Users possess standard modern browsers with JavaScript enabled; backend is hosted on a Jakarta EE 10 compliant Servlet container (Tomcat 10.1+); database is MySQL 8+.
- **Limitations for Review 1**: Real-time WebSockets are not required (simulated via responsive REST polling); payment gateways are out of scope for non-profit adoption processing.

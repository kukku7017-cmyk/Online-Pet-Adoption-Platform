# System Architecture & Technical Design Document
## Online Pet Adoption Platform — Review 1

### 1. Architectural Overview
The platform follows an N-Tier Architecture cleanly separating the Presentation Layer (Frontend), Controller Layer (Jakarta Servlets), Service Layer (Business Logic), Data Access Layer (JDBC DAOs), and Database Layer (MySQL 8+).

```
+-------------------------------------------------------------------------+
|                  Presentation Layer (Vanilla HTML5 / CSS3 / JS)          |
|  - Modern Neumorphic UI Design                                           |
|  - Mobile-First Responsive Layout                                        |
|  - Fetch API with Centralized Error Handling & CSRF Interceptors         |
+------------------------------------+------------------------------------+
                                     | HTTP / REST (JSON)
+------------------------------------v------------------------------------+
|                  Filter & Controller Layer (Jakarta Servlets 6.0)        |
|  - EncodingFilter (UTF-8)                                                |
|  - AuthFilter (Session & Role-Based Access Control)                      |
|  - CsrfFilter (Anti-CSRF Token Validation)                               |
|  - Dedicated Servlet Controllers (Auth, Pet, Application, Admin, etc.)   |
+------------------------------------+------------------------------------+
                                     | Service Calls
+------------------------------------v------------------------------------+
|                  Service & Business Logic Layer (Java 17+)               |
|  - UserService, PetService, AdoptionService, MessageService             |
|  - Security & Password Hashing (BCrypt)                                  |
|  - Validation & Domain Rules                                             |
|  - Asynchronous Concurrency Tasks (Bounded ExecutorService)              |
+------------------------------------+------------------------------------+
                                     | DAO Interface Contracts
+------------------------------------v------------------------------------+
|                  Data Access Layer (JDBC DAOs & HikariCP)                |
|  - UserDAO, PetDAO, AdoptionApplicationDAO, MessageDAO, SystemSettingDAO |
|  - PreparedStatement (100% Parameterized Queries)                        |
|  - ACID Transactions (commit/rollback for multi-step mutations)         |
|  - Try-with-Resources Connection & Statement Lifecycle Management        |
+------------------------------------+------------------------------------+
                                     | TCP / JDBC
+------------------------------------v------------------------------------+
|                  Relational Persistence Layer (MySQL 8.0+ / InnoDB)      |
|  - Foreign Key Constraints & Cascade/Restrict Rules                      |
|  - Unique Constraints (e.g., prevent duplicate pet applications)         |
|  - Composite & Single-Column B-Tree Indexes                              |
+-------------------------------------------------------------------------+
```

---

### 2. Core Java Concepts Demonstrated

#### A. Object-Oriented Programming (OOP)
- **Encapsulation**: Domain models (`User`, `Pet`, `AdoptionApplication`, `Message`, `SystemSetting`, `AuditLog`) maintain private members with validated public getters, setters, and builder/constructor initializers.
- **Abstraction**: High-level services interface strictly with DAO interfaces (`UserDAO`, `PetDAO`, `AdoptionApplicationDAO`, etc.) rather than concrete database implementations.
- **Polymorphism**: The DAO contract pattern allows seamless interchange of JDBC implementations (including test mock/in-memory data sources).
- **Domain Modeling**: Rich domain representations with typed enums for `Role`, `UserStatus`, `PetStatus`, `Gender`, `PetSize`, and `ApplicationStatus`.

#### B. Collections Framework
- **`List<T>`**: Used extensively to retrieve, sort, and paginate collections of pets, applications, audit logs, and message threads.
- **`Map<String, Object>`**: Leveraged in `AnalyticsService` for dynamic aggregation of dashboard statistics, and in JSON serialization for flexible parameter maps.
- **`Set<T>`**: Utilized for role permissions verification, unique category extraction, and de-duplicating participant IDs in conversation retrieval.
- **Stream API**: Applied for functional data transformations, sorting pets by creation date, and status filtering.

#### C. Robust Exception Handling
- **Hierarchical Custom Exceptions**:
  - `AppException` (Base RuntimeException)
  - `AuthenticationException` (Invalid credentials, expired session)
  - `AuthorizationException` (Unauthorized role access, IDOR violation)
  - `ResourceNotFoundException` (Requested pet/user/application does not exist)
  - `ValidationException` (Malformed inputs, failed business constraints)
  - `DatabaseException` (SQL query failures, connection exhaustion)
- **Try-With-Resources**: Guarantees zero resource leaks for `Connection`, `PreparedStatement`, and `ResultSet` instances.
- **Defensive Error Boundaries**: Exceptions are caught at the Servlet controller layer, logged on the server with stack traces, and transformed into sanitized, user-friendly JSON error payloads (`{ "success": false, "message": "..." }`) without exposing internal SQL or server internals.

#### D. Concurrency & Multithreading
- **Bounded Thread Pool**: Rather than spawning unbounded threads per request, the system initializes a bounded `ThreadPoolExecutor` via `NotificationService`:
  ```java
  private final ExecutorService asyncExecutor = new ThreadPoolExecutor(
      2, 4, 60L, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(100),
      new ThreadFactoryBuilder().setNameFormat("pet-async-worker-%d").build(),
      new ThreadPoolExecutor.CallerRunsPolicy()
  );
  ```
- **Asynchronous Workloads**: Used for non-blocking audit logging and background application notification dispatching without delaying the client's HTTP response.
- **Graceful Lifecycle Management**: The thread pool is safely shutdown inside a `ServletContextListener` / JVM shutdown hook using `awaitTermination`.

---

### 3. Transaction Management & Business Integrity

Multi-step database mutations use explicit JDBC transaction demarcation (`connection.setAutoCommit(false)`):

#### The Atomic Adoption Approval Workflow:
1. Begin transaction on the JDBC connection.
2. Verify that the logged-in shelter owns the pet linked to the application.
3. Update `adoption_applications` status for the target application to `APPROVED`.
4. Update `pets` status to `ADOPTED`.
5. Update all other `PENDING` or `UNDER_REVIEW` applications for that pet to `REJECTED` with the note: *"Pet has been adopted by another applicant."*
6. Record an `audit_logs` entry.
7. Commit transaction (`connection.commit()`).
8. If any step fails or throws an `SQLException`, rollback immediately (`connection.rollback()`).

---

### 4. Authentication & Security Architecture

1. **Password Hashing**: Salting and hashing powered by jBCrypt (`BCrypt.hashpw(password, BCrypt.gensalt(12))`). Plaintext passwords never touch persistence.
2. **Session Security**: Session fixation defense by calling `request.changeSessionId()` on successful login. Sessions store only the minimal `User` principal.
3. **Role-Based Authorization Filter (`AuthFilter`)**:
   - Inspects URI prefix against role requirements (`/api/admin/*` -> `ADMIN`, `/api/shelter/*` -> `SHELTER`, `/api/adopter/*` -> `ADOPTER`).
   - Rejects unauthenticated requests with `401 Unauthorized` and unauthorized roles with `403 Forbidden`.
4. **IDOR & Ownership Defense**:
   - Shelters can only edit pets where `shelter_id == currentUser.id`.
   - Adopters can only view applications where `adopter_id == currentUser.id`.
   - Conversations are restricted strictly to the shelter owner and applicant.
5. **CSRF Mitigation**:
   - A cryptographically random CSRF token is issued in the user's session and exposed via `/api/auth/me`.
   - All state-changing mutating requests (`POST`, `PUT`, `DELETE`) must provide the token in the `X-CSRF-Token` header.

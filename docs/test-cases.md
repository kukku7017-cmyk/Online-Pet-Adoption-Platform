# Test Plan & Traceability Matrix (Review 1)
## Online Pet Adoption Platform

This document outlines the test cases verifying functional correctness, authorization boundaries, database ACID compliance, and UI responsiveness.

| Test ID | Module | Scenario / Description | Pre-conditions | Test Steps | Expected Result |
|---|---|---|---|---|---|
| **AUTH-01** | Authentication | User Registration with valid Adopter details | Unique email provided | Send POST `/api/auth/register` with role `ADOPTER` | Status 201 Created; User record saved with BCrypt password hash |
| **AUTH-02** | Authentication | Prevent self-registration as ADMIN | Public request | Send POST `/api/auth/register` with role `ADMIN` | Status 400 Bad Request; Registration rejected |
| **AUTH-03** | Authentication | Reject duplicate email registration | Email already exists | Send POST `/api/auth/register` with existing email | Status 409 Conflict; Helpful error returned |
| **AUTH-04** | Authentication | Successful login & Session creation | Valid registered user | Send POST `/api/auth/login` with correct password | Status 200 OK; Session established; CSRF token generated |
| **AUTH-05** | Authentication | Reject invalid credentials | Incorrect password | Send POST `/api/auth/login` with wrong password | Status 401 Unauthorized; No session created |
| **AUTH-06** | Authentication | Session invalidation upon logout | Active user session | Send POST `/api/auth/logout` | Session destroyed; Subsequent requests return 401 |
| **RBAC-01** | Authorization | Adopter attempting to access Admin endpoint | Logged in as `ADOPTER` | Send GET `/api/admin/users` | Status 403 Forbidden |
| **RBAC-02** | Authorization | Shelter attempting to edit another shelter's pet | Logged in as `SHELTER` (ID 2) | Send POST `/api/pets/update` targeting Pet of Shelter (ID 3) | Status 403 Forbidden / IDOR violation rejected |
| **PET-01** | Pet Listings | Shelter creates new pet listing | Logged in as `SHELTER` | Send POST `/api/pets/create` with valid fields | Status 201 Created; Initial status is `PENDING_REVIEW` |
| **PET-02** | Pet Listings | Public browsing filters only approved pets | Multiple pets in various statuses | Send GET `/api/pets/public` | Returns only pets with status `AVAILABLE`; `PENDING_REVIEW` hidden |
| **PET-03** | Moderation | Admin approves pet listing | Pet in `PENDING_REVIEW` | Send POST `/api/admin/pets/moderate` with status `AVAILABLE` | Status updated in DB; Pet now appears in public catalog |
| **PET-04** | Moderation | Admin rejects listing with feedback | Pet in `PENDING_REVIEW` | Send POST `/api/admin/pets/moderate` with status `REJECTED` & reason | Status set to `REJECTED`; Shelter sees reason |
| **APP-01** | Applications | Adopter submits application | Pet is `AVAILABLE` | Send POST `/api/applications/submit` | Status 201 Created; App status `PENDING`; Conversation auto-created |
| **APP-02** | Applications | Prevent duplicate application for same pet | Adopter already applied | Send POST `/api/applications/submit` again for same pet | Status 409 Conflict; Unique DB constraint enforced |
| **APP-03** | Applications | Atomic adoption approval | Multiple applications on pet | Shelter updates application status to `APPROVED` | Target app becomes `APPROVED`; Pet becomes `ADOPTED`; Other apps for this pet become `REJECTED` |
| **MSG-01** | Messaging | Shelter and Adopter exchange messages | Active conversation | Send POST `/api/messages/send` | Status 201 Created; Message saved with sender/recipient |
| **MSG-02** | Messaging | Unauthorized user access to conversation | User not participant | Send GET `/api/messages/thread?conversationId=X` | Status 403 Forbidden |
| **ADM-01** | Administration | Prevent removing last active admin | Only 1 active admin exists | Admin attempts to deactivate their own account | Status 400 Bad Request; Action blocked |
| **UI-01** | Frontend | Responsive Neumorphic Design & Navigation | Desktop / Mobile viewport | Resize viewport and toggle mobile navigation menu | Smooth transitions; No horizontal overflow; Clean tactile neumorphic shadows |
| **UI-02** | Frontend | Real-time Search and Filter Controls | Available pets listed | Type in search bar, change species select | List updates dynamically via Fetch API without page reload |

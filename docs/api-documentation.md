# RESTful API Documentation
## Online Pet Adoption Platform

All API requests and responses use `application/json; charset=UTF-8` unless handling `multipart/form-data` uploads.
Protected mutating requests (`POST`, `PUT`, `DELETE`) require the `X-CSRF-Token` header.

---

### 1. Authentication Endpoints (`/api/auth/*`)

#### `POST /api/auth/register`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "Password@123",
    "fullName": "Jane Doe",
    "phone": "+1-555-0199",
    "address": "123 Maple Street",
    "role": "ADOPTER"
  }
  ```
  *(Note: Role must be either `ADOPTER` or `SHELTER`. Registration as `ADMIN` is rejected with `400 Bad Request`.)*
- **Response 201 Created**:
  ```json
  {
    "success": true,
    "message": "Registration successful. You can now log in."
  }
  ```

#### `POST /api/auth/login`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "Password@123"
  }
  ```
- **Response 200 OK**:
  ```json
  {
    "success": true,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Jane Doe",
      "role": "ADOPTER",
      "status": "ACTIVE"
    },
    "csrfToken": "b4a8e9..."
  }
  ```

#### `POST /api/auth/logout`
- **Access**: Authenticated
- **Response 200 OK**:
  ```json
  { "success": true, "message": "Successfully logged out" }
  ```

#### `GET /api/auth/me`
- **Access**: Public / Authenticated
- **Response 200 OK**:
  ```json
  {
    "authenticated": true,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Jane Doe",
      "role": "ADOPTER"
    },
    "csrfToken": "b4a8e9..."
  }
  ```

---

### 2. Pet Endpoints (`/api/pets/*`)

#### `GET /api/pets/public`
- **Access**: Public
- **Query Parameters**:
  - `species`: Filter by species (Dog, Cat, Rabbit, Bird, etc.)
  - `breed`: Substring search
  - `gender`: MALE, FEMALE
  - `size`: SMALL, MEDIUM, LARGE, EXTRA_LARGE
  - `query`: Keyword search across name/breed/location
  - `page`: Page index (1-based, default 1)
  - `limit`: Records per page (default 9)
- **Response 200 OK**:
  ```json
  {
    "success": true,
    "pets": [
      {
        "id": 1,
        "name": "Bella",
        "species": "Dog",
        "breed": "Golden Retriever",
        "ageMonths": 24,
        "gender": "FEMALE",
        "size": "LARGE",
        "location": "Springfield",
        "imageUrl": "https://...",
        "shelterName": "Happy Paws Rescue",
        "status": "AVAILABLE"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 3,
      "totalRecords": 25
    }
  }
  ```

#### `GET /api/pets/detail?id={petId}`
- **Access**: Public
- **Response 200 OK**: Full pet details including description, health status, vaccination status, and shelter details.

#### `POST /api/pets/create`
- **Access**: Role `SHELTER`
- **Request Body**:
  ```json
  {
    "name": "Rocky",
    "species": "Dog",
    "breed": "Boxer Mix",
    "ageMonths": 18,
    "gender": "MALE",
    "size": "MEDIUM",
    "location": "Boston, MA",
    "description": "Energetic, playful pup who loves outdoor walks.",
    "healthStatus": "Fully vaccinated, dewormed.",
    "vaccinationStatus": "Up to Date",
    "spayedNeutered": true,
    "imageUrl": "https://..."
  }
  ```
- **Response 201 Created**:
  ```json
  { "success": true, "message": "Pet listed successfully and queued for review.", "petId": 12 }
  ```

#### `GET /api/pets/my-listings`
- **Access**: Role `SHELTER`
- **Response 200 OK**: List of all pets created by the logged-in shelter with their current status (`AVAILABLE`, `PENDING_REVIEW`, `REJECTED`, `ADOPTED`).

---

### 3. Adoption Application Endpoints (`/api/applications/*`)

#### `POST /api/applications/submit`
- **Access**: Role `ADOPTER`
- **Request Body**:
  ```json
  {
    "petId": 1,
    "livingArrangement": "Own House with Fenced Yard",
    "homeType": "Single Family Home",
    "petExperience": "Had dogs for 8 years in the past.",
    "householdMembers": 3,
    "adoptionReason": "Looking for a loyal family companion.",
    "careCommitment": "Daily 2-hour exercise and vet visits.",
    "agreementTerms": true
  }
  ```
- **Response 201 Created**:
  ```json
  { "success": true, "message": "Adoption application submitted successfully.", "applicationId": 5 }
  ```

#### `GET /api/applications/my`
- **Access**: Role `ADOPTER`
- **Response 200 OK**: List of applications submitted by the logged-in adopter.

#### `GET /api/applications/shelter`
- **Access**: Role `SHELTER`
- **Response 200 OK**: List of applications submitted for pets belonging to the logged-in shelter.

#### `POST /api/applications/review`
- **Access**: Role `SHELTER`
- **Request Body**:
  ```json
  {
    "applicationId": 5,
    "status": "APPROVED",
    "reviewNotes": "Excellent home environment and experience."
  }
  ```
  *(Status options: `UNDER_REVIEW`, `APPROVED`, `REJECTED`. Approving triggers atomic adoption completion.)*
- **Response 200 OK**:
  ```json
  { "success": true, "message": "Application status updated successfully." }
  ```

---

### 4. Admin Management Endpoints (`/api/admin/*`)

#### `GET /api/admin/users`
- **Access**: Role `ADMIN`
- **Response 200 OK**: Complete user directory with role and status indicators.

#### `POST /api/admin/user-status`
- **Access**: Role `ADMIN`
- **Request Body**: `{ "userId": 3, "status": "INACTIVE" }`
- **Protection**: Guaranteed check prevents deactivating the last active `ADMIN`.

#### `GET /api/admin/pets/pending`
- **Access**: Role `ADMIN`
- **Response 200 OK**: List of submitted pet listings awaiting moderation.

#### `POST /api/admin/pets/moderate`
- **Access**: Role `ADMIN`
- **Request Body**:
  ```json
  {
    "petId": 8,
    "status": "AVAILABLE",
    "rejectionReason": null
  }
  ```

#### `GET /api/admin/analytics`
- **Access**: Role `ADMIN`
- **Response 200 OK**:
  ```json
  {
    "success": true,
    "totalUsers": 28,
    "totalShelters": 6,
    "totalAdopters": 21,
    "totalPets": 42,
    "pendingPetListings": 3,
    "pendingApplications": 7,
    "approvedAdoptions": 14
  }
  ```

#### `GET /api/admin/settings` & `POST /api/admin/settings`
- **Access**: Role `ADMIN`
- Configurable key-value system settings in MySQL.

---

### 5. Messaging Endpoints (`/api/messages/*`)

#### `GET /api/messages/conversations`
- **Access**: Role `SHELTER` or `ADOPTER`
- **Response 200 OK**: List of active conversation threads accessible by the current user.

#### `GET /api/messages/thread?conversationId={id}`
- **Access**: Role `SHELTER` or `ADOPTER` (Enforces ownership check)
- **Response 200 OK**: Full message history ordered chronologically.

#### `POST /api/messages/send`
- **Access**: Role `SHELTER` or `ADOPTER`
- **Request Body**:
  ```json
  {
    "conversationId": 2,
    "content": "Hello, we would love to schedule a home visit this Saturday!"
  }
  ```
- **Response 201 Created**: Newly inserted message object with server timestamp.

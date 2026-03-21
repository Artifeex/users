# System Requirements & Admin Role Specification

## 1. General System Requirements
- **Authentication & Security:** Unique login/password per user. Passwords MUST be encrypted (e.g., BCrypt). Support for password reset.
- **Roles (RBAC):** `ADMIN`, `TEACHER`, `STUDENT`.
- **Performance & Scalability:** The system must handle high loads and support horizontal auto-scaling (implies stateless architecture, e.g., JWT).
- **Monitoring & Reliability:** Must include health monitoring, metrics, and critical failure alerting. Database backups are required.
- **Integrations:** Integration with an Email Service for sending notifications.
- **Data Import/Export:** Platform must support importing/exporting data via files (Excel).

## 2. Admin Role Capabilities
The Administrator manages the technical aspects of the system and organizes the learning process.

### 2.1. User Account Management
- **Create User:** Manually create accounts (generate login/password). The system must automatically send an email with temprorary credentials to the user.
- **Bulk Import:** Import users from Excel file.
- **Edit/Delete User:** Change email addresses, delete accounts, and reassign students to different groups or fields of study.
- **Search & Filter:** Find users via filters and view their detailed profiles.

### 2.2. Teacher Access Management
- Assign teachers to specific parts of the university hierarchy (Fields of Study, Institutes, or specific Student Groups).
- Edit these assignments later.

### 2.3. University Hierarchy Management
- View the entire hierarchy as a tree structure (Institutes -> Fields of Study -> Departments -> Student Groups).
- Edit structure: Add new groups, rename fields of study, move elements, etc.

## 3. UI/UX Workflows (Backend API Context)
- **User Creation Flow:** Admin submits form -> Backend hashes password, saves user -> Backend triggers async email sending.
- **Import Flow:** Admin uploads file -> Backend parses CSV/Excel -> Validates data -> Backend performs bulk insert.
- **Hierarchy Flow:** Admin requests hierarchy -> Backend returns a nested JSON tree -> Admin updates specific node -> Backend updates DB and cascades changes if necessary.
# Boardly – Multi-Tenant Task Collaboration Backend

Boardly is a **Kanban board based task collaboration platform** designed with **multi-tenancy**, **role-based access control (RBAC)**, and **real-time collaboration** in mind.  

The project focuses on the backend part of the platform ensuring clean architecture, strong authorization boundaries, and scalability.

---

## 🚀 Features

### ✅ Implemented
- **Authentication & Authorization**
  - Secure authentication system
  - Workspace-scoped and board-scoped RBAC
  - Fine-grained permission enforcement at API level

- **Multi-Tenant Workspaces**
  - Tenant-isolated workspaces with strict data boundaries per organization/team
  - Workspace membership and role management
  - Ownership and admin controls

- **Boards (Metadata Layer)**
  - Board lifecycle and visibility control
  - Board-level membership and roles
  - Permission validation and access enforcement

- **Real-Time Collaboration**
  - WebSocket-based communication
  - Live board updates for multiple connected users

- **API Endpoints**
  - **Authentication**: Login, registration, and token management.
  - **Workspaces**: Create, update, delete, and manage workspace members.
  - **Boards**: Create, update, delete, and manage board members.
  - **Kanban Boards**: Manage lists, cards, and board structure (MongoDB backed).

- **API Documentation**
  - REST APIs documented using **Swagger / OpenAPI**.

---

### 🛠️ Future Work
- **Frontend Application**
  - A frontend application is planned for future development.

- **Kanban Board Core**
  - Lists, cards, labels, assignments, and ordering
  - Optimized for frequent updates and collaboration-heavy usage

- **Caching Layer (Redis)**
  - Integration with Redis for improved performance and caching.

---

## 🧱 Architecture Overview

- **PostgreSQL**
  - Authentication & authorization
  - Users, workspaces, board metadata
  - Strong consistency and transactional integrity

- **MongoDB**
  - Kanban board content storage
  - Optimized for:
    - Deeply nested data structures
    - High read/write throughput

- **Spring Boot**
  - Modular service design
  - Declarative security
  - Transaction management

---

## 🧰 Tech Stack

- **Java 22**
- **Spring Boot 3.5.10**
- **Spring Security** (JWT)
- **Spring Data JPA** (PostgreSQL)
- **Spring Data MongoDB**
- **Spring WebSocket**
- **Spring Mail**
- **Lombok**
- **MapStruct**
- **Maven**

---

## 🔐 Authorization Model

Boardly enforces authorization at multiple levels:

- **Workspace Level**
  - Owner / Admin / Member / Guest roles
- **Board Level**
  - Admin / Member / Observer roles
- **API Enforcement**
  - Permission checks integrated with Spring Security
  - Unauthorized actions are rejected before business logic execution

---

## 📋 Prerequisites

Ensure you have the following installed:

*   [Java JDK 22](https://adoptium.net/)
*   [Maven](https://maven.apache.org/)
*   [PostgreSQL](https://www.postgresql.org/)
*   [MongoDB](https://www.mongodb.com/)

## 🛠️ Setup & Installation

1.  **Clone the repository**
    ```bash
    git clone <repository-url>
    cd boardly-backend
    ```

2.  **Database Configuration**

    *   **PostgreSQL**: Create a database named `boardly_db`.
    *   **MongoDB**: Ensure MongoDB is running on port `27017`. The application connects to `boardly_kanban`.

    You can update the credentials in `src/main/resources/application.yaml` if they differ from the defaults:

    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/boardly_db
        username: boardly_user # Default: boardly_user
        password: boardly_pass # Default: boardly_pass
      data:
        mongodb:
          uri: mongodb://boardly_user:boardly_pass@localhost:27017/boardly_kanban?authSource=admin
    ```

3.  **Environment Variables (Optional but Recommended)**
    For security, consider using environment variables for sensitive data like JWT secrets and email credentials.

4.  **Build the project**
    ```bash
    ./mvnw clean install
    ```

5.  **Run the application**
    ```bash
    ./mvnw spring-boot:run
    ```

    The server will start on `http://localhost:8080`.

## ⚙️ Configuration

The application is configured via `src/main/resources/application.yaml`. Key configurations include:

*   **Server Port**: `8080`
*   **API Base Path**: `/api/v1`
*   **JWT Configuration**:
    *   Secret: (Change this in production)
    *   Access Token Expiration: 15 minutes
    *   Refresh Token Expiration: 30 days

## 📧 Email Support

The application includes email functionality (likely for account verification or notifications). Configure your SMTP settings in `application.yaml`:

```yaml
spring:
  mail:
    host: "smtp.example.com"
    username: "your-email@example.com"
    password: "your-password"
```
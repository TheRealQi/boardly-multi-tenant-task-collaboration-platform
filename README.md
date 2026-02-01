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
---

### 🛠️ In Progress / Planned
- **Kanban Board Core**
  - Lists, cards, labels, assignments, and ordering
  - Optimized for frequent updates and collaboration-heavy usage

- **Real-Time Collaboration**
  - WebSocket-based communication
  - Live board updates for multiple connected users

- **Caching Layer**

- **API Documentation**
  - REST APIs fully documented using **Swagger / OpenAPI**
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

- Java  
- Spring Boot  
- PostgreSQL  
- MongoDB  
- Swagger / OpenAPI  
- *(Planned)* Redis  
- *(Planned)* WebSockets  

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

# Human Resource Management System (HRMS) – Backend

## 1. Project Overview

The Human Resource Management System (HRMS) is an enterprise-level web application developed to manage and streamline Human Resource operations at Hexa Co. International Ltd.

This backend system is implemented using Spring Boot and provides secure RESTful APIs for managing employee lifecycle operations, attendance, leave management, training processes, welfare management, and administrative workflows.

The implementation aligns with the functional and non-functional requirements defined in the Software Requirement Specification (SRS).

---

## 2. Objectives

The primary objectives of this backend system are:

- Centralize employee master data management
- Automate HR workflows (leave, training, termination, transfers)
- Provide secure role-based access control
- Enable integration with finance and external systems
- Ensure scalability, reliability, and maintainability

---

## 3. Technology Stack

### Backend Framework
- Java 21
- Spring Boot 3.x
- Spring Web (REST APIs)
- Spring Data JPA
- Spring Security (JWT-based authentication)
- Hibernate ORM

### Database
- PostgreSQL

### Build & DevOps
- Maven
- GitHub Actions (CI)
- Docker (optional containerization)

---

## 4. System Architecture

The backend follows a layered architecture:

- Controller Layer – Handles HTTP requests and responses
- Service Layer – Contains business logic
- Repository Layer – Database interaction using JPA
- Model Layer – Entity definitions
- DTO Layer – Data transfer between client and server
- Configuration Layer – Security and application configuration

---

## 5. Core Functional Modules

### 5.1 Employee Master Management
- Create, update, and manage employee profiles
- Maintain designation and branch details
- Manage employee document records
- Employee transfer workflows

### 5.2 Attendance & Leave Management
- Attendance recording and validation
- Overtime management
- Manual attendance adjustments
- Leave calculation and carry-forward logic
- Special leave workflows (Overseas, Maternity)

### 5.3 Employee Lifecycle Management
- Resignation processing
- Termination workflows
- Employee death case handling
- Multi-level approval workflows
- Finance system integration

### 5.4 Training & Development
- Training plan management
- Training request processing
- Employee allocation
- Attendance and feedback tracking
- Training reporting

### 5.5 Employee Welfare
- Welfare request submission
- Multi-stage approval process
- Finance approval integration

---

## 6. Authentication & Authorization

The system uses:

- JWT-based authentication
- Role-Based Access Control (RBAC)

Defined roles include:

- HR Administrator
- HR User
- Employee
- Higher Authority

Each endpoint is protected based on user roles and permissions.

---

## 7. Project Structure

src/main/java/com/hexaco/hrms
│
├── config → Security & configuration classes
├── controller → REST controllers
├── service → Business logic interfaces
│ └── impl → Service implementations
├── repository → JPA repositories
├── model → Entity classes
├── dto → Data transfer objects
├── exception → Global exception handling
└── util → Utility classes

This structure ensures modularity, maintainability, and separation of concerns.

---

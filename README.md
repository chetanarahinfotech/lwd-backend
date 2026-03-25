# 🧑‍💼 LWD Job Portal

<p align="center">
  A full-stack job portal platform connecting <b>Job Seekers</b>, <b>Recruiters</b>, <b>Recruiter Admins</b>, <b>Admins</b>, and <b>Super Admins</b> with secure authentication, advanced job workflows, premium feature access, and scalable backend architecture.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/Spring%20Boot-Backend-success?style=for-the-badge&logo=springboot" />
  <img src="https://img.shields.io/badge/React-Frontend-61DAFB?style=for-the-badge&logo=react" />
  <img src="https://img.shields.io/badge/MySQL-Database-blue?style=for-the-badge&logo=mysql" />
  <img src="https://img.shields.io/badge/JWT-Authentication-black?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Swagger-API%20Docs-85EA2D?style=for-the-badge&logo=swagger" />
</p>

---

## 📌 Overview

**LWD Job Portal** is a production-ready job platform designed to manage the complete hiring lifecycle.

It supports:
- job seeker registration, profile management, and job applications
- recruiter job posting and applicant handling
- recruiter admin company-level recruiter management
- admin platform-wide control and analytics
- super admin role and permission management

The backend is built with **Spring Boot**, secured using **JWT + Spring Security**, documented with **Swagger/OpenAPI**, and optimized for scalable growth using layered architecture, validation, exception handling, caching, and scheduler-based workflows.

---

## ✨ Key Highlights

- 🔐 JWT Authentication & Role-Based Authorization
- 📄 Swagger/OpenAPI Documentation
- ✅ DTO Validation + Global Exception Handling
- 📧 Email Verification & Password Reset
- ⚡ Active User Tracking with Caffeine Cache
- ⭐ Premium Feature Access using AOP
- 🔍 Global Search + Suggestions
- 📊 Admin / Recruiter / Recruiter Admin Dashboards
- 🚀 MySQL Strict Mode Compatible Queries
- 🧩 Clean layered architecture: Controller → Service → Repository

---

## 👥 User Roles

**👤 JOB_SEEKER**  
Register, login, manage profile, search jobs, apply for jobs, and track applications.

**🧑‍💼 RECRUITER**  
Manage recruiter profile, post jobs, manage job listings, and view applicants.

**🏢 RECRUITER_ADMIN**  
Manage recruiters within company, approve recruiter actions, and handle company-level control.

**🛠️ ADMIN**  
Manage users, companies, jobs, dashboards, analytics, and platform-wide operations.

**👑 SUPER_ADMIN**  
Manage admins, update roles, block/unblock admins, and handle full system-level control.
---

## 🚀 Core Features

### 👤 Job Seeker Module
- Registration and login
- Profile creation and update
- Skills, about info, social links, profile summary
- Job search and filtering
- Apply for portal/external jobs
- Track application history and status

### 🧑‍💼 Recruiter Module
- Recruiter profile management
- Request company approval
- Post and manage jobs
- View job applicants
- Recruiter dashboard

### 🏢 Recruiter Admin Module
- Manage company recruiters
- View pending recruiters
- Approve recruiters
- Block/unblock recruiters
- Recruiter admin dashboard

### 🛠️ Admin Module
- Manage users
- Manage companies
- Manage jobs
- View platform dashboards
- Access analytics
- Search across entities

### 👑 Super Admin Module
- View all admins
- Create admin
- Promote/demote admin
- Block/unblock admin
- Change user roles

---

## 🔐 Security

- JWT-based authentication
- Spring Security authorization
- Role-based endpoint protection using `@PreAuthorize`
- Password encryption using BCrypt
- Email verification flow
- Password reset using token-based flow
- Change password support
- Rate limit and brute-force protection for password APIs
- Protected Swagger testing with Bearer token authorization

---

## 🔍 Search & Discovery

- Global search API
- Category-based search:
  - Jobs
  - Companies
  - Candidates
  - Recruiters
  - Skills
- Search suggestion/autocomplete API
- Job recommendations
- Similar jobs
- Trending jobs

---

## ⭐ Premium Feature System

- Plan-based feature management
- AOP-based feature access control
- Usage limits
- Subscription handling
- Feature-level restriction using annotations like `@RequiresFeature`

---

## ⚡ Active User Tracking Flow

```text
Any API Request
     ↓
Interceptor / Activity Tracker
     ↓
Caffeine Cache (temporary online state)
     ↓
User shown as active
     ↓
Scheduled Sync Job
     ↓
Persist activity to database
```

---

## 🔄 Password Reset Flow

```text
User clicks "Forgot Password"
     ↓
Backend generates reset token
     ↓
Reset link sent by email
     ↓
User opens reset link
     ↓
Frontend sends token + new password
     ↓
Backend validates token
     ↓
Password updated successfully
```

---

## 📧 Email Verification Flow

```text
User registers
     ↓
Verification token generated
     ↓
Verification email sent
     ↓
User clicks email link
     ↓
Backend verifies token
     ↓
Email marked as verified
```

---

## 🧱 Project Architecture

```text
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

### Layer Responsibilities
- **Controller** → Handles HTTP requests/responses
- **Service** → Business logic and validations
- **Repository** → Database operations using JPA
- **DTOs** → Clean request/response payloads
- **Security** → JWT, roles, access checks
- **Exception Layer** → Global error handling

---

## 🛠️ Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- Maven
- Swagger / OpenAPI
- Caffeine Cache

### Frontend
- React
- Vite
- Tailwind CSS
- React Query
- Axios

### Database
- MySQL

### Tools
- Git & GitHub
- Postman
- Swagger UI
- Railway / Vercel

---

## 📂 Project Structure

```text
com.lwd.jobportal
│
├── authcontroller
├── authservice
├── controller
├── service
├── repository
├── entity
├── dto
├── enums
├── exception
├── security
├── config
├── pricing
└── util
```

---

## 📘 API Documentation

Swagger UI is available at:

```bash
http://localhost:8080/swagger-ui/index.html
```

or

```bash
http://localhost:8080/swagger-ui.html
```

After login:
1. copy JWT token
2. click **Authorize**
3. enter token as:

```bash
Bearer YOUR_TOKEN
```

---

## ⚙️ Setup & Installation

### Prerequisites
- Java 17+
- Maven
- MySQL
- Node.js + npm
- Git

### 1. Clone the repository

```bash
git clone https://github.com/chetanarahinfotech/lwd-backend
cd lwd-backend
```

### 2. Configure database

Update your `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### 3. Run backend

```bash
mvn clean install
mvn spring-boot:run
```

### 4. Run frontend

```bash
npm install
npm run dev
```

---

## 📌 Important API Modules

- `/api/auth` → Authentication APIs
- `/api/email` → Email verification APIs
- `/api/password` → Forgot/reset/change password APIs
- `/api/users` → User profile APIs
- `/api/job-seekers` → Job seeker profile APIs
- `/api/recruiter` → Recruiter APIs
- `/api/recruiter-admin` → Recruiter admin APIs
- `/api/admin` → Admin APIs
- `/api/super-admin` → Super admin APIs
- `/api/jobs` → Job APIs
- `/api/job-applications` → Job application APIs
- `/api/search` → Global search APIs
- `/api/dashboard` → Dashboard APIs

---

## ✅ Advanced Backend Concepts Used

- DTO validation
- Global exception handling
- Role-based authorization
- JWT token generation and validation
- AOP-based premium feature access
- Caffeine caching
- Scheduled synchronization
- MySQL strict mode safe queries
- EntityGraph-based optimization
- Layered clean architecture

---

## 📈 Future Enhancements

- Resume upload and parsing
- Notification system
- AI-powered recommendations
- Redis-based distributed cache
- Payment gateway for premium subscriptions
- Microservices migration

---

## 👨‍💻 Author

**Chetan Purkar**  
🎓 MSc Computer Science  
💻 Full Stack Developer

### Skills
Java • Spring Boot • React • MySQL • JWT • Swagger • System Design

---

## 🌟 Final Note

This project demonstrates a strong understanding of:
- backend architecture
- secure API design
- scalable feature planning
- real-world admin/recruiter/job seeker workflows

If you found this project useful, consider giving it a ⭐ on GitHub.
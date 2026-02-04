# 🧑‍💼 LWD Job Seeker Portal

A full-stack Job Portal application that connects Job Seekers, Companies, and Admins on a single secure platform.
Built using Spring Boot, JWT Security, and a scalable layered architecture.

## ✨ Key Highlights

JWT-based secure authentication

Role-based access control (ADMIN | COMPANY | USER)

Clean layered architecture (Controller → Service → Repository)

Soft delete and audit support across all entities

Scalable, frontend-ready backend design

## 🚀 Features
### 👤 Job Seeker (User)

User registration & login (JWT authentication)

View and search job listings

Apply for jobs

Track application status

Manage user profile

### 🏢 Company

Company registration & authentication

Create and manage company profile

Post, update, activate/deactivate jobs

View applicants for posted jobs

Soft delete and active/inactive company support

### 🛠️ Admin

Manage users, companies, and job listings

Monitor overall platform activity

Full role-based access control

### 🔐 Security

JWT Authentication

Spring Security

Role-based authorization
ADMIN | COMPANY | USER

Secure RESTful APIs

## 🧱 Core Entities

User

Company

Job

JobApplication

## 📌 Common Audit Fields

createdAt

updatedAt

createdBy

isActive (Soft delete support)

## 🛠️ Tech Stack
### 🔙 Backend

Java 17

Spring Boot

Spring Security

Spring Data JPA (Hibernate)

### 🗄️ Database

MySQL

### 🧰 Tools

Maven

Postman

Git & GitHub

## 📂 Backend Project Structure
com.lwd.jobportal
│
├── controller
├── service
├── repository
├── entity
├── dto
├── exception
├── security
├── config
└── enums

## ⚙️ Setup & Installation
### ✅ Prerequisites

Java 17+

MySQL

Maven

IDE (IntelliJ IDEA / Eclipse)

## 🧩 Steps to Run
### 1️⃣ Clone the repository
git clone https://github.com/chetanarahinfotech/lwd-job-portal

### 2️⃣ Configure MySQL

Update database credentials in application.properties

### 3️⃣ Build & Run
mvn clean install
mvn spring-boot:run

### 4️⃣ Test APIs

Use Postman to test secured REST APIs

## 📌 Future Enhancements

Advanced job filtering & search

Resume upload functionality

Email notifications

Frontend integration (React)

Migration to microservices architecture

## 👨‍💻 Author
Chetan Purkar

### 🎓 MSc Computer Science
### 💻 Full Stack Developer

Skills:
Java • Spring Boot • React • MySQL
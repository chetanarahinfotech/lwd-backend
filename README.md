🧑‍💼 LWD Job Seeker Portal

A full-stack Job Portal application that connects Job Seekers, Companies, and Admins on a single secure platform.
The system enables job searching, job posting, application tracking, and role-based management using Spring Boot & JWT Security.

✨ Key Highlights

Secure authentication using JWT

Role-based access control (ADMIN / COMPANY / USER)

Clean layered architecture (Controller → Service → Repository)

Soft delete & audit support across entities

Scalable backend design, frontend-ready

🚀 Features
👤 Job Seeker (User)

User registration & login (JWT-based)

View and search job listings

Apply for jobs

Track applied job status

Manage user profile

🏢 Company

Company registration & authentication

Create and manage company profile

Post, update, activate/deactivate job listings

View applicants for posted jobs

Soft delete & active/inactive company support

🛠️ Admin

Manage users, companies, and jobs

Monitor overall platform activity

Full role-based access control

🔐 Security

JWT Authentication

Spring Security

Role-based Authorization
ADMIN | COMPANY | USER

Secure RESTful APIs

🧱 Core Entities

User

Company

Job

JobApplication

📌 Common Audit Fields (All Entities)

createdAt

updatedAt

createdBy

isActive (Soft delete support)

🛠️ Tech Stack
🔙 Backend

Java 17

Spring Boot

Spring Security

Spring Data JPA (Hibernate)

🗄️ Database

MySQL

🧰 Tools

Maven

Postman

Git & GitHub

📂 Backend Project Structure
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

⚙️ Setup & Installation
✅ Prerequisites

Java 17+

MySQL

Maven

IDE (IntelliJ IDEA / Eclipse)

🧩 Steps to Run

Clone the repository

git clone https://github.com/chetanarahinfotech/lwd-job-portal


Configure MySQL

Update database details in application.properties

Build & Run

mvn clean install
mvn spring-boot:run


Test APIs

Use Postman to test secured APIs

📌 Future Enhancements

Advanced job filtering & search

Resume upload feature

Email notifications

Frontend integration (React)

Microservices architecture

👨‍💻 Author

Chetan Purkar
🎓 MSc Computer Science
💻 Full Stack Developer

Skills:
Java • Spring Boot • React • MySQL

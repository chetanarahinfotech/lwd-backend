🧑‍💼 LWD Job Seeker Portal

LWD Job Seeker Portal is a full-stack job portal application that connects job seekers with companies. It allows users to search and apply for jobs, while companies can post jobs and manage applications through a secure and role-based system.

🚀 Features
👤 User (Job Seeker)
User registration & login (JWT based)
View and search job listings
Apply for jobs
View applied job status
Manage profile

🏢 Company
Company registration & authentication
Create and manage company profile
Post, update, and deactivate job listings
View applicants for posted jobs
Soft delete & active/inactive company support

🛠️ Admin
Manage users, companies, and jobs
Monitor platform activities
Role-based access control

🔐 Security
JWT Authentication
Role-based authorization (ADMIN / COMPANY / USER)
Secure REST APIs using Spring Security

🧱 Project Entities
User
Company
Job
Job Application

Each entity includes audit fields:
createdAt
updatedAt
createdBy
isActive (soft delete support)

🛠️ Tech Stack
Backend
Java
Spring Boot
Spring Security
Spring Data JPA (Hibernate)
Database
MySQL
Tools
Maven
Postman
Git & GitHub

📂 Project Structure (Backend)
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
Prerequisites
Java 17+
MySQL
Maven
IDE (IntelliJ / Eclipse)

Steps
Clone the repository
git clone https://github.com/your-username/lwd-job-portal.git
Configure MySQL database in application.properties
Build and run the project
mvn clean install
mvn spring-boot:run
Test APIs using Postman

📌 Future Enhancements
Job filtering & advanced search
Resume upload
Email notifications
Frontend integration (React)
Microservices architecture

👨‍💻 Author

Chetan Purkar
MSc Computer Science | Full Stack Developer
Java • Spring Boot • React • MySQL

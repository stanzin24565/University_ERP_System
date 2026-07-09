# 🎓 University ERP System

> A desktop-based University Enterprise Resource Planning (ERP) system built using Java Swing to streamline academic and administrative workflows through secure authentication, role-based access control, and efficient course management.

---

## 📌 Overview

The University ERP System is a Java desktop application that enables students, instructors, and administrators to manage academic activities through a unified platform.

The application follows a layered architecture, separating authentication, business logic, database operations, and user interfaces. Authentication is handled independently from academic data using two dedicated databases, improving security and maintainability.

---

## ✨ Features

### 👨‍🎓 Student

- Secure Login
- Browse Course Catalog
- Register & Drop Courses
- View Timetable
- View Grades
- Download Transcript (CSV/PDF)

---

### 👨‍🏫 Instructor

- Manage Assigned Sections
- Enter Assessment Scores
- Automatic Final Grade Calculation
- View Class Statistics

---

### 👨‍💼 Administrator

- Add Students & Instructors
- Create Courses & Sections
- Assign Instructors
- Toggle Maintenance Mode
- Manage Academic Records

---

## 🔒 Security Features

- BCrypt Password Hashing
- Separate Authentication Database
- Role-Based Access Control (RBAC)
- Session Management
- Secure Password Verification

---

## 🏗️ System Architecture

```
                  Java Swing UI
                        │
                Service Layer
                        │
      ┌─────────────────┴─────────────────┐
      │                                   │
 Authentication DB                 ERP Database
(Usernames & Passwords)     (Students, Courses,
                              Sections, Grades)
```

---

## 🛠 Tech Stack

| Category | Technologies |
|-----------|--------------|
| Language | Java |
| GUI | Java Swing |
| Database | MySQL |
| Connectivity | JDBC |
| Security | BCrypt Password Hashing |
| Design | Object-Oriented Programming |

---

## 🧠 Core Concepts Used

- Object-Oriented Programming
- JDBC
- Database Design
- Authentication & Authorization
- Role-Based Access Control
- Layered Architecture
- Exception Handling
- File Export (CSV/PDF)

---

## 📂 Project Structure

```
src/
│
├── auth/
├── ui/
│   ├── student/
│   ├── instructor/
│   └── admin/
├── service/
├── data/
├── domain/
├── access/
└── util/
```

---

## 🚀 How It Works

1. User logs in through the authentication system.
2. Credentials are verified using hashed passwords.
3. User role is identified.
4. Appropriate dashboard is loaded.
5. Business operations are validated through the service layer.
6. ERP database is updated securely.

---

## 📸 Screenshots

> Login Page

> Student Dashboard

> Instructor Dashboard

> Admin Dashboard

> Course Registration

> Grade Management

---

## 🎥 Demo

*A short demo showcasing login, course registration, grading, and maintenance mode.*

---

## 🚀 Future Improvements

- Email Notifications
- Attendance Management
- Fee Payment Portal
- Cloud Deployment
- REST API Version
- Mobile Application
- Docker Support

---

## 📚 What I Learned

This project strengthened my understanding of software engineering principles beyond writing code. I gained practical experience in designing layered applications, implementing secure authentication, managing relational databases, enforcing role-based access control, and structuring desktop applications using Java Swing and JDBC.

-----University ERP System (Java Swing)------
---Overview----

The University ERP System is a desktop-based application developed using Java Swing that manages core academic workflows such as student enrollment, grading, transcript generation, and administrative control.

The system follows Object-Oriented Programming (OOP) principles, implements role-based access control, and ensures secure authentication using password hashing.

The application starts from a centralized Login Frame, which routes users based on their role (Admin / Faculty / Student).

------Key Features-----

Secure login system with password hashing

Role-based access control (Admin, Faculty, Student)

Student enrollment and course management

Grade entry and GPA calculation

Transcript generation

Admin-controlled maintenance mode

Dual-database architecture for separation of concerns

Interactive GUI built using Java Swing

-----Tech Stack------
Category	Technology
Language	Java
GUI	Java Swing
Design	OOP (MVC-inspired structure)
Security	Password Hashing (UNIX shadow-style)
IDE	IntelliJ IDEA / Eclipse
OS	Cross-platform

------How to Run the Project----
Run the Application

The application starts from the Login Frame.

 Run this file:

LoginFrame.java


In IntelliJ:

Right-click LoginFrame.java

Click Run 'LoginFrame.main()'

--- Login Flow---

User launches LoginFrame

Credentials are authenticated securely

System identifies the user role

User is redirected to the appropriate dashboard:

Admin Panel

instructor Panel

Student Panel
 

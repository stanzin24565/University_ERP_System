-- Small test dataset for University ERP (minimal)
-- Run this against MySQL to create minimal auth + erp data

-- AUTH DB
CREATE DATABASE IF NOT EXISTS university_auth_db;
USE university_auth_db;

CREATE TABLE IF NOT EXISTS users_auth (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  role ENUM('ADMIN','INSTRUCTOR','STUDENT') NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  status ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  failed_attempts INT DEFAULT 0,
  locked_until TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default password: password123 (bcrypt hash placeholder used in project)
INSERT INTO users_auth (username, role, password_hash) VALUES
('admin1', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J'),
('inst1', 'INSTRUCTOR', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J'),
('stu1', 'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J'),
('stu2', 'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J');

-- ERP DB
CREATE DATABASE IF NOT EXISTS university_erp_db;
USE university_erp_db;

CREATE TABLE IF NOT EXISTS courses (
  course_id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) UNIQUE NOT NULL,
  title VARCHAR(200) NOT NULL,
  credits INT NOT NULL
);

CREATE TABLE IF NOT EXISTS instructors (
  user_id INT PRIMARY KEY,
  department VARCHAR(100),
  office VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS students (
  user_id INT PRIMARY KEY,
  roll_no VARCHAR(20) UNIQUE NOT NULL,
  program VARCHAR(100),
  year INT
);

CREATE TABLE IF NOT EXISTS sections (
  section_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  instructor_id INT,
  day_time VARCHAR(100),
  room VARCHAR(50),
  capacity INT NOT NULL,
  semester VARCHAR(50),
  year INT,
  FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

CREATE TABLE IF NOT EXISTS enrollments (
  enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  section_id INT NOT NULL,
  status ENUM('REGISTERED','DROPPED') DEFAULT 'REGISTERED',
  enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY unique_enrollment (student_id, section_id)
);

CREATE TABLE IF NOT EXISTS settings (
  id INT AUTO_INCREMENT PRIMARY KEY,
  key_name VARCHAR(100) UNIQUE NOT NULL,
  value VARCHAR(255),
  description VARCHAR(255)
);

-- Minimal sample data
INSERT INTO courses (code, title, credits) VALUES ('CSE101','Intro to Programming',3);
INSERT INTO instructors (user_id, department, office) VALUES (2, 'Computer Science', 'CS-201');
INSERT INTO students (user_id, roll_no, program, year) VALUES (3, '2023001', 'Computer Science', 2023), (4, '2023002', 'Computer Science', 2023);

INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES (1, 2, 'Mon-Wed 10:00-11:30', 'A-101', 30, 'Fall 2025', 2025);

INSERT INTO enrollments (student_id, section_id) VALUES (3, 1), (4, 1);

-- Settings: maintenance off, drop deadline in future
INSERT INTO settings (key_name, value, description) VALUES ('maintenance_mode', 'false', 'System maintenance flag'), ('drop_deadline', '2025-12-31', 'Last date to drop courses');

-- Ensure foreign key relationships (if you want to recreate with auth FK, adjust accordingly)

-- End of TEST_DATA.sql

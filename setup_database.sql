CREATE TABLE IF NOT EXISTS university_auth_db.users_auth (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    role ENUM('ADMIN', 'INSTRUCTOR', 'STUDENT') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    failed_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL
);
CREATE TABLE IF NOT EXISTS university_erp_db.courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    credits INT NOT NULL
);
CREATE TABLE IF NOT EXISTS university_erp_db.students (
    user_id INT PRIMARY KEY,
    roll_no VARCHAR(20) UNIQUE NOT NULL,
    program VARCHAR(100),
    year INT,
    FOREIGN KEY (user_id) REFERENCES university_auth_db.users_auth(user_id)
);

CREATE TABLE IF NOT EXISTS university_erp_db.instructors (
    user_id INT PRIMARY KEY,
    department VARCHAR(100),
    office VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES university_auth_db.users_auth(user_id)
);
CREATE TABLE IF NOT EXISTS university_erp_db.sections (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    instructor_id INT,
    day_time VARCHAR(100),
    room VARCHAR(50),
    capacity INT NOT NULL,
    semester VARCHAR(50),
    year INT,
    FOREIGN KEY (course_id) REFERENCES university_erp_db.courses(course_id),
    FOREIGN KEY (instructor_id) REFERENCES university_erp_db.instructors(user_id)
);
CREATE TABLE IF NOT EXISTS university_erp_db.enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    status ENUM('REGISTERED', 'DROPPED') DEFAULT 'REGISTERED',
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_enrollment (student_id, section_id),
    FOREIGN KEY (student_id) REFERENCES university_erp_db.students(user_id),
    FOREIGN KEY (section_id) REFERENCES university_erp_db.sections(section_id)
);
-- Check if students table exists in ERP database
USE university_erp_db;
SHOW TABLES;
SHOW TABLES FROM university_erp_db;
SHOW TABLES FROM university_auth_db;
CREATE TABLE IF NOT EXISTS university_erp_db.students (
    user_id INT PRIMARY KEY,
    roll_no VARCHAR(20) UNIQUE NOT NULL,
    program VARCHAR(100),
    year INT,
    FOREIGN KEY (user_id) REFERENCES university_auth_db.users_auth(user_id)
);
CREATE TABLE IF NOT EXISTS university_erp_db.enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    status ENUM('REGISTERED', 'DROPPED') DEFAULT 'REGISTERED',
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_enrollment (student_id, section_id),
    FOREIGN KEY (student_id) REFERENCES university_erp_db.students(user_id),
    FOREIGN KEY (section_id) REFERENCES university_erp_db.sections(section_id)
);
CREATE TABLE IF NOT EXISTS university_erp_db.grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    component VARCHAR(50) NOT NULL,
    score DECIMAL(5,2),
    max_score DECIMAL(5,2) DEFAULT 100,
    FOREIGN KEY (enrollment_id) REFERENCES university_erp_db.enrollments(enrollment_id)
);

-- =============================================
-- INSERT SAMPLE DATA
-- =============================================

-- Insert data into AUTH database
USE university_auth_db;

-- Insert users (password for all is "password123")
INSERT INTO users_auth (username, role, password_hash) VALUES
('admin1', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J'),
('inst1', 'INSTRUCTOR', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J'),
('stu1', 'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J'),
('stu2', 'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J');

-- Insert data into ERP database
USE university_erp_db;

-- Insert students
INSERT INTO students (user_id, roll_no, program, year) VALUES
(3, '2023001', 'Computer Science', 2023),
(4, '2023002', 'Computer Science', 2023);

-- Insert instructor
INSERT INTO instructors (user_id, department, office) VALUES
(2, 'Computer Science', 'CS-101');

-- Insert courses (already inserted from earlier)
INSERT INTO COURSES(course_id , code , title) VALUES 
(2 ,"CSE201" , "Advanced Programming"),
(3 , "CSE601", "NLP");
-- Insert sections
INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES
-- (1, 2, 'Mon-Wed 10:00-11:30', 'Room A-101', 30, 'Fall', 2024),
(2, 2, 'Tue-Thu 14:00-15:30', 'Room B-201', 25, 'winter', 2024),
(3, 2, 'Mon-Wed-Fri 09:00-10:00', 'Room C-301', 40, 'Fall', 2024);

-- Insert enrollments
INSERT INTO enrollments (student_id, section_id) VALUES
(3, 1),  -- stu1 in CS101
(4, 1),  -- stu2 in CS101  
(3, 2);  -- stu1 in Data Structures

-- Insert settings
INSERT INTO settings (key_name, value, description) VALUES
('maintenance_mode', 'false', 'System maintenance mode'),
('drop_deadline', '2024-12-15', 'Last date to drop courses');




-- Insert data into AUTH database
-- INSERT INTO university_auth_db.users_auth (username, role, password_hash) VALUES
-- ('admin1', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J'),
-- ('inst1', 'INSTRUCTOR', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J'),
-- ('stu1', 'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J'),
-- ('stu2', 'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KbJ1Q.2V.8.5.5J5J5J5J5J5J5J');

-- Insert students
INSERT INTO university_erp_db.students (user_id, roll_no, program, year) VALUES
(3, '2023001', 'Computer Science', 2023),
(4, '2023002', 'Computer Science', 2023);

-- Insert instructor
INSERT INTO university_erp_db.instructors (user_id, department, office) VALUES
(2, 'Computer Science', 'CS-101');

-- Insert sections
INSERT INTO university_erp_db.sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES
(1, 2, 'Mon-Wed 10:00-11:30', 'Room A-101', 30, 'Fall', 2024),
(2, 2, 'Tue-Thu 14:00-15:30', 'Room B-201', 25, 'Fall', 2024),
(3, 2, 'Mon-Wed-Fri 09:00-10:00', 'Room C-301', 40, 'Fall', 2024);

-- Insert enrollments
INSERT INTO university_erp_db.enrollments (student_id, section_id) VALUES
(3, 1),  -- stu1 in CS101
(4, 1),  -- stu2 in CS101  
(3, 2);  -- stu1 in Data Structures

-- Insert settings

INSERT INTO university_erp_db.settings (key_name, value, description) VALUES
('maintenance_mode', 'false', 'System maintenance mode'),
('drop_deadline', '2024-12-15', 'Last date to drop courses');
-- Run this in MySQL to check current maintenance mode
USE university_erp_db;
SELECT * FROM settings WHERE key_name = 'maintenance_mode';

-- Check if settings table exists and has data
USE university_erp_db;
SHOW TABLES;
SELECT * FROM settings;

-- Check user counts
SELECT COUNT(*) as user_count FROM university_auth_db.users_auth;
SELECT COUNT(*) as course_count FROM courses;
SELECT COUNT(*) as section_count FROM sections;

-- Verify foreign key relationships
SELECT * FROM sections s 
JOIN courses c ON s.course_id = c.course_id 
JOIN instructors i ON s.instructor_id = i.user_id;
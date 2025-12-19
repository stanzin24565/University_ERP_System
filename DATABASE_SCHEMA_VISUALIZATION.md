# University ERP Database Schema Visualization

## Overview
The system uses two separate MySQL databases:
1. **university_auth_db** — Authentication & user credentials
2. **university_erp_db** — Academic data (courses, enrollments, grades, etc.)

---

## Database 1: university_auth_db

### Table: users_auth
**Purpose:** Store user login credentials and lockout tracking

| Field | Type | Key | Description |
|-------|------|-----|-------------|
| user_id | INT | PRIMARY | Unique user identifier |
| username | VARCHAR(50) | UNIQUE | Login username |
| role | ENUM('ADMIN','INSTRUCTOR','STUDENT') | | User role |
| password_hash | VARCHAR(255) | | Bcrypt hashed password |
| status | ENUM('ACTIVE','INACTIVE') | | Account status |
| failed_attempts | INT | | Count of failed login attempts |
| locked_until | TIMESTAMP | | Account locked expiry (NULL = not locked) |
| created_at | TIMESTAMP | | Account creation date |

**Sample Data:**
```
user_id | username | role       | password_hash | status | failed_attempts | locked_until | created_at
--------|----------|------------|---------------|--------|-----------------|--------------|-------------------
1       | admin1   | ADMIN      | [hash]        | ACTIVE | 0               | NULL         | 2025-11-27 10:00
2       | inst1    | INSTRUCTOR | [hash]        | ACTIVE | 0               | NULL         | 2025-11-27 10:00
3       | stu1     | STUDENT    | [hash]        | ACTIVE | 0               | NULL         | 2025-11-27 10:00
4       | stu2     | STUDENT    | [hash]        | ACTIVE | 0               | NULL         | 2025-11-27 10:00
```

---

## Database 2: university_erp_db

### Table: courses
**Purpose:** Store course definitions

| Field | Type | Key | Description |
|-------|------|-----|-------------|
| course_id | INT | PRIMARY | Unique course identifier |
| code | VARCHAR(20) | UNIQUE | Course code (e.g., CSE101) |
| title | VARCHAR(200) | | Course name |
| credits | INT | | Credit hours |

**Sample Data:**
```
course_id | code    | title                   | credits
----------|---------|-------------------------|--------
1         | CSE101  | Intro to Programming    | 3
2         | CSE102  | Data Structures         | 4
3         | MTH201  | Calculus I              | 4
```

---

### Table: instructors
**Purpose:** Store instructor-specific information

| Field | Type | Key | Description |
|-------|------|-----|-------------|
| user_id | INT | PRIMARY | Reference to users_auth (2, 5, 6, etc.) |
| department | VARCHAR(100) | | Department name |
| office | VARCHAR(50) | | Office location |

**Sample Data:**
```
user_id | department         | office
--------|-------------------|--------
2       | Computer Science   | CS-201
5       | Mathematics        | M-105
6       | Computer Science   | CS-305
```

---

### Table: students
**Purpose:** Store student-specific information

| Field | Type | Key | Description |
|-------|------|-----|-------------|
| user_id | INT | PRIMARY | Reference to users_auth (3, 4, 7, etc.) |
| roll_no | VARCHAR(20) | UNIQUE | Student roll number |
| program | VARCHAR(100) | | Degree program |
| year | INT | | Academic year |

**Sample Data:**
```
user_id | roll_no | program              | year
--------|---------|----------------------|------
3       | 2023001 | Computer Science     | 2023
4       | 2023002 | Computer Science     | 2023
7       | 2023003 | Electronics          | 2023
8       | 2024001 | Computer Science     | 2024
```

---

### Table: sections
**Purpose:** Store course section/class instances

| Field | Type | Key | Description |
|-------|------|-----|-------------|
| section_id | INT | PRIMARY | Unique section identifier |
| course_id | INT | FK | Reference to courses |
| instructor_id | INT | FK | Reference to instructor user_id |
| day_time | VARCHAR(100) | | Schedule (e.g., Mon-Wed 10:00-11:30) |
| room | VARCHAR(50) | | Classroom location |
| capacity | INT | | Max students allowed |
| semester | VARCHAR(50) | | Semester (e.g., Fall 2025) |
| year | INT | | Academic year |

**Sample Data:**
```
section_id | course_id | instructor_id | day_time           | room  | capacity | semester    | year
-----------|-----------|---------------|-------------------|-------|----------|-------------|------
1          | 1         | 2             | Mon-Wed 10:00-11:30| A-101 | 30       | Fall 2025   | 2025
2          | 1         | 2             | Tue-Thu 14:00-15:30| A-102 | 25       | Fall 2025   | 2025
3          | 2         | 5             | Mon-Wed 13:00-14:30| M-201 | 35       | Fall 2025   | 2025
```

---

### Table: enrollments
**Purpose:** Track student enrollment in sections

| Field | Type | Key | Description |
|-------|------|-----|-------------|
| enrollment_id | INT | PRIMARY | Unique enrollment identifier |
| student_id | INT | FK | Reference to student user_id |
| section_id | INT | FK | Reference to sections |
| status | ENUM('REGISTERED','DROPPED') | | Enrollment status |
| enrolled_at | TIMESTAMP | | Enrollment date |

**Sample Data:**
```
enrollment_id | student_id | section_id | status     | enrolled_at
--------------|-----------|------------|------------|-------------------
1             | 3         | 1          | REGISTERED | 2025-11-01 09:30
2             | 3         | 3          | REGISTERED | 2025-11-01 09:35
3             | 4         | 1          | REGISTERED | 2025-11-01 09:40
4             | 4         | 2          | DROPPED    | 2025-11-25 14:15
5             | 7         | 1          | REGISTERED | 2025-11-05 10:00
```

---

### Table: grades
**Purpose:** Store student grades for enrolled courses

| Field | Type | Key | Description |
|-------|------|-----|-------------|
| grade_id | INT | PRIMARY | Unique grade identifier |
| enrollment_id | INT | FK | Reference to enrollments |
| assignment_weight | DECIMAL(5,2) | | Weight % (0-100) |
| midterm_weight | DECIMAL(5,2) | | Midterm weight % |
| final_weight | DECIMAL(5,2) | | Final exam weight % |
| assignment_score | DECIMAL(5,2) | | Actual assignment score |
| midterm_score | DECIMAL(5,2) | | Midterm score |
| final_score | DECIMAL(5,2) | | Final exam score |
| weighted_score | DECIMAL(5,2) | | Calculated final grade |
| letter_grade | VARCHAR(2) | | A, B, C, D, F |

**Sample Data:**
```
grade_id | enrollment_id | assign_w | midterm_w | final_w | assign_s | midterm_s | final_s | weighted_score | letter
---------|---------------|----------|-----------|---------|----------|-----------|---------|----------------|-------
1        | 1             | 30       | 30        | 40      | 85       | 78        | 82      | 81.6           | B
2        | 2             | 25       | 35        | 40      | 88       | 92        | 90      | 90.2           | A
3        | 3             | 30       | 30        | 40      | 75       | 70        | 68      | 70.8           | C
4        | 5             | 30       | 30        | 40      | 92       | 95        | 98      | 95.2           | A
```

---

### Table: settings
**Purpose:** System-wide configuration settings

| Field | Type | Key | Description |
|-------|------|-----|-------------|
| id | INT | PRIMARY | Setting identifier |
| key_name | VARCHAR(100) | UNIQUE | Setting name |
| value | VARCHAR(255) | | Setting value |
| description | VARCHAR(255) | | Purpose/notes |

**Sample Data:**
```
id | key_name           | value      | description
---|-------------------|------------|----------------------------------------
1  | maintenance_mode  | false      | System maintenance flag
2  | drop_deadline     | 2025-12-31 | Last date to drop courses
3  | semester          | Fall 2025  | Current semester
4  | academic_year     | 2025       | Current academic year
5  | max_courses       | 5          | Max courses per student per semester
```

---

## Entity Relationship Diagram (Text Format)

```
┌─────────────────────────┐
│   university_auth_db    │
├─────────────────────────┤
│     users_auth          │
├─────────────────────────┤
│ PK: user_id             │
│ username (UNIQUE)       │
│ role (ENUM)             │
│ password_hash           │
│ status                  │
│ failed_attempts         │
│ locked_until            │
└──────────┬──────────────┘
           │
           │ user_id FK
           │
┌──────────▼──────────────────────────────────────────────────┐
│          university_erp_db                                  │
├──────────┬──────────────┬──────────────┬────────────────────┤
│          │              │              │                    │
│   instructors   │ students  │ courses    │ settings           │
│   (FK: user_id) │(FK: user_id)│(PK: course_id)│(PK: id)        │
│   department    │ roll_no  │ code       │ key_name           │
│   office        │ program  │ title      │ value              │
│                 │ year     │ credits    │ description        │
└──────────┬──────────────┬──────────────┴────────────────────┘
           │              │
           │              │  
           │     ┌────────▼─────────────┐
           │     │    sections         │
           │     ├─────────────────────┤
           │     │ PK: section_id      │
           │     │ FK: course_id ──────┼──→ courses
           │     │ FK: instructor_id ──┼──→ instructors
           │     │ day_time            │
           │     │ room                │
           │     │ capacity            │
           │     │ semester, year      │
           │     └────────┬────────────┘
           │              │
           │              │
           │     ┌────────▼──────────────┐
           │     │   enrollments        │
           │     ├──────────────────────┤
           │     │ PK: enrollment_id    │
           │     │ FK: student_id ──────┼──→ students
           │     │ FK: section_id ──────┼──→ sections
           │     │ status (ENUM)        │
           │     │ enrolled_at          │
           │     └────────┬─────────────┘
           │              │
           │              │
           │     ┌────────▼──────────────┐
           │     │      grades          │
           │     ├──────────────────────┤
           │     │ PK: grade_id         │
           │     │ FK: enrollment_id ───┼──→ enrollments
           │     │ assignment_weight    │
           │     │ midterm_weight       │
           │     │ final_weight         │
           │     │ assignment_score     │
           │     │ midterm_score        │
           │     │ final_score          │
           │     │ weighted_score       │
           │     │ letter_grade         │
           │     └──────────────────────┘
```

---

## Database Statistics

### Table Sizes (from sample)
- **users_auth:** 4 users (1 admin, 1 instructor, 2 students)
- **courses:** 3 courses
- **instructors:** 3 instructor records
- **students:** 4 student records
- **sections:** 3 sections
- **enrollments:** 5 enrollments
- **grades:** 4 grade records
- **settings:** 5 configuration entries

### Key Relationships
- Each user in `users_auth` has corresponding record in either `instructors` or `students`
- Each section must reference a valid course and instructor
- Each enrollment must reference a valid student and section
- Each grade must reference a valid enrollment

---

## Access Control by Role

| Action | Admin | Instructor | Student |
|--------|-------|------------|---------|
| View own profile | ✓ | ✓ | ✓ |
| View all users | ✓ | ✗ | ✗ |
| Manage courses & sections | ✓ | ✗ | ✗ |
| Set grades | ✓ | ✓ (own sections) | ✗ |
| View grades | ✓ | ✓ (own students) | ✓ (own) |
| Enroll in courses | ✓ | ✗ | ✓ |
| Drop courses | ✓ | ✗ | ✓ (before deadline) |
| Export data (CSV) | ✓ | ✓ | ✓ (own grades) |
| Import data (CSV) | ✓ | ✓ | ✗ |
| Change own password | ✓ | ✓ | ✓ |
| System maintenance mode | ✓ | ✗ | ✗ |

---

## How to Query Sample Data

### Get all students and their current enrollments
```sql
SELECT s.roll_no, s.program, c.code, c.title, sec.day_time
FROM students s
JOIN enrollments e ON s.user_id = e.student_id
JOIN sections sec ON e.section_id = sec.section_id
JOIN courses c ON sec.course_id = c.course_id
WHERE e.status = 'REGISTERED';
```

### Get student grades
```sql
SELECT s.roll_no, c.code, c.title, g.weighted_score, g.letter_grade
FROM students s
JOIN enrollments e ON s.user_id = e.student_id
JOIN sections sec ON e.section_id = sec.section_id
JOIN courses c ON sec.course_id = c.course_id
JOIN grades g ON e.enrollment_id = g.enrollment_id;
```

### Check user lockout status
```sql
SELECT user_id, username, failed_attempts, locked_until
FROM users_auth
WHERE locked_until IS NOT NULL AND locked_until > NOW();
```

---

## Database Initialization Commands

### Create databases and run schema
```bash
# Load both databases
mysql -u root -p < setup_database.sql

# Or load only test data
mysql -u root -p < TEST_DATA.sql

# Apply migration if needed (adds lockout columns)
mysql -u root -p < MIGRATION_ADD_LOCKOUT.sql
```

---

**Last Updated:** 27 November 2025
**Schema Version:** 1.2 (with lockout support)


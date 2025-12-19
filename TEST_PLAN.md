TEST PLAN — University ERP

Objective
- Verify core functionality and newly added bonus features (CSV import/export, Change Password, Login Lockout).

Scope
- Core features: login, student registration, drop (deadline), grade viewing, instructor grading, admin functions, maintenance mode.
- Bonus features: CSV export/import (grades), Change Password dialog & validation, login lockout (3 attempts -> 15 min).

Test Environment
- OS: macOS (your machine)
- Java: OpenJDK 21 (use same JRE used for building)
- DB: MySQL with two databases: `university_auth_db`, `university_erp_db`
- Project: /Users/stanzinchondol/Downloads/erpbackendl
- Compile: `mvn clean compile -q`
- Run: `java -cp target/classes edu.univ.erp.ui.LoginFrame`

Test Data
- Use `TEST_DATA.sql` (included) to initialize minimal dataset: admin1, inst1, stu1, stu2, one course, one section, enrollments, settings.

Pass/Fail Criteria
- Each test has expected outcome. Pass = actual == expected. Fail = mismatch or exception.

Test Cases (high priority)
1) Login (happy path)
   - Steps: Launch LoginFrame; login as `admin1` / `password123`.
   - Expected: Admin dashboard loads.

2) Wrong password handling
   - Steps: Attempt login with wrong password 1-3 times.
   - Expected: Incorrect message on 1-2; on 3rd attempt account locked for 15 min with countdown.

3) Reset lockout (DB)
   - Steps: Run SQL to set failed_attempts=0, locked_until=NULL.
   - Expected: Login allowed with correct password.

4) Change Password (UI)
   - Steps: Login, Settings -> Change Password. Enter current password, then new valid password (e.g., NewPass123). Confirm.
   - Expected: Success dialog; subsequent login works with new password.

5) CSV Export (student)
   - Steps: Login as `stu1`, go to Grades tab, click Export -> select CSV.
   - Expected: CSV file saved; open shows headers and rows; quotes escaped for special characters.

6) CSV Import (instructor grading panel)
   - Steps: (If import UI present) use a sample CSV to import grades.
   - Expected: Grades parsed and saved to DB.

7) Drop Deadline enforcement
   - Steps: Student attempts to drop after `drop_deadline` set to a past date.
   - Expected: Drop blocked with message "Deadline has passed".

8) Maintenance mode blocking
   - Steps: Admin toggles maintenance_mode = true in settings table or System Settings panel.
   - Expected: Students cannot register/drop/export; instructors cannot edit grades; admin still has full access.

Execution Notes
- Perform tests sequentially. Record actual results in the one-page summary.
- For lockout tests, you can speed up by manually setting `locked_until` in DB to a near-future timestamp to verify countdown behavior.

Attachments
- `TEST_DATA.sql` (small dataset)
- `TEST_SUMMARY.md` (one-page, fillable results)

End of TEST_PLAN.md

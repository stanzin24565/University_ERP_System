# One-Page Test Summary (Fillable)

Project: University ERP
Date: ____________________
Tester: __________________

- Test Environment:
  - OS: macOS
  - JDK: OpenJDK 21
  - Maven: (e.g. 3.8.x)
  - Databases: MySQL (auth: `university_auth_db`, erp: `university_erp_db`)

- Quick Checklist:
  - `mvn clean compile -q` : [ ] Success  [ ] Fail
  - Ran `setup_database.sql` or `TEST_DATA.sql` : [ ] Yes  [ ] No
  - Launched UI: `java -cp target/classes edu.univ.erp.ui.LoginFrame` : [ ] Yes  [ ] No

- Test Cases (mark Pass/Fail and optional notes):
  1) Login with default user `admin1` / `password123` — Result: ____  Notes: _______
  2) Incorrect password lockout: attempt 3 wrong passwords for `stu1` → account locked → Result: ____  Notes: _______
  3) Change password flow: Login → Menu → Change Password → set new password and re-login → Result: ____  Notes: _______
  4) Drop deadline enforcement: try to drop a course after `drop_deadline` set to past date → Result: ____  Notes: _______
  5) CSV Export: export student grades to CSV from StudentGradesPanel → Result: ____  Notes: _______
  6) CSV Import (Instructor/Admin): import roster/grades via CSV → Result: ____  Notes: _______
  7) Maintenance banner: set `maintenance_mode` to 'true' in `settings` table → dashboards show banner → Result: ____  Notes: _______

- Overall Result: Pass / Fail  Notes: _______________________________

- Blockers / Issues Found:
  - ___________________________________________________________

- Recommended Next Steps:
  - If login issues, run SQL reset from `LOGIN_PASSWORD_GUIDE.md`.
  - If DB missing lockout columns, apply `MIGRATION_ADD_LOCKOUT.sql`.


Signature: __________________________


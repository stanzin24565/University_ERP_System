#!/bin/bash

# Script to display all main tables from University ERP databases
# Usage: ./view_all_tables.sh

MYSQL_USER="root"
MYSQL_PASS=""  # Change if needed
MYSQL_HOST="localhost"

echo "================================"
echo "UNIVERSITY ERP - DATABASE VIEWER"
echo "================================"
echo ""

# Auth Database Tables
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📁 DATABASE: university_auth_db"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "📋 TABLE: users_auth"
echo "─────────────────────────────────────────────"
mysql -u "$MYSQL_USER" ${MYSQL_PASS:+-p"$MYSQL_PASS"} -h "$MYSQL_HOST" -e "USE university_auth_db; SELECT user_id, username, role, status, failed_attempts, locked_until FROM users_auth;" 2>/dev/null || echo "⚠️  Connection failed. Check MySQL credentials."
echo ""

# ERP Database Tables
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📁 DATABASE: university_erp_db"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "📋 TABLE: courses"
echo "─────────────────────────────────────────────"
mysql -u "$MYSQL_USER" ${MYSQL_PASS:+-p"$MYSQL_PASS"} -h "$MYSQL_HOST" -e "USE university_erp_db; SELECT course_id, code, title, credits FROM courses;" 2>/dev/null || echo "⚠️  Connection failed."
echo ""

echo "📋 TABLE: instructors"
echo "─────────────────────────────────────────────"
mysql -u "$MYSQL_USER" ${MYSQL_PASS:+-p"$MYSQL_PASS"} -h "$MYSQL_HOST" -e "USE university_erp_db; SELECT user_id, department, office FROM instructors;" 2>/dev/null || echo "⚠️  Connection failed."
echo ""

echo "📋 TABLE: students"
echo "─────────────────────────────────────────────"
mysql -u "$MYSQL_USER" ${MYSQL_PASS:+-p"$MYSQL_PASS"} -h "$MYSQL_HOST" -e "USE university_erp_db; SELECT user_id, roll_no, program, year FROM students;" 2>/dev/null || echo "⚠️  Connection failed."
echo ""

echo "📋 TABLE: sections"
echo "─────────────────────────────────────────────"
mysql -u "$MYSQL_USER" ${MYSQL_PASS:+-p"$MYSQL_PASS"} -h "$MYSQL_HOST" -e "USE university_erp_db; SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester FROM sections;" 2>/dev/null || echo "⚠️  Connection failed."
echo ""

echo "📋 TABLE: enrollments"
echo "─────────────────────────────────────────────"
mysql -u "$MYSQL_USER" ${MYSQL_PASS:+-p"$MYSQL_PASS"} -h "$MYSQL_HOST" -e "USE university_erp_db; SELECT enrollment_id, student_id, section_id, status, enrolled_at FROM enrollments;" 2>/dev/null || echo "⚠️  Connection failed."
echo ""

echo "📋 TABLE: grades"
echo "─────────────────────────────────────────────"
mysql -u "$MYSQL_USER" ${MYSQL_PASS:+-p"$MYSQL_PASS"} -h "$MYSQL_HOST" -e "USE university_erp_db; SELECT grade_id, enrollment_id, assignment_weight, midterm_weight, final_weight, weighted_score, letter_grade FROM grades;" 2>/dev/null || echo "⚠️  Connection failed."
echo ""

echo "📋 TABLE: settings"
echo "─────────────────────────────────────────────"
mysql -u "$MYSQL_USER" ${MYSQL_PASS:+-p"$MYSQL_PASS"} -h "$MYSQL_HOST" -e "USE university_erp_db; SELECT id, key_name, value FROM settings;" 2>/dev/null || echo "⚠️  Connection failed."
echo ""

echo "================================"
echo "✅ View Complete"
echo "================================"

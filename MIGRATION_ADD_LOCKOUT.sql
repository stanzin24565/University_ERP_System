-- =============================================
-- DATABASE MIGRATION: Add Login Lockout Columns
-- =============================================
-- Run this if you have an existing database without the lockout columns

USE university_auth_db;

-- Add lockout columns if they don't exist
ALTER TABLE users_auth ADD COLUMN IF NOT EXISTS failed_attempts INT DEFAULT 0;
ALTER TABLE users_auth ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP NULL;

-- Reset all failed attempts and locks
UPDATE users_auth SET failed_attempts = 0, locked_until = NULL;

-- Verify the columns exist
DESC users_auth;

SELECT 'Migration Complete!' as status;

package edu.univ.erp.auth;
import edu.univ.erp.util.PasswordUtil;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncoder {

    // Hash password with salt
    public String encode(CharSequence rawPassword) {
        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(rawPassword.toString(), salt);
        return salt + ":" + hashedPassword;
    }

    // Verify password against stored hash
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || !encodedPassword.contains(":")) {
            return false;
        }

        String[] parts = encodedPassword.split(":", 2);
        String salt = parts[0];
        String storedHash = parts[1];

        return PasswordUtil.verifyPassword(rawPassword.toString(), salt, storedHash);
    }

    // Generate secure temporary password
    public String generateTemporaryPassword() {
        return PasswordUtil.generateTemporaryPassword();
    }

    // Check password strength
    public boolean isStrongPassword(String password) {
        return PasswordUtil.isStrongPassword(password);
    }

    // Get password strength score (0-4)
    public int getPasswordStrength(String password) {
        if (password == null) return 0;

        int score = 0;

        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // Character variety checks
        if (password.matches(".[A-Z].")) score++;
        if (password.matches(".[a-z].")) score++;
        if (password.matches(".[0-9].")) score++;
        if (password.matches(".[!@#$%^&()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;

        return Math.min(score, 4);
    }

    // Generate password hash for storage
    public String createHash(String password) {
        String salt = PasswordUtil.generateSalt();
        return salt + ":" + PasswordUtil.hashPassword(password, salt);
    }
}
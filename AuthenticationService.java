package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import edu.univ.erp.data.UserDAO;
import edu.univ.erp.util.Logger;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AuthenticationService {
    private final UserDAO userDAO;
    private final Logger logger;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_MINUTES = 15;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.logger = Logger.getInstance();
    }

    // ✅ Authenticate user using username and password
    public User authenticate(String username, String password) {
        try {
            User user = userDAO.findByUsername(username);
            
            if (user == null) {
                logger.warn("Login attempt with non-existent username: " + username);
                return null;
            }
            
            // Check if user is locked out
            if (isUserLockedOut(user)) {
                logger.warn("Login attempt for locked out user: " + username);
                return null;
            }
            
            // Verify password
            if (user.getPassword().equals(password)) {
                // Successful login: reset failed attempts
                resetFailedAttempts(user);
                logger.info("User authenticated successfully: " + username);
                return user;
            } else {
                // Failed login: increment failed attempts
                incrementFailedAttempts(user);
                logger.warn("Invalid credentials for user: " + username);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error authenticating user: " + username, e);
            return null;
        }
    }

    /**
     * Check if user account is locked due to too many failed attempts
     */
    private boolean isUserLockedOut(User user) {
        if (user.getLockedUntil() == null) {
            return false; // Not locked
        }
        
        Timestamp lockedUntil = user.getLockedUntil();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockTime = lockedUntil.toLocalDateTime();
        
        if (now.isBefore(lockTime)) {
            return true; // Still locked
        } else {
            // Lock time has passed, reset the lock
            resetFailedAttempts(user);
            return false;
        }
    }

    /**
     * Increment failed login attempts and lock account if necessary
     */
    private void incrementFailedAttempts(User user) {
        int currentAttempts = (user.getFailedAttempts() == null ? 0 : user.getFailedAttempts());
        currentAttempts++;
        
        user.setFailedAttempts(currentAttempts);
        
        if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
            // Lock the account
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
            user.setLockedUntil(Timestamp.valueOf(lockUntil));
            logger.warn("Account locked for user: " + user.getUsername() + 
                       " (too many failed attempts). Locked until: " + lockUntil);
        }
        
        userDAO.save(user);
    }

    /**
     * Reset failed attempts after successful login
     */
    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userDAO.save(user);
    }

    /**
     * Get lockout time remaining in seconds
     * @return seconds remaining, or 0 if not locked
     */
    public int getLockoutTimeRemaining(User user) {
        if (user.getLockedUntil() == null) {
            return 0;
        }
        
        LocalDateTime lockUntil = user.getLockedUntil().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(lockUntil)) {
            return (int) java.time.temporal.ChronoUnit.SECONDS.between(now, lockUntil);
        }
        
        return 0;
    }

    // ✅ Check if session is valid (user exists)
    public boolean isValidSession(Integer userId) {
        try {
            User user = userDAO.findById(userId);
            boolean valid = (user != null);
            logger.debug("Session check for userId " + userId + ": " + valid);
            return valid;
        } catch (Exception e) {
            logger.error("Error checking session validity for userId: " + userId, e);
            return false;
        }
    }

    // ✅ Get user by ID
    public User getUserById(Integer userId) {
        try {
            return userDAO.findById(userId);
        } catch (Exception e) {
            logger.error("Error getting user by ID: " + userId, e);
            return null;
        }
    }
}

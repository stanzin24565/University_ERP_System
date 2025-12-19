package edu.univ.erp.auth;
import edu.univ.erp.domain.User;
import edu.univ.erp.auth.JWTTokenUtil;
import edu.univ.erp.auth.AuthenticationService;
import edu.univ.erp.util.Logger;

public class TokenAuthenticationProvider {
    private AuthenticationService authService;
    private Logger logger;

    public TokenAuthenticationProvider() {
        this.authService = new AuthenticationService();
        this.logger = Logger.getInstance();
    }

    public User authenticate(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            if (!JWTTokenUtil.validateToken(token)) {
                logger.warn("Token authentication failed: Invalid token");
                return null;
            }

            if (JWTTokenUtil.isTokenExpired(token)) {
                logger.warn("Token authentication failed: Token expired");
                return null;
            }

            Integer userId = JWTTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                logger.warn("Token authentication failed: Invalid user ID in token");
                return null;
            }

            User user = authService.getUserById(userId);
            if (user == null) {
                logger.warn("Token authentication failed: User not found for ID: " + userId);
                return null;
            }

            logger.info("Token authentication successful for user: " + user.getUsername());
            return user;
        } catch (Exception e) {
            logger.error("Token authentication error", e);
            return null;
        }
    }

    public String refreshToken(String oldToken) {
        try {
            return JWTTokenUtil.refreshToken(oldToken);
        } catch (Exception e) {
            logger.error("Token refresh error", e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        return JWTTokenUtil.validateToken(token) && !JWTTokenUtil.isTokenExpired(token);
    }

    public String generateToken(User user) {
        return JWTTokenUtil.generateToken(user);
    }

    public String generateToken(User user, long expirationMillis) {
        return JWTTokenUtil.generateToken(user, expirationMillis);
    }
}
package edu.univ.erp.auth;
import edu.univ.erp.util.ConfigManager;

public class SecurityConfig {
    private static SecurityConfig instance;
    private ConfigManager configManager;

    private SecurityConfig() {
        this.configManager = ConfigManager.getInstance();
    }

    public static SecurityConfig getInstance() {
        if (instance == null) {
            instance = new SecurityConfig();
        }
        return instance;
    }

    // Password policy configuration
    public int getMinPasswordLength() {
        return configManager.getIntProperty("security.password.minLength", 8);
    }

    public boolean isPasswordComplexityRequired() {
        return configManager.getBooleanProperty("security.password.complexity", true);
    }

    public int getMaxLoginAttempts() {
        return configManager.getIntProperty("security.login.maxAttempts", 5);
    }

    public int getAccountLockoutDuration() {
        return configManager.getIntProperty("security.login.lockoutDuration", 30); // minutes
    }

    public int getSessionTimeout() {
        return configManager.getIntProperty("security.session.timeout", 3600); // seconds
    }

    public boolean isTwoFactorEnabled() {
        return configManager.getBooleanProperty("security.twofactor.enabled", false);
    }

    public String getAllowedPasswordSpecialChars() {
        return configManager.getProperty("security.password.specialChars", "!@#$%^&*()_+-=[]{};':\",./<>?");
    }

    public int getPasswordHistorySize() {
        return configManager.getIntProperty("security.password.historySize", 5);
    }

    public int getPasswordExpiryDays() {
        return configManager.getIntProperty("security.password.expiryDays", 90);
    }

    // JWT configuration
    public String getJwtSecret() {
        return configManager.getProperty("security.jwt.secret", "defaultJwtSecretKey");
    }

    public long getJwtExpiration() {
        return configManager.getIntProperty("security.jwt.expiration", 86400000); // 24 hours
    }

    // CORS configuration
    public String[] getAllowedOrigins() {
        String origins = configManager.getProperty("security.cors.allowedOrigins", "*");
        return origins.split(",");
    }

    // Security headers configuration
    public boolean isHstsEnabled() {
        return configManager.getBooleanProperty("security.headers.hsts", true);
    }

    public boolean isContentSecurityPolicyEnabled() {
        return configManager.getBooleanProperty("security.headers.csp", true);
    }

    // Audit logging configuration
    public boolean isAuditLogEnabled() {
        return configManager.getBooleanProperty("security.audit.enabled", true);
    }

    public String getAuditLogLevel() {
        return configManager.getProperty("security.audit.level", "INFO");
    }
}
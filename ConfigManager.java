package edu.univ.erp.util;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;
    private final String CONFIG_FILE = "config.properties";

    private ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfig() {
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            // Use default values if config file not found
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        properties.setProperty("database.url", "jdbc:mysql://localhost:3306/univ_erp");
        properties.setProperty("database.username", "root");
        properties.setProperty("database.password", "password");
        properties.setProperty("system.name", "University ERP System");
        properties.setProperty("system.version", "1.0.0");
        properties.setProperty("log.level", "INFO");
        properties.setProperty("session.timeout", "3600"); // in seconds
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(properties.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Database configuration methods
    public String getDatabaseUrl() {
        return getProperty("database.url");
    }

    public String getDatabaseUsername() {
        return getProperty("database.username");
    }

    public String getDatabasePassword() {
        return getProperty("database.password");
    }

    // System configuration methods
    public String getSystemName() {
        return getProperty("system.name", "University ERP System");
    }

    public int getSessionTimeout() {
        return getIntProperty("session.timeout", 3600);
    }
}
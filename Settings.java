package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class Settings {
    private String keyName;
    private String value;
    private String description;
    private LocalDateTime updatedAt;

    public Settings(String keyName, String value, String description) {
        this.keyName = keyName;
        this.value = value;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getKeyName() { return keyName; }
    public String getValue() { return value; }
    public String getDescription() { return description; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setValue(String value) { this.value = value; }
    public void setDescription(String description) { this.description = description; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean getValueAsBoolean() {
        return "true".equalsIgnoreCase(value);
    }

    public int getValueAsInt() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return String.format("Settings{key='%s', value='%s', description='%s'}",
                keyName, value, description);
    }
}
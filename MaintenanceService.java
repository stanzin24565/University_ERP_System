package edu.univ.erp.service;

import edu.univ.erp.domain.Settings;
import edu.univ.erp.data.SettingsDAO;

public class MaintenanceService {
    private SettingsDAO settingsDAO;

    public MaintenanceService() {
        this.settingsDAO = new SettingsDAO();
    }

    public boolean isMaintenanceMode() {
        Settings maintenanceSetting = settingsDAO.findByKey("maintenance_mode"); // ← CHANGE TO LOWERCASE
        return maintenanceSetting != null && "true".equalsIgnoreCase(maintenanceSetting.getValue());
    }

    public boolean setMaintenanceMode(boolean enabled) {
        return settingsDAO.updateValue("maintenance_mode", enabled ? "true" : "false"); // ← CHANGE TO LOWERCASE
    }

    public String getSystemVersion() {
        Settings versionSetting = settingsDAO.findByKey("system_version"); // ← CHANGE TO LOWERCASE
        return versionSetting != null ? versionSetting.getValue() : "ERP System v2.1.0";
    }

    public boolean updateSystemSetting(String key, String value) {
        return settingsDAO.updateValue(key, value);
    }
}
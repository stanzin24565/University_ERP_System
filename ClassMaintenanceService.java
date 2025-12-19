package edu.univ.erp.service;

import edu.univ.erp.domain.Settings;
import edu.univ.erp.data.SettingsDAO;

public class ClassMaintenanceService {
    private SettingsDAO settingsDAO;

    public ClassMaintenanceService() {
        this.settingsDAO = new SettingsDAO();
    }

    public boolean isMaintenanceMode() {
        Settings maintenanceSetting = settingsDAO.findByKey("MAINTENANCE_MODE");
        return maintenanceSetting != null && maintenanceSetting.getValueAsBoolean();
    }

    public boolean setMaintenanceMode(boolean enabled) {
        return settingsDAO.updateValue("MAINTENANCE_MODE", enabled ? "true" : "false");
    }

    public String getSystemVersion() {
        Settings versionSetting = settingsDAO.findByKey("SYSTEM_VERSION");
        return versionSetting != null ? versionSetting.getValue() : "1.0.0";
    }

    public boolean updateSystemSetting(String key, String value) {
        return settingsDAO.updateValue(key, value);
    }
}
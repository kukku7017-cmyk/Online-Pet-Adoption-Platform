package com.petadoption.service;

import com.petadoption.dao.SystemSettingDAO;
import com.petadoption.dao.SystemSettingDAOImpl;
import com.petadoption.model.SystemSetting;

import java.util.List;

public class SettingService {

    private final SystemSettingDAO settingDAO;

    public SettingService() {
        this(new SystemSettingDAOImpl());
    }

    public SettingService(SystemSettingDAO settingDAO) {
        this.settingDAO = settingDAO;
    }

    public List<SystemSetting> getAllSettings() {
        return settingDAO.findAll();
    }

    public String getSetting(String key, String defaultValue) {
        return settingDAO.findByKey(key)
            .map(SystemSetting::getSettingValue)
            .orElse(defaultValue);
    }

    public void updateSetting(String key, String value) {
        settingDAO.updateValue(key, value);
    }
}

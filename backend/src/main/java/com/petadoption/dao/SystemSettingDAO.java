package com.petadoption.dao;

import com.petadoption.model.SystemSetting;

import java.util.List;
import java.util.Optional;

public interface SystemSettingDAO {
    Optional<SystemSetting> findByKey(String key);
    List<SystemSetting> findAll();
    void updateValue(String key, String value);
}

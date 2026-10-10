package com.petadoption.model;

import java.time.LocalDateTime;

public class SystemSetting {
    private String settingKey;
    private String settingValue;
    private String description;
    private Boolean isEditable = true;
    private LocalDateTime updatedAt;

    public SystemSetting() {}

    public SystemSetting(String settingKey, String settingValue, String description, Boolean isEditable) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.isEditable = isEditable != null ? isEditable : true;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsEditable() {
        return isEditable;
    }

    public void setIsEditable(Boolean isEditable) {
        this.isEditable = isEditable != null ? isEditable : true;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

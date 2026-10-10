package com.petadoption.service;

import com.petadoption.model.PlatformAnalytics;

import java.util.Map;

public interface AnalyticsService {
    PlatformAnalytics getPlatformAnalytics();
    Map<String, Object> getShelterAnalytics(Long shelterId);
}

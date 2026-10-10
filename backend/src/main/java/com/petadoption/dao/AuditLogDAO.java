package com.petadoption.dao;

import com.petadoption.model.AuditLog;

import java.util.List;

public interface AuditLogDAO {
    void log(AuditLog auditLog);
    List<AuditLog> findRecent(int limit);
}

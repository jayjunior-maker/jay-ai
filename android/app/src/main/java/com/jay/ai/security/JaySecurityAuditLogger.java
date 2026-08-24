package com.jay.ai.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JaySecurityAuditLogger {

    private final List<JaySecurityAudit> auditRecords;

    public JaySecurityAuditLogger() {
        auditRecords = new ArrayList<>();
    }

    /**
     * Adds a security event to the audit log.
     */
    public void log(JaySecurityAudit audit) {

        if (audit == null) {
            return;
        }

        auditRecords.add(audit);
    }

    /**
     * Returns all recorded security events.
     */
    public List<JaySecurityAudit> getRecords() {

        return Collections.unmodifiableList(
                new ArrayList<>(auditRecords)
        );
    }

    /**
     * Returns the number of recorded events.
     */
    public int getRecordCount() {
        return auditRecords.size();
    }

    /**
     * Removes all records from the current in-memory log.
     */
    public void clear() {
        auditRecords.clear();
    }
    }

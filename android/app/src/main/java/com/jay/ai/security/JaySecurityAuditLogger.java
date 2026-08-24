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
    public synchronized void log(JaySecurityAudit audit) {

        if (audit == null) {
            return;
        }

        auditRecords.add(audit);
    }

    /**
     * Returns all recorded security events.
     */
    public synchronized List<JaySecurityAudit> getRecords() {

        return Collections.unmodifiableList(
                new ArrayList<>(auditRecords)
        );
    }

    /**
     * Returns the most recent security events.
     */
    public synchronized List<JaySecurityAudit> getRecentRecords(int limit) {

        if (limit <= 0 || auditRecords.isEmpty()) {
            return Collections.emptyList();
        }

        int startIndex = Math.max(
                0,
                auditRecords.size() - limit
        );

        return Collections.unmodifiableList(
                new ArrayList<>(
                        auditRecords.subList(
                                startIndex,
                                auditRecords.size()
                        )
                )
        );
    }

    /**
     * Returns the number of recorded events.
     */
    public synchronized int getRecordCount() {
        return auditRecords.size();
    }

    /**
     * Removes all records from the current in-memory log.
     */
    public synchronized void clear() {
        auditRecords.clear();
    }
                }

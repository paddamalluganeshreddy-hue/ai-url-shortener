package com.example.urlshortener.orchestration;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "workflow_audit_events")
public class WorkflowAuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long workflowId; private String eventType; private String actor; private String detail; private Instant occurredAt;
    protected WorkflowAuditEvent() { }
    public WorkflowAuditEvent(Long workflowId, String eventType, String actor, String detail) {
        this.workflowId = workflowId; this.eventType = eventType; this.actor = actor; this.detail = detail; occurredAt = Instant.now();
    }
    public Long getId() { return id; } public String getEventType() { return eventType; } public String getActor() { return actor; }
    public String getDetail() { return detail; } public Instant getOccurredAt() { return occurredAt; }
}

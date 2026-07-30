package com.example.urlshortener.orchestration;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "engineering_workflows")
public class EngineeringWorkflow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String scenario;
    private String requirement;
    @Enumerated(EnumType.STRING) private WorkflowStatus status;
    private int planVersion;
    private boolean safeStopRequested;
    private Instant createdAt;
    private Instant completedAt;

    protected EngineeringWorkflow() { }
    public EngineeringWorkflow(String scenario, String requirement) {
        this.scenario = scenario; this.requirement = requirement; status = WorkflowStatus.DRAFT;
        planVersion = 1; createdAt = Instant.now();
    }
    public Long getId() { return id; } public String getScenario() { return scenario; }
    public String getRequirement() { return requirement; } public WorkflowStatus getStatus() { return status; }
    public int getPlanVersion() { return planVersion; } public boolean isSafeStopRequested() { return safeStopRequested; }
    public Instant getCreatedAt() { return createdAt; } public Instant getCompletedAt() { return completedAt; }
    public void begin() { status = WorkflowStatus.RUNNING; }
    public void awaitingApproval() { status = WorkflowStatus.AWAITING_APPROVAL; }
    public void complete() { status = WorkflowStatus.COMPLETED; completedAt = Instant.now(); }
    public void safeStop() { safeStopRequested = true; status = WorkflowStatus.SAFE_STOPPED; }
    public void replan(String requirement) { this.requirement = requirement; planVersion++; status = WorkflowStatus.DRAFT; safeStopRequested = false; completedAt = null; }
}

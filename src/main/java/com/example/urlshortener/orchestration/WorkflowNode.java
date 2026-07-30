package com.example.urlshortener.orchestration;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "workflow_nodes")
public class WorkflowNode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long workflowId;
    private String nodeKey;
    private String dependencies;
    private boolean humanApprovalRequired;
    @Enumerated(EnumType.STRING) private NodeStatus status;
    private int attempts;
    private String outcome;
    private Instant updatedAt;

    protected WorkflowNode() { }
    public WorkflowNode(Long workflowId, String nodeKey, String dependencies, boolean approvalRequired) {
        this.workflowId = workflowId; this.nodeKey = nodeKey; this.dependencies = dependencies;
        this.humanApprovalRequired = approvalRequired; status = NodeStatus.PENDING; updatedAt = Instant.now();
    }
    public Long getId() { return id; } public Long getWorkflowId() { return workflowId; }
    public String getNodeKey() { return nodeKey; } public boolean isHumanApprovalRequired() { return humanApprovalRequired; }
    public NodeStatus getStatus() { return status; } public int getAttempts() { return attempts; } public String getOutcome() { return outcome; }
    public Set<String> dependencies() { return dependencies == null || dependencies.isBlank() ? Set.of() : Arrays.stream(dependencies.split(",")).collect(Collectors.toSet()); }
    public void start() { status = NodeStatus.RUNNING; attempts++; updatedAt = Instant.now(); }
    public void succeed(String outcome) { status = NodeStatus.SUCCEEDED; this.outcome = outcome; updatedAt = Instant.now(); }
    public void awaitApproval() { status = NodeStatus.WAITING_APPROVAL; updatedAt = Instant.now(); }
    public void retry(String detail) { status = NodeStatus.PENDING; outcome = detail; updatedAt = Instant.now(); }
    public void fallback(String detail) { status = NodeStatus.FALLBACK_USED; outcome = detail; updatedAt = Instant.now(); }
    public void reset() { status = NodeStatus.PENDING; attempts = 0; outcome = null; updatedAt = Instant.now(); }
}

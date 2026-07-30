package com.example.urlshortener.orchestration;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkflowAuditRepository extends JpaRepository<WorkflowAuditEvent, Long> { List<WorkflowAuditEvent> findByWorkflowIdOrderByOccurredAt(Long workflowId); long countByEventType(String eventType); }

package com.example.urlshortener.orchestration;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Long> { List<WorkflowNode> findByWorkflowIdOrderById(Long workflowId); }

package com.example.urlshortener.orchestration;

import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkflowRepository extends JpaRepository<EngineeringWorkflow, Long> { }

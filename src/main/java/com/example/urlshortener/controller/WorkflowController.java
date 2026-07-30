package com.example.urlshortener.controller;

import com.example.urlshortener.orchestration.EngineeringWorkflow;
import com.example.urlshortener.orchestration.WorkflowAuditEvent;
import com.example.urlshortener.orchestration.WorkflowNode;
import com.example.urlshortener.orchestration.WorkflowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowService service;
    public WorkflowController(WorkflowService service) { this.service = service; }
    @PostMapping @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public EngineeringWorkflow create(@Valid @RequestBody CreateRequest request) { return service.create(request.scenario(), request.requirement(), request.actor()); }
    @PostMapping("/{id}/execute") public EngineeringWorkflow execute(@PathVariable Long id, @Valid @RequestBody ActorRequest request) { return service.execute(id, request.actor()); }
    @PostMapping("/{id}/nodes/{nodeKey}/approve") public EngineeringWorkflow approve(@PathVariable Long id, @PathVariable String nodeKey, @Valid @RequestBody ActorRequest request) { return service.approve(id, nodeKey, request.actor()); }
    @PostMapping("/{id}/nodes/{nodeKey}/failure") public EngineeringWorkflow failure(@PathVariable Long id, @PathVariable String nodeKey, @Valid @RequestBody FailureRequest request) { return service.recordFailure(id, nodeKey, request.reason(), request.actor()); }
    @PostMapping("/{id}/safe-stop") public EngineeringWorkflow safeStop(@PathVariable Long id, @Valid @RequestBody StopRequest request) { return service.safeStop(id, request.actor(), request.reason()); }
    @PostMapping("/{id}/replan") public EngineeringWorkflow replan(@PathVariable Long id, @Valid @RequestBody ReplanRequest request) { return service.replan(id, request.requirement(), request.actor()); }
    @GetMapping("/{id}") public EngineeringWorkflow get(@PathVariable Long id) { return service.get(id); }
    @GetMapping("/{id}/graph") public List<WorkflowNode> graph(@PathVariable Long id) { return service.graph(id); }
    @GetMapping("/{id}/audit") public List<WorkflowAuditEvent> audit(@PathVariable Long id) { return service.auditTrail(id); }
    @GetMapping("/metrics") public WorkflowService.ReliabilityMetrics metrics() { return service.metrics(); }
    public record CreateRequest(@NotBlank String scenario, @NotBlank String requirement, @NotBlank String actor) { }
    public record ActorRequest(@NotBlank String actor) { }
    public record FailureRequest(@NotBlank String actor, @NotBlank String reason) { }
    public record StopRequest(@NotBlank String actor, @NotBlank String reason) { }
    public record ReplanRequest(@NotBlank String actor, @NotBlank String requirement) { }
}

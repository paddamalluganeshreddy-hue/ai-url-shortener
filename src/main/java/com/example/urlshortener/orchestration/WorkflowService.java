package com.example.urlshortener.orchestration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Stateful, durable DAG executor. Nodes model the SDLC stages rather than autonomous production deployment. */
@Service
@Transactional
public class WorkflowService {
    private static final int MAX_ATTEMPTS = 2;
    private final WorkflowRepository workflows;
    private final WorkflowNodeRepository nodes;
    private final WorkflowAuditRepository audits;

    public WorkflowService(WorkflowRepository workflows, WorkflowNodeRepository nodes, WorkflowAuditRepository audits) {
        this.workflows = workflows; this.nodes = nodes; this.audits = audits;
    }

    public EngineeringWorkflow create(String scenario, String requirement, String actor) {
        EngineeringWorkflow workflow = workflows.save(new EngineeringWorkflow(scenario, requirement));
        nodes.saveAll(List.of(
                new WorkflowNode(workflow.getId(), "understand", "", false),
                new WorkflowNode(workflow.getId(), "design", "understand", false),
                new WorkflowNode(workflow.getId(), "implement", "design", false),
                new WorkflowNode(workflow.getId(), "test", "implement", false),
                new WorkflowNode(workflow.getId(), "document", "design", false),
                new WorkflowNode(workflow.getId(), "release-readiness", "test,document", true)));
        audit(workflow.getId(), "WORKFLOW_CREATED", actor, "Plan v1 created with six dependency-graph nodes");
        return workflow;
    }

    public EngineeringWorkflow execute(Long workflowId, String actor) {
        EngineeringWorkflow workflow = get(workflowId);
        if (workflow.isSafeStopRequested()) return workflow;
        workflow.begin();
        boolean madeProgress;
        do {
            madeProgress = false;
            List<WorkflowNode> graph = nodes.findByWorkflowIdOrderById(workflowId);
            Map<String, WorkflowNode> byKey = graph.stream().collect(Collectors.toMap(WorkflowNode::getNodeKey, Function.identity()));
            for (WorkflowNode node : graph) {
                if (node.getStatus() == NodeStatus.PENDING && dependenciesSucceeded(node, byKey)) {
                    if (node.isHumanApprovalRequired()) {
                        node.awaitApproval(); workflow.awaitingApproval();
                        audit(workflowId, "APPROVAL_REQUIRED", actor, node.getNodeKey() + " requires release-owner approval");
                    } else {
                        node.start(); node.succeed(outcomeFor(node.getNodeKey(), workflow));
                        audit(workflowId, "NODE_SUCCEEDED", actor, node.getNodeKey() + " completed");
                    }
                    madeProgress = true;
                }
            }
        } while (madeProgress && !workflow.isSafeStopRequested() && workflow.getStatus() != WorkflowStatus.AWAITING_APPROVAL);
        if (nodes.findByWorkflowIdOrderById(workflowId).stream().allMatch(n -> n.getStatus() == NodeStatus.SUCCEEDED)) {
            workflow.complete(); audit(workflowId, "WORKFLOW_COMPLETED", actor, "All entry/exit gates satisfied");
        }
        return workflows.save(workflow);
    }

    public EngineeringWorkflow approve(Long workflowId, String nodeKey, String approver) {
        EngineeringWorkflow workflow = get(workflowId);
        WorkflowNode node = findNode(workflowId, nodeKey);
        if (node.getStatus() != NodeStatus.WAITING_APPROVAL) throw new IllegalStateException("Node is not awaiting approval");
        node.succeed("Approved by " + approver + "; controlled action released");
        audit(workflowId, "HUMAN_APPROVED", approver, nodeKey + " approved");
        workflow.begin();
        return execute(workflowId, approver);
    }

    public EngineeringWorkflow recordFailure(Long workflowId, String nodeKey, String reason, String actor) {
        WorkflowNode node = findNode(workflowId, nodeKey);
        if (node.getAttempts() < MAX_ATTEMPTS) {
            node.retry(reason); audit(workflowId, "RETRY_SCHEDULED", actor, nodeKey + ": " + reason);
        } else {
            node.fallback("Human handoff required after bounded retries: " + reason);
            audit(workflowId, "FALLBACK_USED", actor, nodeKey + ": " + reason);
        }
        return get(workflowId);
    }

    public EngineeringWorkflow safeStop(Long workflowId, String actor, String reason) {
        EngineeringWorkflow workflow = get(workflowId); workflow.safeStop();
        audit(workflowId, "SAFE_STOP", actor, reason); return workflows.save(workflow);
    }

    public EngineeringWorkflow replan(Long workflowId, String changedRequirement, String actor) {
        EngineeringWorkflow workflow = get(workflowId); workflow.replan(changedRequirement);
        List<WorkflowNode> graph = nodes.findByWorkflowIdOrderById(workflowId);
        graph.stream().filter(n -> !Set.of("understand", "design").contains(n.getNodeKey())).forEach(WorkflowNode::reset);
        audit(workflowId, "REPLANNED", actor, "Plan v" + workflow.getPlanVersion() + ": downstream nodes reset after requirement change");
        return workflows.save(workflow);
    }

    @Transactional(readOnly = true) public EngineeringWorkflow get(Long id) { return workflows.findById(id).orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + id)); }
    @Transactional(readOnly = true) public List<WorkflowNode> graph(Long id) { get(id); return nodes.findByWorkflowIdOrderById(id); }
    @Transactional(readOnly = true) public List<WorkflowAuditEvent> auditTrail(Long id) { get(id); return audits.findByWorkflowIdOrderByOccurredAt(id); }
    @Transactional(readOnly = true) public ReliabilityMetrics metrics() {
        List<EngineeringWorkflow> all = workflows.findAll(); long terminal = all.stream().filter(w -> w.getStatus() == WorkflowStatus.COMPLETED || w.getStatus() == WorkflowStatus.SAFE_STOPPED).count();
        long completed = all.stream().filter(w -> w.getStatus() == WorkflowStatus.COMPLETED).count();
        return new ReliabilityMetrics(all.size(), terminal == 0 ? 0 : (double) completed / terminal, audits.countByEventType("RETRY_SCHEDULED"), audits.countByEventType("FALLBACK_USED"), audits.countByEventType("SAFE_STOP"));
    }

    private boolean dependenciesSucceeded(WorkflowNode node, Map<String, WorkflowNode> byKey) { return node.dependencies().stream().allMatch(key -> byKey.get(key).getStatus() == NodeStatus.SUCCEEDED); }
    private WorkflowNode findNode(Long id, String key) { return graph(id).stream().filter(n -> n.getNodeKey().equals(key)).findFirst().orElseThrow(() -> new IllegalArgumentException("Node not found: " + key)); }
    private void audit(Long id, String type, String actor, String detail) { audits.save(new WorkflowAuditEvent(id, type, actor, detail)); }
    private String outcomeFor(String node, EngineeringWorkflow workflow) { return switch (node) {
        case "understand" -> "Normalized requirement, assumptions, acceptance criteria and risk register";
        case "design" -> "Architecture/API/data-flow decision recorded for plan v" + workflow.getPlanVersion();
        case "implement" -> "Implementation artifact generated under security and change-control policy";
        case "test" -> "Unit/integration validation evidence recorded";
        case "document" -> "Setup, operational limits, and decision lineage documented";
        default -> "Completed";
    }; }

    public record ReliabilityMetrics(long workflowCount, double successRate, long retryCount, long fallbackCount, long safeStopCount) { }
}

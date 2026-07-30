# Requirement decomposition and scenarios

## Normalized problem

Build a URL-shortening API that creates unique aliases, redirects safely, records redirect counts, and exposes analytics. Build a governed execution system that turns a textual engineering requirement into evidence-bearing stages. The prototype assumes URLs are valid absolute HTTP(S) URLs (validation should be tightened in production), aliases are caller-provided, and H2 data is non-durable across application restarts.

## Greenfield: add URL shortening

1. Understand: normalize API contracts, persistence needs, redirect/abuse risks, and acceptance criteria.
2. Design: select `ShortUrl` and append-only `UrlAccess` records, REST endpoints, and a 302 redirect.
3. Implement and document can proceed after design; test depends on implementation.
4. Synchronize test and documentation at release readiness; a human approves release.

Validation: duplicate alias returns 409, unknown alias returns 404, redirect creates one access event, and analytics reports the event count.

## Brownfield: change the analytics contract

Use `POST /api/workflows/{id}/replan` with the changed requirement. The workflow version increases and invalidates implementation/test/document nodes, preserving requirement understanding and design evidence. Re-execute the plan and inspect `/graph` and `/audit` before approving release. This makes impacted artifacts explicit rather than treating a change as a new linear run.

## Ambiguous: "make links safer"

The understand stage should record unresolved decisions before design: permitted schemes/domains, malware scanning provider, retention duration, role that may override blocks, and false-positive handling. Safe action is to stop or use a fallback/human review rather than infer a security policy. Once a human clarifies the decisions, replan and resume with the new version.

## Example workflow API

```http
POST /api/workflows
{"scenario":"greenfield","requirement":"Add click analytics","actor":"product-owner"}

POST /api/workflows/1/execute
{"actor":"engineering-agent"}

GET /api/workflows/1/graph
GET /api/workflows/1/audit

POST /api/workflows/1/nodes/release-readiness/approve
{"actor":"release-manager"}
```

For an injected validation failure, call `POST /api/workflows/{id}/nodes/{nodeKey}/failure` with `actor` and `reason`, then execute again. The audit trail displays the bounded retry or fallback decision.

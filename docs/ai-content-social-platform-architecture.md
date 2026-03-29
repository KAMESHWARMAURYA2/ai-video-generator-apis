# AI-Powered Content Generation & Social Media Automation Platform

## 1) Product Architecture Plan

### 1.1 Product Goals
- Generate publish-ready social assets (images, short videos, captions, hashtags) from prompts.
- Put all generated content through a governed review workflow before posting.
- Automate multi-channel publishing with schedule control and timezone awareness.
- Support multi-account, multi-workspace usage for thousands of active users.
- Provide measurable ROI via analytics and engagement tracking.
- **Free-first implementation:** start with open-source/self-hosted tools while enforcing provider abstraction for paid upgrades later.

### 1.2 Core Modules
1. **Identity & Access Module**
   - User registration/login, RBAC (owner/admin/editor/reviewer/viewer), workspace/tenant isolation.
   - OAuth connections to social platforms and secure token vault management.

2. **Prompt & Campaign Module**
   - Stores prompts, campaign objectives, content themes, brand voice, target persona.
   - Template library for reusable prompt patterns and posting sequences.

3. **AI Generation Orchestrator Module**
   - Routes requests to model providers for image/video/text generation.
   - Handles job orchestration, callback ingestion, quality checks, and model fallback.

4. **Asset Management Module**
   - Stores generated artifacts (images/videos/text metadata) in object storage.
   - Maintains versioning, thumbnails, duration, dimensions, format compliance.

5. **Review & Approval Module**
   - Review queue, edit/approve/reject lifecycle, comments, audit trail, moderation flags.
   - Optional auto-approval rules (e.g., safe content score above threshold).

6. **Scheduling & Calendar Module**
   - Manages one-time and recurring schedules.
   - Timezone conversion, DST-safe execution windows, blackout windows.

7. **Publishing Automation Module**
   - Connector adapters for Instagram, YouTube Shorts, Facebook, X, LinkedIn.
   - Queue-based dispatch, retries, dead-letter queues, idempotent publish requests.

8. **Analytics & Reporting Module**
   - Pulls post-level metrics from platform APIs.
   - Aggregates views/likes/comments/shares/CTR/engagement rate.

9. **Notification Module**
   - Email/in-app/webhook alerts for generation completion, approval needed, posting status.

10. **Observability & Admin Ops Module**
    - Logs, traces, metrics, alerting, quotas, feature flags, rate/usage dashboards.

### 1.3 End-to-End Data Flow
1. User submits prompt (+ style, format, platform goals).
2. Generation Orchestrator creates a generation job and emits `content.requested` event.
3. AI worker(s) generate image/video/caption/hashtags and store assets in object storage.
4. Metadata persisted; content item enters `REVIEW_PENDING` queue.
5. Reviewer edits/approves/rejects; on approval, item moves to scheduling.
6. Scheduler emits `post.ready` at target publish time.
7. Publisher worker picks event, resolves account/platform adapter, posts content.
8. Posting result stored (external post ID, status, retries if failed).
9. Metrics collector periodically fetches engagement stats and updates analytics store.
10. Dashboard API serves near-real-time and historical insights.

---

## 2) High-Level Design (HLD)

### 2.1 Textual Architecture Diagram

```text
[React Web App]
      |
      v
 [API Gateway]
      |
      +--> [Auth Service] --------------------> [PostgreSQL]
      |
      +--> [Content Service] -----------------> [PostgreSQL]
      |          |
      |          +--> [Generation Orchestrator] ---> [Kafka/RabbitMQ]
      |                                      |              |
      |                                      v              v
      |                             [AI Workers]      [Review Queue]
      |                                      |
      |                                      v
      |                               [S3/Object Store]
      |
      +--> [Review Service] -----------------> [PostgreSQL]
      |
      +--> [Scheduler Service] --------------> [Kafka/RabbitMQ]
      |                                              |
      |                                              v
      +--> [Publishing Service] ---> [Platform Adapters: IG/YT/FB/X/LinkedIn]
      |                                              |
      |                                              v
      |                                       [Posting Logs DB]
      |
      +--> [Analytics Service] <------------- [Metrics Collector Workers]
      |
      +--> [Notification Service]

[Observability Stack: Prometheus + Grafana + ELK + OpenTelemetry]
```

### 2.2 Microservices vs Monolith Recommendation

**Phase 1 (MVP): Modular Monolith**
- Spring Boot modular boundaries: auth, content, review, schedule, publish, analytics.
- Faster delivery, lower DevOps complexity.

**Phase 2 (Scale): Progressive Microservices Extraction**
- Extract high-throughput domains first:
  1. Generation Orchestrator + Workers
  2. Publishing Service
  3. Analytics Collector
- Keep shared contracts via events and API versioning.

### 2.3 Recommended Tech Stack
- **Backend:** Java 21, Spring Boot 3.x, Spring Security, Spring Data JPA, Spring Batch (optional).
- **Frontend:** React + TypeScript + Next.js (or Vite SPA), TanStack Query, Material UI/AntD.
- **DB:** PostgreSQL (OLTP), TimescaleDB extension or ClickHouse (analytics scale-out option).
- **Queue/Event Bus:** Kafka (high-throughput) or RabbitMQ (simpler routing).
- **Cache:** Redis (job state caching, rate limiter, session/token metadata).
- **Storage:** AWS S3 (or compatible object store).
- **Search:** OpenSearch/Elasticsearch for audit and content search.
- **Workflow:** Temporal or Camunda for robust long-running orchestration (optional but powerful).
- **Infra:** Docker + Kubernetes, Helm, Terraform, GitHub Actions.
- **Observability:** OpenTelemetry, Prometheus, Grafana, Loki/ELK, Sentry.

---

## 3) Low-Level Design (LLD)

### 3.1 Database Schema (Core Entities)

#### Tenant/User/Auth
- `tenants(id, name, plan, created_at)`
- `users(id, tenant_id, email, password_hash, status, created_at)`
- `roles(id, name)`
- `user_roles(user_id, role_id)`

#### Social Accounts
- `social_accounts(id, tenant_id, platform, account_name, external_account_id, token_ref, refresh_token_ref, token_expiry, status, created_at)`
- `social_account_permissions(id, social_account_id, permission_key)`

#### Content & Generation
- `content_items(id, tenant_id, campaign_id, creator_user_id, status, title, prompt_text, brand_profile_id, created_at, updated_at)`
- `generation_jobs(id, content_item_id, job_type[IMAGE|VIDEO|TEXT], provider, provider_job_id, status, request_json, response_json, error_code, created_at, updated_at)`
- `assets(id, content_item_id, asset_type[IMAGE|VIDEO|TEXT], storage_url, mime_type, width, height, duration_sec, version_no, checksum, created_at)`
- `captions(id, content_item_id, caption_text, hashtags_json, language, version_no, created_at)`

#### Review Workflow
- `review_tasks(id, content_item_id, assigned_to, status[PENDING|APPROVED|REJECTED|CHANGES_REQUESTED], due_at, created_at, updated_at)`
- `review_comments(id, review_task_id, user_id, comment_text, created_at)`
- `review_audit_log(id, content_item_id, action, actor_user_id, before_json, after_json, created_at)`

#### Scheduling & Publishing
- `publish_schedules(id, content_item_id, social_account_id, scheduled_at_utc, timezone, status[SCHEDULED|CANCELLED|POSTED|FAILED], created_at)`
- `publish_jobs(id, schedule_id, platform, idempotency_key, status, attempt_count, next_retry_at, last_error, created_at, updated_at)`
- `published_posts(id, publish_job_id, external_post_id, external_url, posted_at, raw_response_json)`
- `dead_letter_jobs(id, job_type, payload_json, reason, created_at)`

#### Analytics
- `post_metrics(id, published_post_id, metric_timestamp, views, likes, comments, shares, saves, clicks, watch_time_sec)`
- `metric_sync_state(id, social_account_id, last_sync_at, cursor)`

### 3.2 Key API Contracts (Illustrative)

#### Create Generation Request
`POST /api/v1/content/generate`

Request:
```json
{
  "campaignId": "cmp_123",
  "prompt": "Create a futuristic gym reel for beginners",
  "outputs": ["IMAGE", "VIDEO", "CAPTION"],
  "style": {
    "tone": "motivational",
    "aspectRatio": "9:16",
    "durationSec": 20
  }
}
```

Response:
```json
{
  "contentItemId": "cnt_987",
  "generationJobIds": ["gj_1", "gj_2", "gj_3"],
  "status": "IN_PROGRESS"
}
```

#### Review Action
`POST /api/v1/review/tasks/{taskId}/action`

Request:
```json
{
  "action": "APPROVE",
  "comment": "Looks good, publish to all channels",
  "editedCaption": "Your future self starts now. #FitnessJourney"
}
```

Response:
```json
{
  "taskId": "rvw_456",
  "status": "APPROVED",
  "contentStatus": "READY_TO_SCHEDULE"
}
```

#### Schedule Publish
`POST /api/v1/publish/schedules`

Request:
```json
{
  "contentItemId": "cnt_987",
  "targets": [
    {"socialAccountId": "sa_ig_1", "scheduledAt": "2026-04-01T17:00:00", "timezone": "America/New_York"},
    {"socialAccountId": "sa_yt_1", "scheduledAt": "2026-04-01T17:05:00", "timezone": "America/New_York"}
  ]
}
```

Response:
```json
{
  "scheduleIds": ["sch_1", "sch_2"],
  "status": "SCHEDULED"
}
```

#### Analytics Query
`GET /api/v1/analytics/posts?from=2026-03-01&to=2026-03-31&platform=INSTAGRAM`

Response:
```json
{
  "summary": {
    "views": 240000,
    "engagementRate": 0.072
  },
  "items": [
    {
      "postId": "pst_1",
      "views": 12000,
      "likes": 800,
      "comments": 120
    }
  ]
}
```

### 3.3 Key Components / Classes (Spring Boot)
- `GenerationController`, `ReviewController`, `PublishController`, `AnalyticsController`
- `GenerationOrchestratorService`
- `PromptTemplateService`
- `AssetStorageService` (S3 adapter)
- `ReviewWorkflowService`
- `ScheduleService` + `TimezoneResolver`
- `PublishDispatcherService`
- `PlatformAdapter` (interface) + concrete adapters:
  - `InstagramAdapter`, `YouTubeShortsAdapter`, `FacebookAdapter`, `XAdapter`, `LinkedInAdapter`
- `RetryPolicyEngine`
- `RateLimitCoordinator` (Redis token bucket/leaky bucket)
- `MetricsCollectorService`
- `AuditLogService`

---

## 4) Content Generation Pipeline Design

### 4.1 Pipeline Stages
1. **Prompt Intake & Validation**
   - Validate profanity/safety, required dimensions, platform constraints.
2. **Prompt Enrichment**
   - Inject brand style guide, historical best-performing tags, target audience hints.
3. **Generation Orchestration**
   - Parallel fan-out jobs for image/video/caption generation.
4. **Model Invocation**
   - Use provider abstraction for interchangeable AI vendors.
5. **Post-Processing**
   - Video transcoding (H.264/AAC), image resizing, caption normalization, hashtag dedupe.
6. **Storage & Metadata**
   - Save binaries to object store; metadata to PostgreSQL.
7. **Quality Gates**
   - Safety moderation, watermark/brand compliance, technical checks (duration, ratio).
8. **Review Queue Entry**
   - Mark `REVIEW_PENDING`, assign reviewer per workspace policy.

### 4.2 Reliability Patterns
- Idempotent generation request key.
- Outbox pattern for DB-to-queue consistency.
- Circuit breaker and provider fallback strategy.
- Saga-like compensation for failed multi-asset generation.

---

## 5) Social Media Posting System Design

### 5.1 Queue-Based Publishing Flow
1. Scheduler emits `post.ready` event at `scheduled_at_utc`.
2. Publisher consumer locks job with idempotency key.
3. Fetch assets + transformed caption per platform policy.
4. Call platform adapter API.
5. Persist success/failure and emit `post.result` event.

### 5.2 Retry Mechanism
- Retry on transient failures (5xx, timeout, rate-limit) with exponential backoff + jitter.
- Sample policy: 1m, 5m, 15m, 1h, 6h; max 6 attempts.
- Permanent failures (4xx validation/auth) go straight to DLQ.
- Automatic token refresh attempt before marking auth failure.

### 5.3 Rate Limiting Strategy
- Central rate policy registry by platform + endpoint.
- Redis-based distributed token bucket.
- Per-tenant and per-account quotas to prevent noisy neighbor issues.
- Adaptive throttling when platform returns rate-limit headers.

### 5.4 Idempotency & Exactly-Once Semantics (Practical)
- Unique `idempotency_key = platform + social_account_id + content_item_id + scheduled_at`.
- At-least-once delivery from queue, exactly-once effect achieved with idempotency table.

---

## 6) Suggested Implementation Stack (Free-First, Upgrade-Ready)


### 6.1 Free-First + Easy Paid Migration Principles
1. **Provider Adapter Pattern Everywhere**
   - Define interfaces (`ImageGenProvider`, `VideoGenProvider`, `CaptionGenProvider`, `PublisherProvider`).
   - Keep provider-specific payload mapping in adapter classes only.
2. **Canonical Domain Model**
   - Store normalized request/response model in DB (`canonical_prompt`, `generation_params`, `canonical_metrics`).
   - Persist raw provider payload in `provider_payload_json` for diagnostics only.
3. **Configuration-driven Provider Routing**
   - Per tenant/workspace choose provider by config flag (`provider_strategy`: `OPEN_SOURCE`, `HYBRID`, `PREMIUM`).
4. **No Vendor Lock-In in Core Tables**
   - Avoid columns tied to single vendor naming; use generic `provider`, `provider_job_id`, `provider_cost`.
5. **Shadow Mode for Migration**
   - Support dual-write/dual-run (open-source primary, paid provider shadow inference) to compare quality and cost.

### Backend (Spring-first, provider-agnostic contracts)
- Java 21 + Spring Boot 3.x
- Spring Web, Spring Security OAuth2 Client, Spring Data JPA
- Flyway for DB migrations
- Resilience4j for circuit breaker/retry
- Redis + Redisson

### Frontend (React)
- React + TypeScript + Next.js
- Zustand/Redux Toolkit for state
- React Query for server state
- Charting: Recharts/ECharts for analytics dashboards

### Storage/Queue/DB
- S3 for media assets
- Kafka for event streaming (`content.requested`, `review.pending`, `post.ready`, `metrics.updated`)
- PostgreSQL for transactional data
- Optional ClickHouse for large analytics scale

---

## 7A) Open-Source Tools/APIs (Default Phase-1 Implementation)

1. **Model Serving / AI Tooling**
   - Stable Diffusion (images)
   - ComfyUI (visual generation workflows)
   - FFmpeg (video post-processing)
   - Whisper (transcription/caption utility)

2. **Orchestration & Jobs**
   - Temporal (workflow orchestration)
   - Kafka / RabbitMQ
   - Debezium (CDC/eventing)

3. **Backend/Foundation**
   - Spring Boot, Keycloak (IAM), PostgreSQL, Redis
   - MinIO (S3-compatible local/private object storage)

4. **Observability**
   - Prometheus, Grafana, Loki, Jaeger/OpenTelemetry

5. **Cost Profile (Open-source)
   - License: typically $0 software license cost.
   - Infra costs still apply: compute, GPU, storage, network egress, ops labor.
   - Practical monthly estimate for small production (100–500 active users):
     - Cloud compute + DB + cache + storage: **~$500–$2,500/month**
     - With dedicated GPU workloads: **+$1,000–$8,000/month** depending utilization.

---

## 7B) Paid APIs/Tools with Rough Cost Estimation (Phase-2/3 Plug-in Replacement)

> Costs vary by region, usage, negotiated enterprise contracts, and platform API tiers. Use these as planning bands, not quotes.

1. **Generative AI APIs**
   - Text/caption generation APIs (token-based pricing):
     - Early stage: **$100–$1,500/month**
     - Scale stage: **$2,000–$20,000+/month**
   - Image/video generation APIs:
     - Pilot: **$300–$3,000/month**
     - Growth: **$5,000–$50,000+/month**

2. **Cloud Storage/CDN**
   - Object storage + CDN + egress:
     - **$100–$5,000+/month** depending media volume.

3. **Managed Queue/Streaming**
   - Managed Kafka/queue:
     - **$200–$3,000+/month**.

4. **Monitoring/Logs (Managed)**
   - Datadog/New Relic/Sentry tiers:
     - **$100–$4,000+/month**.

5. **Social Platforms**
   - API access is often free but gated by app review/permissions; costs are indirect:
     - Engineering maintenance, API compliance, rate-limit handling, policy changes.

6. **Representative Total Monthly Budget Bands**
   - **MVP (up to ~1k users): $1,500–$10,000/month**
   - **Growth (1k–20k users): $10,000–$80,000/month**
   - **Large scale (20k+ users): $80,000+/month**

---

## 8) Scalability, Fault Tolerance, Extensibility Blueprint

### 8.1 Scalability
- Stateless API services horizontally autoscaled (Kubernetes HPA).
- Async-heavy architecture for generation and publishing workloads.
- Partition queues by tenant/platform for parallelism and isolation.
- Read replicas and query optimization for analytics endpoints.

### 8.2 Fault Tolerance
- Retry + DLQ + replay tools.
- Multi-AZ DB deployments and automated backups.
- Graceful degradation: queue build-up handling, fallback models, partial publish reporting.
- End-to-end tracing for fast incident triage.

### 8.3 Extensibility
- Platform adapter interface lets you add TikTok/Pinterest later with minimal core changes.
- Provider abstraction for AI vendors avoids lock-in.
- Versioned APIs/events (`v1`, `v2`) to evolve without breaking clients.
- Feature flags for progressive rollout of new generation models and posting channels.

---

## 9) Suggested Delivery Roadmap

### Phase 1 (0–8 weeks)
- Prompt → generation → review → manual publish.
- 2 platforms (Instagram + X) + basic analytics.

### Phase 2 (8–16 weeks)
- Scheduler, multi-account, auto-posting, retry/DLQ.
- Add YouTube Shorts, Facebook, LinkedIn.

### Phase 3 (16+ weeks)
- Advanced analytics, A/B variants, recommendation engine, cost optimization layer.
- Tenant quotas, usage billing, enterprise controls.

---

## 10) Final Recommendations
- Start with modular monolith + event-driven internals to reduce initial complexity.
- Isolate AI generation and publishing early because they scale differently.
- Invest in review/audit and moderation from day one (critical for brand safety).
- Build observability and idempotency first; they pay off quickly at scale.


---


## 11) Free-First Implementation Blueprint (Actionable)

### 11.1 What to Build First (Open-source Defaults)
- **Image generation:** Stable Diffusion/SDXL via ComfyUI service.
- **Caption + hashtag generation:** local/self-hosted LLM endpoint (or small OSS model gateway).
- **Video assembly:** FFmpeg pipeline + template engine for reels.
- **Storage:** MinIO in non-prod, S3 in prod with same S3 API.
- **Queue:** RabbitMQ first (simple), switch to Kafka by replacing event transport adapter.

### 11.2 What Must Be Abstracted from Day 1
- AI model invocation contract (`generateImage`, `generateVideo`, `generateCaption`).
- Social publish contract (`publishPost`, `getPostMetrics`, `refreshToken`).
- Cost/latency telemetry contract independent of provider.
- Safety/moderation contract so you can swap moderation vendors.

### 11.3 Migration Path to Paid Tools
1. Add paid provider adapter (no API contract changes to frontend).
2. Route small traffic percentage via feature flag.
3. Compare quality/latency/cost in analytics dashboard.
4. Gradually increase traffic and keep open-source as fallback.

### 11.4 Non-Functional Guardrails
- Every provider call must be idempotent, time-limited, and circuit-breaker-protected.
- Every job stores provider, model version, latency, and estimated cost for future optimization.
- Keep generated assets in your own storage to avoid dependency on provider asset URLs.

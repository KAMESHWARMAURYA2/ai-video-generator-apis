# AI Video Generator API

Spring Boot application with:

1. Legacy video generation endpoint compatibility.
2. Free-first AI content generation + review + scheduling workflow APIs.
3. Phase-1 production hardening: PostgreSQL persistence, Flyway migrations, JWT auth.

## Prerequisites

- Java 17+
- PostgreSQL (default: `localhost:5432/ai_video_generator`)

Set environment variables as needed:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/ai_video_generator
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET='replace-with-strong-32-char-minimum-secret'
```

## Authentication endpoints

### Register
`POST /api/v1/auth/register`

```json
{
  "email": "admin@example.com",
  "password": "strong-password"
}
```

### Login
`POST /api/v1/auth/login`

Returns a JWT token. Pass it in `Authorization: Bearer <token>` for protected `/api/v1/**` routes.

## Platform workflow endpoints (JWT required)

### 1) Generate content
`POST /api/v1/content/generate`

```json
{
  "prompt": "Create a motivational fitness reel",
  "outputs": ["IMAGE", "VIDEO", "CAPTION"]
}
```

### 2) Fetch generated content
`GET /api/v1/content/{contentId}`

### 3) Approve/reject review task
`POST /api/v1/review/tasks/{taskId}/action`

```json
{
  "action": "APPROVE",
  "comment": "Looks good",
  "editedCaption": "Optional edited caption"
}
```

### 4) Schedule posting (timezone-aware)
`POST /api/v1/publish/schedules`

```json
{
  "contentItemId": "cnt_xxx",
  "targets": [
    {
      "socialAccountId": "acc_1",
      "scheduledAt": "2026-04-01T17:00:00",
      "timezone": "America/New_York"
    }
  ]
}
```

### 5) Check schedule status
`GET /api/v1/publish/schedules/{scheduleId}`

## Legacy endpoint (existing)

- `POST /generate-video`
- `POST /videos/generate`

## Design note
Provider interfaces (`ImageGenProvider`, `VideoGenProvider`, `CaptionGenProvider`, `PublisherProvider`) are intentionally abstraction-first so open-source defaults can be swapped with paid providers later without API contract changes.

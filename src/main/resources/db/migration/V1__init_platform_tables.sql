CREATE TABLE IF NOT EXISTS app_users (
    id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS content_items (
    id VARCHAR(64) PRIMARY KEY,
    prompt VARCHAR(2000) NOT NULL,
    requested_outputs_json TEXT,
    asset_urls_json TEXT,
    caption TEXT,
    hashtags_json TEXT,
    status VARCHAR(64) NOT NULL,
    review_task_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS review_tasks (
    id VARCHAR(64) PRIMARY KEY,
    content_item_id VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    reviewer_comment TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS publish_schedules (
    id VARCHAR(64) PRIMARY KEY,
    content_item_id VARCHAR(64) NOT NULL,
    social_account_id VARCHAR(255) NOT NULL,
    timezone VARCHAR(128) NOT NULL,
    scheduled_at_utc TIMESTAMPTZ NOT NULL,
    status VARCHAR(64) NOT NULL,
    external_post_id VARCHAR(255),
    attempts INT NOT NULL,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_publish_due ON publish_schedules(status, scheduled_at_utc);

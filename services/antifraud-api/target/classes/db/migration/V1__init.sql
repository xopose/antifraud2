CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(64) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    account_id BIGINT NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    merchant VARCHAR(128) NOT NULL,
    country VARCHAR(8) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'APPROVED',
    payload JSONB,
    ingested_at TIMESTAMPTZ NOT NULL,
    source VARCHAR(16) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_transactions_account_created ON transactions(account_id, created_at DESC);

CREATE TABLE IF NOT EXISTS fraud_alerts (
  id BIGSERIAL PRIMARY KEY,
  transaction_id VARCHAR(64) NOT NULL,
  account_id BIGINT NOT NULL,
  rule_code VARCHAR(64) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  description TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  resolved SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_alerts_created ON fraud_alerts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_rule ON fraud_alerts(rule_code);

CREATE TABLE IF NOT EXISTS fraud_rules (
  id SERIAL PRIMARY KEY,
  code VARCHAR(64) UNIQUE NOT NULL,
  title VARCHAR(128) NOT NULL,
  description TEXT NOT NULL,
  threshold DOUBLE PRECISION,
  enabled SMALLINT NOT NULL DEFAULT 1,
  severity VARCHAR(16) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO fraud_rules(code,title,description,threshold,enabled,severity,created_at)
VALUES
    ('HIGH_AMOUNT','High amount','Amount > threshold',10000,1,'HIGH', NOW()),
    ('VELOCITY','Velocity','More than threshold tx within 2 minutes (threshold=N)',5,1,'MEDIUM', NOW()),
    ('GEO_JUMP','Geo jump','Country changed within 60 seconds (threshold=seconds)',60,1,'MEDIUM', NOW())
    ON CONFLICT (code) DO NOTHING;

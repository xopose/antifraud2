from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime
import psycopg2

#Даг для обновления витрины
def refresh_cdm():
    conn = psycopg2.connect(
        host="antifraud-postgres",
        port=5432,
        dbname="antifraud",
        user="antifraud",
        password="antifraud"
    )
    conn.autocommit = True
    cur = conn.cursor()

    cur.execute("""
                CREATE TABLE IF NOT EXISTS cdm_tx_per_minute (
                                                                 minute_ts TIMESTAMPTZ PRIMARY KEY,
                                                                 tx_count BIGINT NOT NULL
                );

                CREATE TABLE IF NOT EXISTS cdm_fraud_total (
                                                               id SMALLINT PRIMARY KEY DEFAULT 1,
                                                               total_alerts BIGINT NOT NULL,
                                                               updated_at TIMESTAMPTZ NOT NULL
                );

                CREATE TABLE IF NOT EXISTS cdm_fraud_top_merchants (
                                                                       merchant VARCHAR(128) PRIMARY KEY,
                    alerts_count BIGINT NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                    );

                CREATE TABLE IF NOT EXISTS cdm_fraud_top_accounts (
                                                                      account_id BIGINT PRIMARY KEY,
                                                                      alerts_count BIGINT NOT NULL,
                                                                      updated_at TIMESTAMPTZ NOT NULL
                );

                CREATE TABLE IF NOT EXISTS cdm_fraud_rules_share (
                                                                     rule_code VARCHAR(64) PRIMARY KEY,
                    alerts_count BIGINT NOT NULL,
                    percentage NUMERIC(5,2) NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                    );

                CREATE TABLE IF NOT EXISTS cdm_fraud_by_country (
                                                                    country VARCHAR(8) PRIMARY KEY,
                    alerts_count BIGINT NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                    );

                CREATE TABLE IF NOT EXISTS cdm_blocked_tx_per_minute (
                                                                         minute_ts TIMESTAMPTZ PRIMARY KEY,
                                                                         blocked_count BIGINT NOT NULL
                );

                CREATE TABLE IF NOT EXISTS cdm_saved_money (
                                                               id SMALLINT PRIMARY KEY DEFAULT 1,
                                                               total_saved NUMERIC(18,2) NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                    );
                """)

    cur.execute("""
                INSERT INTO cdm_tx_per_minute
                SELECT date_trunc('minute', created_at), COUNT(*)
                FROM transactions
                GROUP BY 1
                    ON CONFLICT (minute_ts) DO UPDATE
                                                   SET tx_count = EXCLUDED.tx_count;

                INSERT INTO cdm_fraud_total (id, total_alerts, updated_at)
                SELECT 1, COUNT(*), now()
                FROM fraud_alerts
                    ON CONFLICT (id) DO UPDATE
                                            SET total_alerts = EXCLUDED.total_alerts,
                                            updated_at = EXCLUDED.updated_at;

                TRUNCATE cdm_fraud_top_merchants;
                INSERT INTO cdm_fraud_top_merchants
                SELECT t.merchant, COUNT(*), now()
                FROM fraud_alerts f
                         JOIN transactions t USING(transaction_id)
                GROUP BY t.merchant
                ORDER BY COUNT(*) DESC
                    LIMIT 10;

                TRUNCATE cdm_fraud_top_accounts;
                INSERT INTO cdm_fraud_top_accounts
                SELECT account_id, COUNT(*), now()
                FROM fraud_alerts
                GROUP BY account_id
                ORDER BY COUNT(*) DESC
                    LIMIT 10;

                WITH total AS (SELECT COUNT(*) cnt FROM fraud_alerts)
                INSERT INTO cdm_fraud_rules_share
                SELECT
                    rule_code,
                    COUNT(*),
                    ROUND(COUNT(*) * 100.0 / total.cnt, 2),
                    now()
                FROM fraud_alerts, total
                GROUP BY rule_code, total.cnt
                    ON CONFLICT (rule_code) DO UPDATE
                                                   SET alerts_count = EXCLUDED.alerts_count,
                                                   percentage = EXCLUDED.percentage,
                                                   updated_at = EXCLUDED.updated_at;

                INSERT INTO cdm_fraud_by_country
                SELECT t.country, COUNT(*), now()
                FROM fraud_alerts f
                         JOIN transactions t USING(transaction_id)
                GROUP BY t.country
                    ON CONFLICT (country) DO UPDATE
                                                 SET alerts_count = EXCLUDED.alerts_count,
                                                 updated_at = EXCLUDED.updated_at;

                INSERT INTO cdm_blocked_tx_per_minute
                SELECT date_trunc('minute', created_at), COUNT(*)
                FROM transactions
                WHERE status = 'DECLINED'
                GROUP BY 1
                    ON CONFLICT (minute_ts) DO UPDATE
                                                   SET blocked_count = EXCLUDED.blocked_count;

                INSERT INTO cdm_saved_money (id, total_saved, updated_at)
                SELECT 1, COALESCE(SUM(amount),0), now()
                FROM transactions
                WHERE status = 'DECLINED'
                    ON CONFLICT (id) DO UPDATE
                                            SET total_saved = EXCLUDED.total_saved,
                                            updated_at = EXCLUDED.updated_at;
                """)

    cur.close()
    conn.close()


with DAG(
        dag_id="fraud_cdm_refresh",
        start_date=datetime(2026, 1, 1),
        schedule_interval="* * * * *",
        catchup=False,
        max_active_runs=1,
        tags=["fraud", "cdm"],
) as dag:

    refresh_task = PythonOperator(
        task_id="refresh_cdm",
        python_callable=refresh_cdm
    )

# Антифрод в банковских транзакциях

Этот репозиторий — **прототип антифрод-системы**:
- приём транзакций из **HTTP / Kafka / CSV**
- проверка транзакций по **3 правилам фрода** (кастомизируемые пороги в БД)
- запись транзакций и алертов в **PostgreSQL**
- **мониторинг** (Prometheus + Grafana) и **аналитика** (Superset)
---
## Стек технологий
- PostgreSQL
- Kafka
- Superset
- Prometheus
- Grafana
- Java/Spring

## Быстрый старт

### 1) Запуск всей инфраструктуры
```bash
docker compose up -d --build
```

Поднимется:
- antifraud-api (Spring Boot): http://localhost:8080
- Kafka (2 брокера) + Kafka UI: http://localhost:8089
- Postgres: localhost:5432 (db=antifraud, user=antifraud, pass=antifraud)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- Superset: http://localhost:8088 (admin/admin)
- Airflow: http://localhost:8081 (airflow/airflow)

### 2) Проверить работу REST API
Коллекция postman лежит в папке postman в корне проекта
### 3) Реальные транзакции в фоне (через Kafka)
Контейнер `tx-generator` публикует транзакции в Kafka в реальном времени.  
Сервис `antifraud-api` потребляет их и пишет в БД + создаёт фрод-алерты.

---

## Правила фрода (минимум 3)

Описаны и реализованы в `FraudEngine`:

1) **HIGH_AMOUNT** — сумма > threshold (по умолчанию 10_000)  
2) **VELOCITY** — более N транзакций за окно минут (по умолчанию N=5 за 2 минуты)  
3) **GEO_JUMP** — смена страны менее чем за X секунд (по умолчанию 60 секунд)

Все параметры лежат в таблице `fraud_rules` и меняются через API:
- `GET /rules`
- `PUT /rules/{code}`

---

## CSV-источник и тестовые данные

Папка `data/` содержит:
- `transactions_sample.csv` — небольшой пример(я не хочу 200к строк в гит)
- `generate_transactions_csv.py` — генератор CSV (в т.ч. на 200_000 строк)

Сгенерировать **200k**:
```bash
#linux
python3 data/generate_transactions_csv.py --out data/transactions_sample.csv --rows 200000
#winfows 
python3 generate_transactions_csv.py
```

Импорт/ETL в Airflow: DAG `airflow/dags/etl_transactions_daily.py`

---

## Мониторинг и дашборды

### Grafana
- URL: http://localhost:3000
- Логин: admin / admin
- Дашборды провиженятся из `monitoring/grafana/dashboards/`
- Экспорт в JSON: `scripts/export_grafana_dashboard.sh`

### Superset
- URL: http://localhost:8088
- Логин: admin / admin
- В проекте есть пример экспортируемого набора: `dashboards/superset_export.zip`
- Экспорт/импорт через CLI: `scripts/superset_export_import.sh`

---

## Демонстрация отказоустойчивости

**Сценарий: остановить один брокер Kafka**
```bash
docker stop kafka-2
```

Далее:
- POST /transactions продолжает работать (HTTP источник)
- генератор продолжит публиковать (переедет на kafka-1)
- consumer продолжит потребление (при корректных репликах)

Вернуть брокер:
```bash
docker start kafka-2
```

## Дмаграммы
- [01_component_diagram.puml](diagrams/01_component_diagram.puml) | High-level system components and their interactions |
- [02_class_diagram_entities.puml](diagrams/02_class_diagram_entities.puml) | JPA entities and DTOs with relationships |
- [03_class_diagram_services.puml](diagrams/03_class_diagram_services.puml) | Service layer, controllers, and repositories |
- [04_erd_database.puml](diagrams/04_erd_database.puml) | Database schema including CDM tables created by Airflow |
- [05_sequence_http_transaction.puml](diagrams/05_sequence_http_transaction.puml) | HTTP transaction processing flow |
- [06_sequence_kafka_flow.puml](diagrams/06_sequence_kafka_flow.puml) | Kafka message streaming flow |
- [07_sequence_airflow_etl.puml](diagrams/07_sequence_airflow_etl.puml) | Airflow ETL DAG execution |
- [08_sequence_cdm_refresh.puml](diagrams/08_sequence_cdm_refresh.puml) | CDM tables refresh DAG (every minute) |
- [09_activity_fraud_evaluation.puml](diagrams/09_activity_fraud_evaluation.puml) | Fraud engine rule evaluation logic |
- [10_deployment_diagram.puml](diagrams/10_deployment_diagram.puml) | Docker Compose deployment architecture |
- [11_state_diagram_transaction.puml](diagrams/11_state_diagram_transaction.puml) | Transaction state machine |
- [12_usecase_diagram.puml](diagrams/12_usecase_diagram.puml) | System use cases by actor |
---

## Тестирование
- Сделаны автотесты. Отчет о покрытии в diagrams/test_coverage
- Сделано ручное тестирование

## Структура проекта
- `services/antifraud-api` — Spring Boot сервис (REST + Kafka consumer + rules + DB)
- `services/tx-generator` — генератор транзакций (Kafka producer)
- `airflow/` — DAG ETL
- `monitoring/` — Prometheus + Grafana provisioning
- `dashboards/` — экспорт/заготовки дашбордов
- `docker-compose.yml` — всё поднимается одной командой

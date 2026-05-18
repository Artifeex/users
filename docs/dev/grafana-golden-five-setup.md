# Grafana Golden Five Setup

## What is provisioned
- Grafana datasource provisioning from `ops/grafana/provisioning/datasources/datasource.yml`.
- Grafana dashboard provisioning from `ops/grafana/provisioning/dashboards/dashboards.yml`.
- Dashboards:
  - `ops/grafana/dashboards/01-system-overview.json`
  - `ops/grafana/dashboards/02-microservice-template.json`
- `ops/grafana/dashboards/03-postgresql-health.json`
- `ops/grafana/dashboards/04-kafka-cluster-streaming.json`
- `ops/grafana/dashboards/05-minio-s3-storage.json`
- `ops/grafana/dashboards/06-infrastructure-os.json`

## Start stack
```bash
docker compose up -d
```

## Validate metrics names in Prometheus
Validation result against `http://localhost:8080/actuator/prometheus`:
- Confirmed in this service: `hikaricp_connections_active`, `hikaricp_connections_pending`, `hikaricp_connections_timeout_total`.
- Not present yet: Kafka client metrics (`kafka_consumer_*`, `kafka_producer_*`) because there is no active producer/consumer traffic in current local run.

Before using the Kafka row in dashboard #2, verify metric names on `http://localhost:9090/graph`:

1. Hikari metrics:
   - `hikaricp_connections_active`
   - `hikaricp_connections_pending`
   - `hikaricp_connections_timeout_total`
2. Kafka client metrics:
   - `kafka_consumer_fetch_manager_records_lag_max`
   - `kafka_producer_record_error_total`

If your metric names differ, edit only panels in `02-microservice-template.json`.

## Drill-down links
- System Overview -> Microservice Template by `var-application`.
- Microservice Template (Hikari panel) -> PostgreSQL dashboard.
- Microservice Template (Kafka panel) -> Kafka dashboard.

## Alerting baseline
Prometheus alert rules are in `ops/prometheus/alerts.yml`:
- service down
- high 5xx ratio
- low disk space
- kafka under-replicated partitions

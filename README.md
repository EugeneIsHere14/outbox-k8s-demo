# Outbox K8s Demo

Training project demonstrating Outbox Pattern with Debezium, Kafka, and Kubernetes.

---

## Requirements

- Java 21
- Maven 3.9+
- Docker
- k3d (or any local Kubernetes cluster)
- kubectl

### Ports used locally

Make sure the following ports are free:

- 8080 — order-service
- 8081 — payment-service
- 9092 — Kafka
- 3306 — MySQL (inside cluster)
- 5432 — PostgreSQL (inside cluster)

### Environment

Tested on:
- Linux (Ubuntu)
- Docker / k3d

---

## Architecture Overview

```
Client -> Order Service -> MySQL (orders + outbox_events)
                               |
                               v
                         Debezium (CDC)
                               |
                               v
                         Kafka (order-events)
                               |
                               v
                        Payment Service
```

---

## What is happening

1. order-service creates an order
2. In the same transaction, it writes an event to outbox_events
3. Debezium reads MySQL binlog (CDC)
4. Event is transformed and sent to Kafka (order-events)
5. payment-service consumes the event

Order service does not communicate with Kafka directly. It only writes to the database.

---

## Local Setup (k3d + Kubernetes Manual Deployment)

### 1. Build services

```bash
mvn clean package -DskipTests
```

### 2. Build Docker images

```bash
docker build -t order-service:1.0.0 .
docker build -t payment-service:1.0.0 .
```

### 3. Import images into k3d

```bash
k3d image import order-service:1.0.0 -c demo-k8s-cluster
k3d image import payment-service:1.0.0 -c demo-k8s-cluster
```

---

## Kubernetes Deployment

### 4. Deploy services in the specified order

```bash
kubectl apply -f orders-db.yaml
kubectl apply -f payments-db.yaml
kubectl apply -f order-service.yaml
kubectl apply -f kafka.yaml
kubectl apply -f payment-service.yaml
```

---

### 5. Deploy infrastructure in the specified order

### 5.1 Delete existing jobs (if any)

```bash
kubectl delete job kafka-topics-init
kubectl delete job register-order-outbox-connector
```

### 5.2 Deploy topics, connectors, and jobs
```bash
kubectl apply -f precreate-topics.yaml
kubectl apply -f debezium-connector-config.yaml
kubectl apply -f kafka-connect.yaml
kubectl apply -f register-connector-job.yaml
```

This step:
- creates Kafka topics (including internal topics)
- starts Kafka Connect
- loads Debezium configuration
- registers the connector

---

### 6. Restart deployments (if needed)

```bash
kubectl rollout restart deployment order-service
kubectl rollout restart deployment payment-service
```

---

### 7. Check status

```bash
kubectl get pods
kubectl get svc
```

---

## Access Services

```bash
kubectl port-forward svc/order-service 8080:8080
kubectl port-forward svc/payment-service 8081:8080
```

---

## Test End-to-End Flow

### 1. (Optional) Open Kafka consumer

```bash
kubectl exec -it deployment/kafka -- bash
```

```bash
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning
```

---

### 2. Create order via API

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Greg Decker",
    "amount": 334.75
  }'
```

---

### 3. What happens next

1. Order is saved in the database
2. Event is written to outbox_events
3. Debezium reads the change from binlog
4. Kafka receives the event
5. payment-service processes the event

---

### 4. Verification

Kafka output:

```
{"eventType":"ORDER_CREATED","orderId":X}
```

Check logs:

```bash
kubectl logs deployment/payment-service -f
```

---

## Local Setup (k3d + Argo CD and Strimzi)

### 1. Install Argo CD and Strimzi Kafka Operator

```bash
kubectl create namespace argocd
kubectl apply -n argocd --server-side --force-conflicts -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

kubectl create namespace kafka
kubectl apply -f https://strimzi.io/install/latest?namespace=kafka -n kafka   
```

---

### 2. Build and push debezium mysql connector image for Kafka Connect to local registry

```bash
docker build -t kafka-connect-debezium:1.0.0 .

k3d image import kafka-connect-debezium:1.0.0 -c demo-k8s-cluster
```

---

### 3. Remove existing application (if any) via Argo CD UI or CLI and port-forward Argo CD server

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

---

### 4. Get Argo CD initial admin password

```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

---

### 5. Run Argo CD application manifest from argocd directory

```bash
kubectl apply -f application.yaml -n argocd
```

---

### 6. Test the flow as described in the section for manual setup.

---

## Database Access (optional)

```bash
kubectl port-forward svc/orders-mysql 3310:3306
```

```bash
mysql -h 127.0.0.1 -P 3310 -u user -p
```

---

## Local Setup (k3d + Argo CD and Strimzi via Helm Charts)

Steps are the same to the previous section, but you will apply application manifest for Argo CD in k8s-v3 folder.

---

## Important Notes

Debezium snapshot mode:

```json
"snapshot.mode": "initial"
```

Reads existing data first, then listens for new changes.

---

## Next Steps

- produce events from order-service (remove manual inserts)
- simplify Kafka message format
- add retry and dead letter queue
- add persistent storage for Kafka

---

## Tech Stack

- Spring Boot
- MySQL
- PostgreSQL
- Kafka
- Kafka Connect
- Debezium
- Kubernetes (k3d)
---

## Debezium Snapshot and Signal Practice

This section describes how to practice Debezium snapshot modes, incremental snapshots, and signals without changing the existing Outbox flow.

The existing Outbox connector should continue to capture only:

```text
orders.outbox_events -> order-events
```

Do not use the Outbox table for snapshot experiments. Snapshotting `outbox_events` can re-publish old business events and cause duplicate processing in `payment-service`.

### Test tables

Add the following tables to the Orders MySQL schema:

```sql
CREATE TABLE IF NOT EXISTS customers_snapshot_test (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS debezium_signals (
    id VARCHAR(64) PRIMARY KEY,
    type VARCHAR(32),
    data VARCHAR(2048)
    );
```

Add initial test data:

```sql
INSERT IGNORE INTO customers_snapshot_test (id, name, email) VALUES
(1, 'John Smith', 'john@test.com'),
(2, 'Anna Black', 'anna@test.com'),
(3, 'Mike Green', 'mike@test.com');
...       
```

The `debezium_signals` table is used only for Debezium operational commands. It is not a business table.

### Test connector

Create a separate KafkaConnector for snapshot experiments.

The connector should capture only:

```text
orders.customers_snapshot_test
orders.debezium_signals
```

Recommended default configuration:

```yaml
snapshot.mode: "no_data"
signal.enabled.channels: "source"
signal.data.collection: "orders.debezium_signals"
incremental.snapshot.chunk.size: "2"
```

Expected Kafka topic for customer changes:

```text
orders-snapshot-cdc.orders.customers_snapshot_test
```

The exact topic name depends on the connector `topic.prefix` value.

The test connector intentionally does not use the Debezium Outbox SMT (`EventRouter`).

The goal is to observe raw Debezium CDC events and snapshot behavior, including:
- snapshot events
- incremental snapshots
- chunking
- Debezium metadata
- operation types (`op=r`, `op=c`, etc.)

### Open Kafka consumer

Run a Kafka console consumer for the test topic:

```bash
kubectl exec -it -n kafka kafka-kafka-0 -- bash
```

```bash
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka-kafka-bootstrap:9092 \
  --topic orders-snapshot-cdc.orders.customers_snapshot_test \
  --from-beginning
```

### Snapshot trigger endpoint

The project exposes an internal REST endpoint for triggering Debezium incremental snapshots.

This endpoint is intended only for local demo and training purposes.

Production systems usually integrate snapshot triggering with:
- internal operations tooling
- GitOps workflows
- Kafka signal channels
- operational backfill jobs

Endpoint:

```http
POST /internal/debezium/snapshots
```

Example request without filters:

```json
{
  "tableName": "customers_snapshot_test"
}
```

Example request with filters:

```json
{
  "tableName": "customers_snapshot_test",
  "fromIdInclusive": 2,
  "createdFrom": "2026-05-01T00:00:00",
  "createdTo": "2026-05-08T23:59:59"
}
```

The endpoint returns:

```http
202 Accepted
```

because Debezium processes snapshots asynchronously after the signal is stored in the database.

### Test case 1: snapshot.mode=initial

Goal: verify that Debezium reads existing rows from the database during the first connector startup.

Set the test connector mode to:

```yaml
snapshot.mode: "initial"
```

Apply the connector and wait until it becomes ready.

Expected result:

```text
Rows with ids 1, 2, and 3 are published to Kafka.
```

This proves that an initial snapshot reads existing table data before streaming new binlog changes.

Important behavior:

```text
If the connector is restarted and Kafka Connect offsets are still available,
Debezium does not repeat the initial snapshot.
It resumes streaming from the last stored offset.
```

### Test case 2: snapshot.mode=no_data

Goal: verify that Debezium skips existing rows and streams only new changes.

Set the test connector mode to:

```yaml
snapshot.mode: "no_data"
```

Recreate the test connector if needed, then insert a new row:

```sql
INSERT INTO customers_snapshot_test (id, name, email)
VALUES (4, 'New User', 'new@test.com');
```

Expected result:

```text
Existing rows are not published during startup.
Only the new row with id 4 is published after INSERT.
```

This mode is usually safer for Outbox-style flows because old events are not re-published during startup snapshots.

### Test case 3: incremental snapshot without filters

Goal: trigger a runtime incremental snapshot while the connector is already streaming CDC events.

Keep the connector in streaming mode:

```yaml
snapshot.mode: "no_data"
signal.enabled.channels: "source"
signal.data.collection: "orders.debezium_signals"
incremental.snapshot.chunk.size: "2"
```

Trigger snapshot:

```bash
curl -X POST http://localhost:8080/internal/debezium/snapshots \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "customers_snapshot_test"
  }'
```

Expected result:

```text
Debezium starts an incremental snapshot without restarting the connector.
Existing rows from customers_snapshot_test are published to Kafka in chunks.
Streaming remains active during the snapshot.
```

Observe snapshot records in Kafka:

```json
{
  "op": "r"
}
```

where:
- `r` = snapshot read
- `c` = create
- `u` = update
- `d` = delete

### Test case 4: incremental snapshot with filters

Goal: trigger a partial backfill for selected rows only.

Trigger snapshot with an id filter:

```bash
curl -X POST http://localhost:8080/internal/debezium/snapshots \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "customers_snapshot_test",
    "fromIdInclusive": 2
  }'
```

Expected result:

```text
Only rows matching id > 2 are snapshotted.
```

You can also test date filtering:

```bash
curl -X POST http://localhost:8080/internal/debezium/snapshots \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "customers_snapshot_test",
    "createdFrom": "2026-05-01T00:00:00"
  }'
```

Or combine filters:

```bash
curl -X POST http://localhost:8080/internal/debezium/snapshots \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "customers_snapshot_test",
    "fromIdInclusive": 2,
    "createdFrom": "2026-05-01T00:00:00",
    "createdTo": "2026-05-08T23:59:59"
  }'
```

Default values for signalType and snapshotType fields are set by default to 'execute-snapshot' and 'incremental' respectively, but can be overridden:

```bash
curl -X POST http://localhost:8080/internal/debezium/snapshots \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "customers_snapshot_test",
    "signalType": "execute-snapshot",
    "snapshotType": "blocking",
    "fromIdInclusive": 2,
    "createdFrom": "2026-05-01T00:00:00",
    "createdTo": "2026-05-08T23:59:59"
  }'
```

This demonstrates that Debezium snapshots are table-based and can be filtered with SQL conditions.

### Test case 5: snapshot.mode=when_needed

Goal: understand the difference between `initial` and `when_needed`.

Use this mode only after the basic tests are clear:

```yaml
snapshot.mode: "when_needed"
```

Expected behavior:

```text
If valid offsets exist, Debezium resumes streaming and does not snapshot again.
If offsets or required binlog position are missing, Debezium automatically performs a new snapshot.
```

Difference from `initial`:

```text
initial      -> snapshot on the first connector start, then resume from offsets on normal restarts
when_needed  -> resume from offsets when possible, but automatically snapshot again if recovery requires it
```

For Outbox tables, `when_needed` can be risky because a recovery snapshot can re-publish old business events. Consumers must be idempotent if this mode is used with Outbox data.

### How to switch snapshot modes during tests

To test `initial`, `no_data`, or `when_needed`, update the test KafkaConnector configuration:

```yaml
snapshot.mode: "initial"
```

or:

```yaml
snapshot.mode: "no_data"
```

or:

```yaml
snapshot.mode: "when_needed"
```

Then apply the changes through Argo CD sync or directly with kubectl, depending on how the environment is currently deployed.

If the connector already has stored offsets, changing `snapshot.mode` alone may not be enough to force a new startup snapshot.

For a clean snapshot mode test, remove and recreate the test connector:

```bash
kubectl delete kafkaconnector customers-snapshot-connector -n kafka
```

Then sync/apply it again.

If the connector still resumes from previous offsets, Kafka Connect internal offsets are still present. In that case, use a fresh connector name or clean the local environment/PVCs/internal topics as appropriate for the demo.

### Cleanup

Remove the test connector:

```bash
kubectl delete kafkaconnector customers-snapshot-connector -n kafka
```

Remove the test topic if needed:

```bash
/opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server kafka-kafka-bootstrap:9092 \
  --delete \
  --topic orders-snapshot-cdc.orders.customers_snapshot_test
```

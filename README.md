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

## Local Setup (k3d + Kubernetes Manual Deployment)

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

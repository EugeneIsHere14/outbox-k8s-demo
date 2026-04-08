# outbox-k8s-demo
Training project for demonstration Kafka+Debezium+K8s

## Local Setup (k3d + Kubernetes)

### 1. Build services
mvn clean package -DskipTests

### 2. Build Docker images
docker build -t order-service:1.0.0 .
docker build -t payment-service:1.0.0 .

### 3. Import images into k3d
k3d image import order-service:1.0.0 -c demo-k8s-cluster
k3d image import payment-service:1.0.0 -c demo-k8s-cluster

### 4. Deploy to Kubernetes (apply all yaml files)
kubectl apply -f .

### 5. Restart deployments (after rebuild)
kubectl rollout restart deployment order-service
kubectl rollout restart deployment payment-service

### 6. Check status
kubectl get pods
kubectl get svc

### 7. Access services (port-forward)
kubectl port-forward svc/order-service 8080:8080
kubectl port-forward svc/payment-service 8081:8080

Open in browser:
http://localhost:8080   (order)
http://localhost:8081   (payment)

### 8. Access MySQL (not necessary)
kubectl port-forward svc/orders-mysql 3310:3306
mysql -h 127.0.0.1 -P 3310 -u user -p

### Notes
- Use imagePullPolicy: IfNotPresent for local images
- Services inside cluster: http://<service-name>:<port>
- Example DB URL: jdbc:mysql://orders-mysql:3306/orders

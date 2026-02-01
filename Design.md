# System Design Document: Order Ingestion Platform

## 1. System Overview
The Order Ingestion Platform is a high-throughput microservice designed to ingest, validate, and process real-time order events from multiple external partners (currently A and B). It standardizes diverse external formats into a single internal schema, prevents duplicate orders (idempotency), and provides analytical summaries via a REST API and Dashboard.

## 2. Architecture: Local vs. Cloud
The current implementation focuses on logical correctness using local abstractions. The target architecture is designed for AWS.

| Component | Local Implementation (Current) | Production Cloud Design (AWS) |
| :--- | :--- | :--- |
| **Ingestion API** | Spring Boot (Tomcat) | AWS API Gateway + Application Load Balancer (ALB) |
| **Compute** | Local JVM | AWS Fargate (ECS) |
| **Message Queue** | `LinkedBlockingQueue` (In-Memory) | **Amazon SQS** (Simple Queue Service) |
| **Database** | H2 (In-Memory) | **Amazon RDS for PostgreSQL** |
| **Secrets** | Hardcoded Map | AWS Secrets Manager |
| **Frontend** | React (Localhost) | AWS S3 (Static Website) + CloudFront (CDN) |

---

## 3. AWS Cloud Deployment Strategy
This section details how the application translates to a production AWS environment.

### 3.1 Compute Strategy: Containers (AWS Fargate)
**Decision:** Use **AWS Fargate** (Serverless Containers) instead of AWS Lambda.
* **Reasoning:** The application runs continuous background threads (`OrderProcessorService`) to consume messages from queues. Long-running polling consumers are cost-prohibitive and architecturally complex in Lambda (which has timeout limits). Fargate provides a robust environment for Spring Boot's "always-on" nature.

### 3.2 Streaming & Queues: Amazon SQS
**Decision:** Replace the internal `BlockingQueue` with **Amazon SQS**.
* **Workflow:**
    1.  **Ingestion:** Controller validates request -> Sends message to `valid_orders_queue` (SQS).
    2.  **Processing:** Service listens to SQS -> Persists to RDS.
* **Why SQS?**
    * **Durability:** Messages are stored across multiple availability zones. If the app crashes, messages are not lost (unlike the current in-memory queue).
    * **Dead Letter Queues (DLQ):** Automatically capture "poison pill" messages that fail processing 3+ times, allowing for manual investigation.
    * **Scaling:** We can trigger auto-scaling of Fargate tasks based on the `ApproximateNumberOfMessagesVisible` metric in SQS.

### 3.3 Storage: Amazon RDS (PostgreSQL)
**Decision:** Use **Amazon RDS (PostgreSQL)**.
* **Why Relational?** The requirements involve complex aggregations (e.g., *Monthly Sales Summary*, *Date Range Queries*). SQL is highly efficient for `SUM()`, `COUNT()`, and `GROUP BY` operations compared to NoSQL (DynamoDB), which would require complex indexing or Analytics/Stream processing for simple summaries.
* **Data Integrity:** ACID transactions are critical for financial order data.

### 3.4 Frontend Hosting
**Decision:** **S3 + CloudFront**.
* The React frontend is a Single Page Application (SPA).
* **S3:** Stores the built static files (HTML, CSS, JS).
* **CloudFront:** Caches content globally (CDN) for low latency and handles HTTPS termination.

### 3.5 Security
* **Network:** The RDS instance runs in a private subnet, accessible only by the Fargate tasks.
* **Secrets:** API Keys and Database Credentials are stored in **AWS Secrets Manager**, injected into the container as environment variables at runtime.
* **WAF:** AWS WAF placed in front of the ALB to rate-limit requests and block malicious IP addresses.

---



---

## 5. Observability Plan
To ensure operational health in AWS:

1.  **Logs:** Use **Amazon CloudWatch Logs**.
    * Structured JSON logging (via Logback) to allow querying error rates by `partner_id`.
2.  **Metrics:** Use **Spring Boot Actuator + Micrometer** to push metrics to CloudWatch.
    * Key Metric: `orders.ingested.count` (Counter)
    * Key Metric: `queue.depth` (Gauge)
    * Key Metric: `processing.latency` (Timer)
3.  **Alarms:**
    * Trigger PagerDuty if `error_orders_queue` > 10 messages (indicates a bad deployment or partner API change).
    * Trigger Scaling if `valid_orders_queue` > 1000 messages.

---

## 6. CI/CD Pipeline
We assume a GitHub Actions workflow:
1.  **Commit:** Developer pushes code.
2.  **Test:** Run Unit & Integration Tests (Maven).
3.  **Build:** Build Docker Image.
4.  **Scan:** ECR Image Scan for vulnerabilities.
5.  **Deploy:** Update AWS ECS Service (Rolling update).

# Order Ingestion Platform – System Design
Each partner sends orders in **different formats** and may resend the same order multiple times. The system must therefore:

- Validate incoming requests
- Normalize partner-specific payloads into a **single internal schema**
- Prevent **duplicate order processing** (idempotency)
- Process orders reliably at high throughput
- Store data safely for analytics and reporting
- Expose summaries via REST APIs and a dashboard

---

## 1. Local vs Production Architecture

During development, the system runs locally using lightweight components to focus on correctness and logic.  
In production, the same architecture is mapped to **managed AWS services** for scalability, durability, and security.

| Layer | Local (Development) | AWS (Production) |
|-----|--------------------|------------------|
| API | Spring Boot (Tomcat) | API Gateway + ALB |
| Compute | Local JVM | ECS (AWS Fargate) |
| Queue | In-memory BlockingQueue | Amazon SQS |
| Database | H2 (In-memory) | Amazon RDS (PostgreSQL) |
| Secrets | Hardcoded config | AWS Secrets Manager |
| Frontend | React (localhost) | S3 + CloudFront |

---

## 2. AWS Deployment Design

### 2.1 Compute Layer – ECS with Fargate

**Decision:** Use **AWS Fargate** instead of AWS Lambda.

**Reasoning:**
- The system contains a **long-running background consumer** (`OrderProcessorService`) that continuously polls a queue.
- Lambda is not ideal for always-on workloads due to execution time limits and cost inefficiencies.
- Fargate allows Spring Boot services to run continuously without managing EC2 instances.

**Further pros:**
- Serverless container execution
- Easy horizontal scaling
- Clean fit for microservice workloads

---

### 2.2 Messaging Layer – Amazon SQS

**Decision:** Replace the local `BlockingQueue` with **Amazon SQS**.

**Order Flow:**
1. Partner sends an order to the ingestion API
2. API validates and normalizes the payload
3. Order is pushed to `valid_orders_queue`
4. ECS consumers read messages and persist data to RDS

**Why SQS:**
- Messages are **durable and highly available**
- API and processing layers are **loosely coupled**
- **Dead Letter Queue (DLQ)** captures repeatedly failing messages
- ECS services can auto-scale based on queue depth

This eliminates data loss risks present in in-memory queues.

---

### 2.3 Database Layer – Amazon RDS (PostgreSQL)

**Decision:** Use **PostgreSQL on Amazon RDS**.

**Why a relational database:**
- The system requires:
  - Monthly and daily sales summaries
  - Partner-based aggregations
  - Date-range queries
- SQL handles these efficiently using `GROUP BY`, `SUM`, and indexes

**Additional benefits:**
- ACID transactions (critical for order data)
- Automated backups and failover
- Strong integration with Spring Data JPA

---

### 3.4 Frontend Hosting

The frontend is a **React Single Page Application (SPA)**.

**Deployment approach:**
- Build React app and upload static assets to **Amazon S3**
- Serve content globally using **CloudFront**

**Benefits:**
- Low latency via CDN caching
- Automatic HTTPS
- Very low operational cost

---

## 4. Security Design

Security is integrated into the architecture:

- **Private Subnets:** RDS runs in private subnets, not publicly accessible
- **IAM Roles:** ECS tasks access AWS services via IAM (no credentials in code)
- **Secrets Manager:** Database credentials and API keys are injected securely at runtime
- **AWS WAF:** Protects the ALB from:
  - Excessive request rates
  - Malicious IPs
  - Common web exploits

---

## 5. CI/CD Pipeline

The deployment pipeline uses **GitHub Actions**:

1. Code pushed to repository
2. Unit and integration tests executed
3. Docker image built
4. Image scanned in Amazon ECR
5. ECS service updated using rolling deployment

This ensures **zero downtime deployments** and quick rollback if needed.

---


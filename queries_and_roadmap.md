# Food Redistribution — Queries & Scaling Roadmap

---

## Q1: What Happens to Disputed Food Items?

When a claim is raised to `DISPUTED`, the food quantity is **NOT restored**. Here is why, and what actually happens:

### The Chain of Events

```
Claim is ACTIVE
  → Receiver claims 5 units
  → FoodPost.quantity reduced by 5 (already done at claim time)
  → Status moves to DISPUTED (via raiseDispute)
  → Food quantity stays reduced
```

**Why not restore on dispute?** The food is physically still somewhere — either the donor has it or the receiver has it. We don't know who is telling the truth yet. Restoring it and re-listing it as AVAILABLE would be wrong — a third person might claim food that's actually in someone's hands.

### What Admin Decides (resolveDispute)

| Admin Decision | `resolution = "COMPLETED"` | `resolution = "CANCELLED"` |
|---|---|---|
| Claim status becomes | `COMPLETED` | `CANCELLED` |
| Food quantity restored? | ❌ No (food was received) | ✅ **Should be yes** |
| Trust metrics updated? | Currently no (needs fix) | Currently no |

> ⚠️ **Bug found:** When admin resolves CANCELLED, the food quantity is NOT restored yet. This is because `resolveDispute()` doesn't call `restoreFoodQuantity()`. This should be fixed.

### Current Behavior in Code
```java
// resolveDispute() in ClaimLifecycleService
if ("COMPLETED".equalsIgnoreCase(resolution)) {
    claim.setStatus(ClaimStatus.COMPLETED);
    // food stays reduced — correct, transaction happened
} else {
    claim.setStatus(ClaimStatus.CANCELLED);
    // ❌ quantity NOT restored here — this is a bug
}
```

### Summary
| Status | Food Quantity |
|---|---|
| ACTIVE → CANCELLED (by receiver/donor) | ✅ Restored |
| ACTIVE → NO_SHOW → CANCELLED | ✅ Restored |
| DISPUTED → COMPLETED (admin) | ❌ Not restored (correct) |
| DISPUTED → CANCELLED (admin) | ❌ Not restored (BUG — should restore) |

---

## Q2: Data Structure Improvements for Time Complexity

### Current Problem — Nearby Receivers at Food Post Creation

```java
// Current O(n) loop over ALL receivers in DB
List<User> nearbyReceivers = userRepository.findByRoleAndEmailVerified(RECEIVER, true);
for (User receiver : nearbyReceivers) {
    double distance = GeoUtils.calculateDistanceKm(...);  // Haversine calc for every user
    if (distance <= 50) { sendEmail(...); }
}
```

**This is O(n) in users** — if you have 10,000 receivers, you load and loop all 10,000 of them.

### Better Data Structures & Approaches

#### 1. Spatial Index (Best Long-Term — MySQL or Postgres)
MySQL supports `POINT` column type with `ST_Distance_Sphere()` and spatial indexes.
- Query complexity: **O(log n)** using R-Tree spatial index
- Replace the loop with one SQL query:
```sql
SELECT * FROM users
WHERE role = 'RECEIVER'
  AND ST_Distance_Sphere(location_point, ST_GeomFromText('POINT(lng lat)')) <= 50000
```
- Zero Java loop needed.

#### 2. QuadTree (In-Memory, Not Recommended for Prod)
- Good for competitive programming problems, not Spring Boot
- Would require custom implementation + cache invalidation on user move

#### 3. Redis GeoSet (Best for Real-Time)
- Redis has native `GEOADD` / `GEORADIUS` / `GEOSEARCH` commands
- O(N+log(M)) where N = matches (very fast)
- Store: `GEOADD receivers lng lat userId` when user registers/updates location
- Query: `GEORADIUS receivers lng lat 50 km` — returns user IDs in range
- Fetch those user records from DB for emails

#### 4. Elasticsearch Geo Queries
- Discussed in Q4 below.

### Other Places with Complexity Issues

| Area | Current | Better |
|---|---|---|
| `getMyClaims()` | Two separate list queries + Java stream concat | One JPQL `WHERE donor = ? OR receiver = ?` |
| Trust score | Recalculated on every call from raw counters | Cache in Redis with 1-min TTL |
| `findByReviewed(false)` | Full table scan | Add `@Index` on `reviewed` column |
| Password reset token lookup | `findByPasswordResetToken` — string scan | Add `@Index(unique=true)` on that column |

---

## Q3: The Dispute System — Detailed Explanation

### What is a Dispute?

A dispute is a **formal disagreement** between a donor and a receiver about a transaction. It freezes the claim and hands control to an admin.

### Who Can Raise It and When?

| Party | Allowed Status | Reason they'd dispute |
|---|---|---|
| **Receiver** | `ACTIVE` | Donor isn't responding, food looks different, safety concern |
| **Receiver** | `DONOR_CONFIRMED` | Donor says they gave food but receiver denies receiving it |
| **Donor** | `ACTIVE` | Receiver is being abusive or threatening |
| **Donor** | `DONOR_CONFIRMED` | Donor gave food, receiver won't confirm (non-response) |

**Not allowed:** COMPLETED, CANCELLED, or already DISPUTED.

### State Machine Flow

```
       ┌─────────────────────────────────────────────┐
       │                                             │
       ▼          raiseDispute()                     │
   [ACTIVE] ─────────────────────────► [DISPUTED]   │
       │                                    │        │
       │ donorConfirm()                     │        │
       ▼                          admin resolves     │
[DONOR_CONFIRMED] ──raiseDispute()──► [DISPUTED]     │
       │                                    │        │
       │ receiverConfirm()          COMPLETED│CANCEL  │
       ▼                                    │        │
  [COMPLETED]                              / \       │
                                     [COMP] [CANC]───┘
```

### What Happens Step by Step

```
Step 1: Party calls PUT /api/v1/claims/{id}/dispute
        Body: { "reason": "FOOD_NOT_AS_DESCRIBED" }

Step 2: ClaimLifecycleService.raiseDispute()
        - Verifies caller is donor or receiver of this claim
        - Checks status is ACTIVE or DONOR_CONFIRMED
        - Sets claim.status = DISPUTED
        - Sets claim.disputeReason = the provided reason
        - Sets claim.disputedAt = now()
        - Increments disputeCount on BOTH donor and receiver
          (both lose 1 dispute point — trust score hurts both)

Step 3: Party calls POST /api/v1/reports/claim/{id}
        Body: { "reason": "FAKE_FOOD_POST", "description": "..." }
        - Only allowed now that claim is DISPUTED
        - Increments reportCount on the OTHER party

Step 4: Admin calls GET /api/v1/reports/admin/unreviewed
        - Sees all unreviewed reports
        - Reads descriptions and makes a decision

Step 5: Admin calls PUT /api/v1/reports/admin/{reportId}/review
        Body: { "adminNote": "Investigated. Donor at fault." }
        - Marks report as reviewed, adds admin note

Step 6: Admin calls PUT /api/v1/reports/admin/claim/{id}/resolve
        Body: { "resolution": "CANCELLED" }  or  "COMPLETED"
        - COMPLETED: marks as done, food stays reduced (receiver got it)
        - CANCELLED: marks as cancelled (food should be restored — pending bug fix)

Step 7: Admin calls PUT /api/v1/reports/admin/user/{id}/ban  (optional)
        - If one party was clearly at fault and repeated offender
```

### Trust Score Impact

```
raiseDispute() → both parties get +1 disputeCount
                → trust score = f(successfulDonations, disputeCount, noShowCount, reportCount, rating)
                → dispute hurts trust even before guilt is determined
```

This is intentional — raising disputes should be a last resort, not used casually.

### DisputeReason vs ReportReason — Summary

| | `DisputeReason` | `ReportReason` |
|---|---|---|
| Stored on | `FoodClaim` entity | `Report` entity |
| Set by | Either party via dispute endpoint | Reporter via report endpoint |
| Purpose | Categorizes WHY the transaction failed | Categorizes the MISCONDUCT of a user |
| Visibility | Both parties can see it | Admin only |
| Examples | `FOOD_NOT_AS_DESCRIBED`, `RECEIVER_NO_RESPONSE` | `HARASSMENT`, `FAKE_FOOD_POST`, `FRAUD` |

---

## Q4: How Elasticsearch Geo-Search Would Help

### Current Approach (Java Loop)

```
Database → ALL receivers (could be thousands) → Java → loop each → calculate distance → filter
```

**Problem:** You pull ALL records from DB into Java memory, then throw most away. Terrible at scale.

### Current MySQL Geo Approach (Better)

```
MySQL spatial index → returns only nearby records
```

Already much better but MySQL spatial queries can't do advanced scoring like "sort by nearest + sort by trust score + full-text search on food name" in one query.

### Elasticsearch Approach (Best at Scale)

Elasticsearch stores documents (food posts or users) with a `geo_point` field and builds a **geospatial index** internally.

```
Your Query:
"Find all AVAILABLE food posts within 10km of user's location,
 sorted by distance, where foodName contains 'rice'"

Elasticsearch does this in ONE query:
{
  "query": {
    "bool": {
      "filter": [
        { "term": { "status": "AVAILABLE" } },
        { "geo_distance": { "distance": "10km", "location": { "lat": 12.9, "lon": 80.2 } } }
      ],
      "must": { "match": { "foodName": "rice" } }
    }
  },
  "sort": [
    { "_geo_distance": { "location": { "lat": 12.9, "lon": 80.2 }, "order": "asc" } }
  ]
}
```

### Side-by-Side Comparison

| Feature | MySQL Loop | MySQL Spatial | Elasticsearch |
|---|---|---|---|
| Query nearby posts | O(n) all rows | O(log n) index | O(log n) + scoring |
| Full-text food search | Basic LIKE | Basic LIKE | Relevance scoring |
| Sort by distance | Java sort | SQL ORDER BY | Built-in geo sort |
| Filter + geo + text | 3 separate queries | 1 spatial query | 1 compound query |
| Scale (1M posts) | ❌ Terrible | ✅ OK | ✅ Excellent |
| Real-time updates | Immediate | Immediate | Near real-time (sync needed) |
| Setup complexity | None | Medium | High |

### What You'd Sync

You'd keep MySQL as your primary database (source of truth), and sync `FoodPost` records to Elasticsearch whenever they are created/updated. This is done with:
- **Spring Data Elasticsearch** (official Spring library)
- Or an outbox pattern with Kafka

### Verdict for Your App Now
For your current scale, **MySQL with a spatial index column** is the sweet spot. Elasticsearch makes sense when you have:
- 100,000+ food posts
- Need for relevance-based search (not just geo)
- Complex multi-filter queries

---

---

# Scaling Roadmap — Student Edition (Free Tier + AWS)

You have:
- GitHub Student Pack ✅
- AWS interest ✅
- Free tier requirement ✅
- New to this ✅

Below is a **phased roadmap** — learn one layer at a time, each building on the previous.

---

## Phase 0: Before You Start — Prerequisites (2 weeks)

Learn these first or you'll be lost.

| Topic | Resource | Time |
|---|---|---|
| Basic Linux CLI | `man` pages, `tldr` tool | 3 days |
| Docker basics | Official Docker tutorial | 3 days |
| What is HTTP/TCP | "HTTP: The Definitive Guide" (free online) | 2 days |
| Git branching | Learn Git Branching (interactive) | 1 day |
| Basic networking (IP, DNS, ports) | YouTube: "Networking Fundamentals" by Professor Messer | 3 days |

---

## Phase 1: Containerization with Docker (1–2 weeks)

**Goal:** Package your Spring Boot app so it runs identically anywhere.

### What to Use
- **Docker** — containerize your app + MySQL
- **Docker Compose** — run multi-container apps locally (app + db + redis)

### Free Resources
- Docker Desktop (free for students)
- Play with Docker (free online sandbox): https://labs.play-with-docker.com

### What to Do
1. Write a `Dockerfile` for your Spring Boot app
2. Write a `docker-compose.yml` with: `app`, `mysql`, `redis`
3. Run everything with `docker compose up`
4. Your entire system boots with one command

### Why First
- Everything else (AWS, Kubernetes) depends on containers
- You can test scaling locally before going to cloud

```dockerfile
# Example Dockerfile for Spring Boot
FROM eclipse-temurin:21-jre-alpine
COPY target/foodredistribution.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## Phase 2: Caching with Redis (1 week)

**Goal:** Don't hit the database for the same data repeatedly.

### What to Use
- **Redis** — the industry standard in-memory cache
- **Spring Data Redis** — Spring's integration library
- AWS **ElastiCache** (Redis) — managed Redis on AWS (has free tier)

### What to Cache in Your App

| Data | Cache Key | TTL | Why |
|---|---|---|---|
| `GET /food/available` (paginated) | `food:available:page:0:size:10` | 30s | Most visited, changes rarely |
| User trust score | `trust:userId:42` | 60s | Expensive to calculate |
| User profile | `user:email:john@x.com` | 5 min | DB hit on every API call |
| Nearby receivers list | `receivers:near:lat:lng` | 2 min | Expensive loop |

### Free Tier
- **Redis Cloud** (via GitHub Student Pack): Free 30MB Redis instance
- **AWS ElastiCache**: Free 750 hours/month on t2.micro (Free Tier)

### Learn Order
1. Run Redis locally via Docker
2. Add `spring-boot-starter-data-redis` to your pom.xml
3. Annotate methods with `@Cacheable`, `@CacheEvict`
4. Move to ElastiCache when deploying to AWS

---

## Phase 3: AWS Core Services (3–4 weeks)

**Goal:** Deploy your app to the cloud, understand AWS fundamentals.

### Your Free Tier Budget
AWS Free Tier gives you (per month, first 12 months):
- **EC2**: 750 hours of t2.micro (1 vCPU, 1GB RAM)
- **RDS MySQL**: 750 hours of db.t2.micro + 20GB storage
- **S3**: 5GB storage (for food post images — you need this!)
- **ElastiCache**: 750 hours t2.micro
- **CloudWatch**: Basic monitoring free
- **ECR**: 500MB container storage

### Services to Learn (in Order)

#### Step 1: S3 — File Storage
**Replace your local `/uploads` folder**
- Your app currently stores food images on disk — this breaks if you have 2 servers
- S3 = unlimited, durable, cheap file storage
- Tool: AWS SDK for Java (already in Spring ecosystem)
- Cost: Free Tier 5GB

#### Step 2: RDS — Managed MySQL
**Replace your local MySQL**
- AWS RDS handles backups, patching, failover automatically
- You just connect your Spring Boot app with a JDBC URL
- Free Tier: db.t2.micro, MySQL 8.0

#### Step 3: EC2 — Virtual Server
**Run your app on a real server**
- Launch a t2.micro EC2 instance
- Install Docker, pull your image, run it
- Assign an Elastic IP so the address doesn't change
- Cost: Free Tier t2.micro

#### Step 4: ECR — Container Registry
**Store your Docker images on AWS**
- Like Docker Hub but private and in AWS
- Free Tier: 500MB
- `docker push` your image, EC2 pulls it

### Learn Path for AWS

```
IAM (users, roles, policies) → 3 days, CRITICAL first
    ↓
S3 (upload/download images) → 2 days
    ↓
RDS (managed MySQL) → 2 days
    ↓
EC2 (run your Docker app) → 3 days
    ↓
ECR (push/pull images) → 1 day
    ↓
CloudWatch (logs + alerts) → 2 days
```

### Recommended Learning Resources
- **AWS Skill Builder** (free): https://skillbuilder.aws
- **FreeCodeCamp AWS course** (YouTube, free): search "AWS Full Course freeCodeCamp"
- **AWS Documentation** (best reference)
- **GitHub Student Pack**: Get AWS Educate credits (additional free credits beyond free tier)

---

## Phase 4: Load Balancing (1–2 weeks)

**Goal:** Handle more users by running multiple app instances.

### What to Use
- **AWS ALB** (Application Load Balancer) — distributes requests across your app instances
- Free Tier: 750 hours/month of load balancer hours

### How It Works

```
User Request
    ↓
AWS ALB (load balancer)
    ↓          ↓          ↓
EC2 App #1  EC2 App #2  EC2 App #3
    ↓          ↓          ↓
      AWS RDS (shared database)
          ↓
      AWS ElastiCache (shared Redis)
```

### What You Must Fix Before Adding Load Balancing
Your app currently has two stateful things that break with multiple instances:

1. **Local file storage** (`/uploads` folder) — fix by moving to S3 (Phase 3)
2. **JWT is stateless** ✅ already fine — no session state

### AWS Auto Scaling
- Define min/max number of instances
- AWS adds/removes EC2s automatically based on CPU usage
- Free to configure, you only pay for the EC2 instances

---

## Phase 5: Rate Limiting (1 week)

**Goal:** Prevent abuse — one user can't spam 1000 requests/second.

### What to Use
- **Bucket4j** — Java library, integrates with Spring Boot directly
- Backed by **Redis** (which you already set up in Phase 2)
- No extra cost

### How It Works

```java
// Rate limit: 20 requests per minute per IP
@RateLimiter(name = "default")
public ClaimDetailResponseDTO donorConfirm(...) { ... }
```

### Where to Apply Rate Limits

| Endpoint | Limit | Reason |
|---|---|---|
| `POST /auth/login` | 5/min per IP | Brute force protection |
| `POST /auth/register` | 3/min per IP | Spam accounts |
| `POST /food/{id}/claim` | 10/min per user | Claim spam |
| `POST /reports/claim/{id}` | 5/hour per user | Report abuse |
| `GET /food/available` | 100/min per IP | DDoS protection |

### AWS API Gateway (Alternative)
- AWS API Gateway has built-in rate limiting
- More powerful but adds complexity and cost
- Learn Bucket4j first, API Gateway later

---

## Phase 6: Fault Tolerance (2 weeks)

**Goal:** App keeps working even when parts fail.

### What to Use
- **Resilience4j** — the standard Spring Boot fault tolerance library
- Patterns: Circuit Breaker, Retry, Timeout, Fallback

### Patterns to Implement

#### Circuit Breaker
If the email service is down, stop trying to call it and return a fallback.
```
Email service fails 5 times in 10 seconds
→ Circuit OPENS (stops calling email service)
→ After 30 seconds, tries one test call
→ If success, CLOSES again (normal operation)
```

#### Retry
```
JWT token refresh fails due to network blip
→ Retry automatically 3 times with exponential backoff
→ 1st retry: 100ms, 2nd: 200ms, 3rd: 400ms
```

#### Timeout
```
Downstream API takes too long
→ After 3 seconds, give up and return an error
→ Don't let one slow service hang all threads
```

### AWS Multi-AZ (Availability Zones)
- Deploy your EC2 instances in 2 different Availability Zones
- If one data center goes down, the other handles traffic
- ALB automatically routes to healthy instances
- RDS Multi-AZ: automatic failover if primary DB fails (not free tier)

---

## Phase 7: Monitoring (1 week)

**Goal:** Know what's happening in production.

### Free Tools

| Tool | Purpose | Cost |
|---|---|---|
| **AWS CloudWatch** | Logs, metrics, alarms | Free basic tier |
| **Spring Boot Actuator** | Health check endpoint `/actuator/health` | Free, built-in |
| **Grafana + Prometheus** | Beautiful dashboards | Free, open source |

### What to Monitor
- API response time (p95, p99)
- Error rate (5xx responses)
- JVM heap usage
- DB connection pool usage
- Cache hit rate (Redis)
- Request count per endpoint

---

## Full Roadmap Timeline

```
Month 1: Foundation
├── Week 1: Docker (containerize your app)
├── Week 2: Docker Compose (app + mysql + redis locally)
├── Week 3: Redis caching (Spring Data Redis)
└── Week 4: AWS IAM + S3 (move image uploads)

Month 2: Cloud Deployment
├── Week 1: AWS RDS (managed MySQL)
├── Week 2: AWS EC2 (deploy your Docker image)
├── Week 3: AWS ECR + ALB (load balancer)
└── Week 4: AWS CloudWatch (logs + alerts)

Month 3: Resilience & Performance
├── Week 1: Rate limiting (Bucket4j + Redis)
├── Week 2: Resilience4j (circuit breaker, retry)
├── Week 3: MySQL spatial index (geo queries)
└── Week 4: Auto Scaling + Multi-AZ
```

---

## Software Stack Summary

| Layer | Tool | Free? | Why This One |
|---|---|---|---|
| Container | Docker | ✅ Free | Industry standard |
| Container Orchestration (later) | AWS ECS or EKS | ✅ Free Tier | Managed by AWS |
| Cache | Redis (ElastiCache) | ✅ Free Tier | Fastest, best Spring support |
| Object Storage | AWS S3 | ✅ 5GB Free | Replace /uploads folder |
| Database | AWS RDS MySQL | ✅ Free Tier | Managed, backups, failover |
| Load Balancer | AWS ALB | ✅ Free Tier | Integrates with Auto Scaling |
| Rate Limiting | Bucket4j | ✅ Open Source | Pure Java, Redis-backed |
| Fault Tolerance | Resilience4j | ✅ Open Source | Native Spring Boot support |
| Monitoring | CloudWatch + Actuator | ✅ Free Tier | No extra setup |
| Image Registry | AWS ECR | ✅ 500MB Free | Tight AWS integration |
| Geo Search (later) | MySQL Spatial → Elasticsearch | ✅ Open Source | Progressive upgrade |

---

## Where GitHub Student Pack Helps

| Perk | What You Get | How to Use |
|---|---|---|
| **AWS Educate** | Extra AWS credits | More EC2/RDS time beyond free tier |
| **Redis Cloud** | Free 30MB Redis instance | Use for development/testing |
| **MongoDB Atlas** | $200 credit | Not needed (you use MySQL) |
| **DigitalOcean** | $200 credit | Alternative to AWS, simpler UI |
| **Datadog** | Free Pro account | Professional monitoring (advanced) |

> **Tip:** Start with AWS Free Tier — don't burn GitHub Student Pack credits yet. Save them for after your free tier expires or when you need more resources.

---

## Start Here — Action Items for This Week

1. `docker build -t foodredist .` — containerize your app
2. Write a `docker-compose.yml` with app + mysql + redis
3. Create an AWS account: https://aws.amazon.com
4. Complete "AWS Cloud Practitioner Essentials" on AWS Skill Builder (free, 6 hours)
5. Create an IAM user (never use root account) — first lesson in security

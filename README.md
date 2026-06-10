# 🍱 Food Redistribution Platform

A scalable backend platform designed to reduce food waste by connecting food donors with nearby receivers in need.

The system enables donors to post surplus food, automatically notifies nearby receivers, and manages the complete food claiming lifecycle through secure, role-based APIs.

---

# 🚀 Features

## 🔐 Authentication & Authorization

- JWT-based authentication
- Spring Security integration
- Role-based access control
- User registration and login
- Email verification system
- Protected API endpoints

### Supported Roles

- DONOR
- RECEIVER

---

## 👥 User Management

- User registration
- Secure login
- Email verification
- Location tracking
- Profile management
- Role assignment

---

## 🍲 Food Posting System

Donors can:

- Create food posts
- Specify available quantity
- Set expiry time
- Add pickup location
- Track claims
- Manage food availability

Food posts contain:

- Food details
- Quantity available
- Pickup address
- Geographic coordinates
- Expiration information
- Current status

---

## 📍 Location-Based Distribution

The platform uses geographical coordinates to improve food distribution efficiency.

### Features

- Latitude & Longitude support
- Haversine distance calculation
- Nearby receiver discovery
- Automated notification system
- Radius-based recipient filtering

---

## 📧 Smart Notification System

When food is posted:

1. Nearby receivers are identified
2. Verified users within range are filtered
3. Notification emails are sent asynchronously
4. Receivers can claim available food

This ensures surplus food reaches recipients quickly before expiration.

---

## 🤝 Food Claim Management

Receivers can:

- Browse available food
- Claim food quantities
- View claim history

Donors can:

- Track claims
- Monitor remaining quantity
- View receiver information

### Claim Features

- Partial quantity claims
- Transaction-safe operations
- Claim history tracking
- Quantity validation
- Status updates

---

## ⚡ Performance & Reliability

### Transaction Management

- Atomic claim operations
- Consistency guarantees
- Rollback support
- Concurrent request handling

### Asynchronous Processing

- Non-blocking email notifications
- Improved API response times
- Better scalability

---

# 🏗️ Architecture

```text
┌──────────────┐
│    Client    │
└──────┬───────┘
       │
       ▼
┌────────────────────┐
│ Spring Boot APIs   │
└─────────┬──────────┘
          │
 ┌────────┼────────┐
 ▼        ▼        ▼
Security  Services Repositories
(JWT)              (JPA)

          │
          ▼
    MySQL Database

          │
          ▼
 Async Email Service

          │
          ▼
 Nearby Receiver
 Notification Engine
```

---

# 🛠️ Technology Stack

## Backend

- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate

## Authentication

- JWT Authentication
- Role-Based Authorization

## Database

- MySQL

## Communication

- Java Mail Sender
- Async Processing

## Build Tool

- Maven

---

# 📂 Project Structure

```text
src/main/java
│
├── config
│   └── SecurityConfig
│
├── controller
│   ├── AuthController
│   └── FoodPostController
│
├── service
│   ├── AuthService
│   └── FoodPostService
│
├── entity
│   ├── User
│   ├── FoodPost
│   └── FoodClaim
│
├── repository
│   ├── UserRepository
│   ├── FoodPostRepository
│   └── FoodClaimRepository
│
├── dto
│
├── jwt
│   ├── JwtService
│   └── JwtAuthFilter
│
├── exception
│   └── GlobalExceptionHandler
│
└── enums
```

---

# ⚙️ Installation

## Clone Repository

```bash
git clone https://github.com/Makzz1/foodredistribution.git
```

```bash
cd foodredistribution
```

---

## Configure Database

Update:

```properties
application.properties
```

```properties
spring.datasource.url=YOUR_DB_URL
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

---

## Configure Email

```properties
spring.mail.username=YOUR_EMAIL
spring.mail.password=YOUR_APP_PASSWORD
```

---

## Run Application

Using Maven:

```bash
mvn spring-boot:run
```

Or:

```bash
./mvnw spring-boot:run
```

---

# 🔑 Core API Modules

## Authentication

- Register User
- Login User
- Verify Email

## Food Management

- Create Food Post
- Get Available Food
- View Donor Posts
- Update Food Status

## Claim Management

- Claim Food
- View Claims
- Track Claim History

---

# 🛡️ Security Features

- JWT Token Authentication
- Stateless Sessions
- Password Encryption
- Role-Based Access Control
- Protected Endpoints
- Email Verification

---

# 📈 Scalability Considerations

Current implementation includes:

- Transaction Management
- Async Email Processing
- DTO-Based API Design
- Pagination Support
- Global Exception Handling

Future enhancements:

- Redis Caching
- Elasticsearch Geo Search
- Docker Deployment
- Kubernetes Support
- Message Queues
- Cloud Deployment

---

# 🌍 Real-World Impact

This platform aims to:

- Reduce food wastage
- Help vulnerable communities
- Improve food accessibility
- Connect donors and receivers efficiently
- Promote sustainable resource utilization

---

# 👨‍💻 Author

**Maghizhvanban E S**

Backend Developer | Spring Boot | Java

GitHub:
https://github.com/Makzz1/foodredistribution

---

# 📜 License

This project is developed for educational, research, and social impact purposes.

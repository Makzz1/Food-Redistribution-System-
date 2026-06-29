# Integration Testing Guide — FoodRedistribution Project

## What is Integration Testing?

Before diving in, understand the **Testing Pyramid**:

```
        /  E2E  \        ← Few (Slow, expensive, tests the full user flow in a browser)
       /─────────\
      / Integration \    ← Medium (Tests multiple layers working together)
     /───────────────\
    /   Unit Tests    \  ← Many (Fast, tests a single method/class in isolation)
   /───────────────────\
```

- **Unit Test:** Test one method. Example: "Does `calculateExpiry()` return the correct date?"
- **Integration Test:** Test multiple components wired together. Example: "When I call `POST /api/v1/food`, does it save to the database AND return a 201 status?"
- **E2E (End-to-End) Test:** A robot opens Chrome, clicks buttons, fills forms, and checks if the whole app works.

**Your goal:** Add **Integration Tests** to your Spring Boot backend. These tests will boot up a real (temporary) Spring context, hit your REST API endpoints, and verify the responses.

---

## Prerequisites — What You Should Already Know

> [!TIP]
> You already know all of these from building this project!

- ✅ Java basics (classes, annotations, interfaces)
- ✅ Spring Boot fundamentals (`@RestController`, `@Service`, `@Repository`)
- ✅ How REST APIs work (GET, POST, PUT, DELETE)
- ✅ How your database entities and JPA repositories work
- ✅ How Docker Compose runs your app

---

## What You Need to Learn (Study Plan)

### Phase 1: Core Concepts (Study First)

#### 1. JUnit 5 — The Test Runner
JUnit is the engine that runs your tests. Learn these annotations:

| Annotation | What it does |
|---|---|
| `@Test` | Marks a method as a test case |
| `@BeforeEach` | Runs before EVERY test (setup) |
| `@AfterEach` | Runs after EVERY test (cleanup) |
| `@DisplayName("...")` | Gives a human-readable name to a test |

**Key methods (Assertions):**
```java
assertEquals(expected, actual);      // "Is 5 equal to 5?"
assertNotNull(object);               // "Is this object NOT null?"
assertTrue(condition);               // "Is this true?"
assertThrows(Exception.class, () -> code); // "Does this throw an error?"
```

> [!NOTE]
> **Where to learn:** Search YouTube for "JUnit 5 Tutorial for Beginners" (~20 min video).

---

#### 2. Mockito — Faking Dependencies
Sometimes you don't want to actually send an email or call AWS S3 during a test. Mockito lets you create "fake" versions of your services.

```java
// Instead of actually sending an email:
@Mock
private EmailService emailService;

// Tell the fake: "When someone calls sendEmail(), just return true"
when(emailService.sendEmail(any())).thenReturn(true);
```

> [!NOTE]
> **Where to learn:** Search YouTube for "Mockito Tutorial Spring Boot" (~25 min video).

---

#### 3. `@SpringBootTest` — The Integration Test Annotation
This is the magic annotation. When you put `@SpringBootTest` on a test class, Spring Boot will:
1. Start the entire application context (just like `docker-compose up` but in-memory)
2. Connect to a test database
3. Wire up all your `@Service`, `@Repository`, and `@Controller` beans
4. Let you call your APIs and check the results

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {
    // Spring is fully running here!
}
```

---

#### 4. `MockMvc` — Calling Your APIs Without a Browser
`MockMvc` lets you simulate HTTP requests (GET, POST, etc.) inside your tests without needing Postman or a browser.

```java
mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
                "name": "Test User",
                "email": "test@example.com",
                "password": "password123"
            }
        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test User"));
```

> [!NOTE]
> **Where to learn:** Search YouTube for "Spring Boot MockMvc Integration Testing" (~30 min video).

---

#### 5. H2 / Testcontainers — The Test Database
You do NOT want your tests to touch your real Supabase PostgreSQL database! You have two options:

| Option | What it is | Pros | Cons |
|---|---|---|---|
| **H2** | A tiny in-memory database that pretends to be PostgreSQL | Super fast, zero setup | Some PostgreSQL-specific queries may not work |
| **Testcontainers** | Spins up a real PostgreSQL inside a Docker container just for the test | 100% real database behavior | Slightly slower, requires Docker running |

> [!IMPORTANT]
> **Recommendation for you:** Start with **H2** for simplicity. If you run into SQL compatibility issues, switch to **Testcontainers**.

---

### Phase 2: What to Test (Priority Order)

Once you understand the concepts above, here is what to test in your project, in order of importance:

1. **Auth Endpoints** — Register, Login, Forgot Password
   - Can a user register successfully?
   - Does login return a valid JWT token?
   - Does login fail with a wrong password?

2. **Food Post Endpoints** — Create, List, Delete
   - Can a donor create a food post?
   - Does a receiver see available food posts?
   - Does claiming reduce the available quantity?

3. **Claim Flow** — The full lifecycle
   - Can a receiver claim food?
   - Can a donor confirm the claim?
   - Does cancellation work?

4. **Admin Endpoints** — Protected routes
   - Does a normal user get a 403 Forbidden?
   - Can an admin access admin-only routes?

---

## What You Need to Install

> [!TIP]
> You already have most of these! Spring Boot includes JUnit 5 and Mockito by default.

### Already in your `pom.xml`:
```xml
<!-- This one dependency gives you JUnit 5 + Mockito + MockMvc + Spring Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### You need to ADD (for the test database):
```xml
<!-- H2 in-memory database for tests -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### You need to CREATE:
A test-specific configuration file at:
`src/test/resources/application-test.properties`

```properties
# Use H2 instead of PostgreSQL for tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Disable S3 for tests (use local storage)
storage.type=local

# Use a dummy JWT secret for tests
jwt.secret=test-secret-key-that-is-at-least-32-characters-long
```

---

## File Structure for Tests

Your tests should mirror your main source code structure:

```
src/
├── main/java/com/foodredistribution/
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── FoodController.java
│   └── service/
│       └── UserService.java
│
└── test/java/com/foodredistribution/     ← YOUR TESTS GO HERE
    ├── controller/
    │   ├── AuthControllerTest.java       ← Tests for AuthController
    │   └── FoodControllerTest.java       ← Tests for FoodController
    └── service/
        └── UserServiceTest.java          ← Unit tests for UserService
```

---

## How to Run Tests

```bash
# Run ALL tests
mvn test

# Run a single test class
mvn test -Dtest=AuthControllerTest

# Run tests with detailed output
mvn test -Dtest=AuthControllerTest -Dsurefire.useFile=false
```

---

## Recommended Learning Path (Order)

| Step | Topic | Time | Resource |
|---|---|---|---|
| 1 | JUnit 5 basics | 20 min | YouTube: "JUnit 5 Tutorial" |
| 2 | Assertions & test lifecycle | 15 min | YouTube (same video usually covers this) |
| 3 | Mockito basics | 25 min | YouTube: "Mockito Tutorial Spring Boot" |
| 4 | `@SpringBootTest` + `MockMvc` | 30 min | YouTube: "Spring Boot Integration Testing" |
| 5 | H2 test database setup | 10 min | Read the H2 section above |
| 6 | Write your first test (AuthController) | 45 min | Try it yourself! |
| 7 | Add tests for FoodController | 30 min | Apply what you learned |

> [!IMPORTANT]
> **Total estimated study time: ~3 hours.** After that, you should be able to write tests independently. When you are ready to start coding the tests, come back and I will guide you through any issues!

---

## Bonus: Adding Tests to Your CI/CD Pipeline

Once your tests are working locally, we can add a `test` step to your `.github/workflows/deploy.yml` so that GitHub automatically runs all tests BEFORE deploying. If any test fails, the deployment is blocked! This prevents broken code from ever reaching your live server.

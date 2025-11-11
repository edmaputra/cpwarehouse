# Setup Instructions for Integration Tests

## ✅ Testcontainers with Podman is Configured!

You have **3 options** to run the integration tests:

---

## Option 1: Use Podman (Recommended - You Have It!)

### Step 1: Initialize Podman Machine (One-time setup)
```powershell
podman machine init
podman machine start
```

### Step 2: Configure Environment Variables
```powershell
.\configure-podman-tests.ps1
```

### Step 3: Run Tests
```powershell
.\mvnw test
```

---

## Option 2: Use Docker Compose (Simpler)

### Step 1: Start MongoDB with Podman Compose
```powershell
# Using podman-compose
podman-compose -f docker-compose.test.yml up -d

# OR using docker-compose with Podman
docker-compose -f docker-compose.test.yml up -d
```

### Step 2: Run Tests Without Testcontainers
Comment out these lines in `ItemControllerIntegrationTest.java`:
```java
// @Testcontainers  // Comment this out
// @Container       // Comment this out
// static MongoDBContainer mongoDBContainer = ...  // Comment this out
// @DynamicPropertySource  // Comment this out
// static void setProperties(...) {...}  // Comment this out
```

Then run:
```powershell
.\mvnw test -Dspring.data.mongodb.uri=mongodb://localhost:27017/test_db
```

### Step 3: Stop MongoDB
```powershell
podman-compose -f docker-compose.test.yml down
```

---

## Option 3: Manual MongoDB with Podman (Quickest for Now)

### Start MongoDB Manually
```powershell
podman run -d -p 27017:27017 --name cpwarehouse-mongodb mongo:7.0
```

### Run Tests (With Testcontainers Disabled)
Comment out the Testcontainers annotations (see Option 2), then:
```powershell
.\mvnw test -Dspring.data.mongodb.uri=mongodb://localhost:27017/test_db
```

### Stop MongoDB
```powershell
podman stop cpwarehouse-mongodb
podman rm cpwarehouse-mongodb
```

---

## Quick Start (For Right Now)

Since Podman machine is not initialized yet, here's the **fastest way** to run tests:

```powershell
# 1. Start MongoDB
podman run -d -p 27017:27017 --name mongo-test mongo:7.0

# 2. Wait a few seconds for MongoDB to start
Start-Sleep -Seconds 5

# 3. Run tests (Testcontainers will be disabled automatically when it can't connect)
.\mvnw test -Dspring.data.mongodb.uri=mongodb://localhost:27017/test_db

# 4. Stop MongoDB when done
podman stop mongo-test
podman rm mongo-test
```

---

## Recommended Setup for Future

Initialize Podman machine once:
```powershell
podman machine init --cpus 2 --memory 4096 --disk-size 20
podman machine start
```

Then Testcontainers will work automatically!

---

## Current Status

- ✅ Podman 5.4.0 is installed
- ⏳ Podman machine needs initialization
- ✅ Testcontainers dependencies are configured
- ✅ Integration tests are ready

Choose the option that works best for you!

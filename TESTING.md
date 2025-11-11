# Testing Environment Setup

This guide explains how to run tests with MongoDB authentication.

## Test MongoDB Configuration

The test environment uses a separate MongoDB instance to avoid conflicts with development:

- **Port**: 27018 (dev uses 27017)
- **Database**: cpwarehouse_test
- **Username**: cpwarehouse_test_user
- **Password**: cpwarehouse_test_pass
- **Container**: cpwarehouse-mongodb-test

## Quick Start

### 1. Start Test MongoDB

```powershell
.\docker-test.ps1 start
```

This will:
- Start MongoDB on port 27018
- Automatically create test user with readWrite permissions
- Initialize the database

### 2. Run Tests

```powershell
# Run all tests
.\mvnw.cmd test

# Run specific test
.\mvnw.cmd test -Dtest=ItemControllerIntegrationTest

# Run tests with verbose output
.\mvnw.cmd test -X
```

### 3. Stop Test MongoDB

```powershell
.\docker-test.ps1 stop
```

## Management Commands

```powershell
# Start test MongoDB
.\docker-test.ps1 start

# Stop test MongoDB
.\docker-test.ps1 stop

# Restart test MongoDB
.\docker-test.ps1 restart

# Clean everything (fresh start)
.\docker-test.ps1 clean

# View logs
.\docker-test.ps1 logs

# Check status
.\docker-test.ps1 status

# Open MongoDB shell
.\docker-test.ps1 shell

# Create user manually (if needed)
.\docker-test.ps1 createuser
```

## Connection Details

### Application Properties (application-test.yaml)
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://cpwarehouse_test_user:cpwarehouse_test_pass@localhost:27018/cpwarehouse_test?authSource=cpwarehouse_test
```

### MongoDB Compass / Tools
```
mongodb://cpwarehouse_test_user:cpwarehouse_test_pass@localhost:27018/cpwarehouse_test?authSource=cpwarehouse_test
```

## Using Testcontainers

For CI/CD or isolated testing, you can use Testcontainers instead of the local MongoDB:

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ItemControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(
        DockerImageName.parse("mongo:7.0")
    );

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    // ... tests
}
```

Testcontainers automatically:
- Starts a MongoDB instance
- Creates users and databases
- Cleans up after tests
- Works in CI/CD without manual setup

## Troubleshooting

### Authentication Failed

1. **Create user manually**:
   ```powershell
   .\docker-test.ps1 createuser
   ```

2. **Verify user exists**:
   ```powershell
   .\docker-test.ps1 shell
   # In MongoDB shell:
   db.getUsers()
   ```

3. **Clean start**:
   ```powershell
   .\docker-test.ps1 clean
   .\docker-test.ps1 start
   # Wait 10 seconds
   .\docker-test.ps1 createuser
   ```

### Port Already in Use

If port 27018 is taken, edit `docker-compose.test.yml`:

```yaml
ports:
  - "27019:27017"  # Use different port
```

Then update `application-test.yaml`:
```yaml
uri: mongodb://cpwarehouse_test_user:cpwarehouse_test_pass@localhost:27019/cpwarehouse_test?authSource=cpwarehouse_test
```

### Tests Hang or Timeout

1. Check MongoDB is running:
   ```powershell
   .\docker-test.ps1 status
   ```

2. Check logs:
   ```powershell
   .\docker-test.ps1 logs
   ```

3. Restart:
   ```powershell
   .\docker-test.ps1 restart
   ```

### Clean Everything

If nothing works, clean slate:

```powershell
# Stop all MongoDB containers
docker stop cpwarehouse-mongodb-test cpwarehouse-mongodb
docker rm cpwarehouse-mongodb-test cpwarehouse-mongodb

# Remove volumes
docker volume prune

# Start fresh
.\docker-test.ps1 start

# Wait 10 seconds, then run tests
.\mvnw.cmd test
```

## Best Practices

### 1. Development vs Test Separation

- **Development MongoDB**: Port 27017, `docker-compose.yml`
- **Test MongoDB**: Port 27018, `docker-compose.test.yml`

Always use separate instances to avoid data conflicts.

### 2. Clean Tests

```java
@BeforeEach
void setUp() {
    itemRepository.deleteAll(); // Clean before each test
}

@AfterEach
void tearDown() {
    itemRepository.deleteAll(); // Clean after each test
}
```

### 3. Test Data Isolation

Create unique data per test to avoid conflicts:

```java
@Test
void testCreateItem() {
    String uniqueSku = "TEST-" + UUID.randomUUID();
    // Use uniqueSku in test
}
```

### 4. Use Test Profiles

Activate test profile automatically:

```java
@SpringBootTest
@ActiveProfiles("test")
class MyTest {
    // Uses application-test.yaml
}
```

## Running Tests in Different Environments

### Local Development
```powershell
.\docker-test.ps1 start
.\mvnw.cmd test
```

### CI/CD Pipeline
Use Testcontainers (no manual MongoDB setup needed):
```java
@Testcontainers
class IntegrationTest {
    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");
}
```

### Docker-based Testing
```powershell
# Build
docker-compose -f docker-compose.test.yml run --rm maven mvn clean test

# Or use a test profile
docker-compose -f docker-compose.test.yml up -d
.\mvnw.cmd test -Dspring.profiles.active=test
```

## Verification Checklist

Before running tests, verify:

- ✅ Test MongoDB is running: `.\docker-test.ps1 status`
- ✅ User exists: `.\docker-test.ps1 shell` → `db.getUsers()`
- ✅ Connection string is correct in `application-test.yaml`
- ✅ Port 27018 is accessible
- ✅ No port conflicts with dev MongoDB (27017)

## Quick Commands Summary

```powershell
# Setup
.\docker-test.ps1 clean && .\docker-test.ps1 start

# Run tests
.\mvnw.cmd test

# Teardown
.\docker-test.ps1 stop

# Fresh start
.\docker-test.ps1 clean && .\docker-test.ps1 start && .\mvnw.cmd test
```

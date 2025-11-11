# Running Integration Tests

This project uses **Testcontainers** for integration testing with MongoDB. There are multiple ways to run the tests depending on your setup.

## Prerequisites

Choose one of the following options:

### Option 1: Docker Desktop (Recommended for Testcontainers)
- **Docker Desktop** must be installed and **running**
- Testcontainers will automatically start a MongoDB container

### Option 2: Local MongoDB Instance
- Install MongoDB locally or run it via Docker manually
- Start MongoDB on `localhost:27017`

### Option 3: Podman (Alternative to Docker)
- Install Podman
- Configure Testcontainers to use Podman (see Podman setup below)

---

## Running Tests

### With Docker Desktop (Testcontainers)

1. **Start Docker Desktop**
2. Run the tests:
   ```bash
   ./mvnw test
   ```

The integration tests will:
- Automatically pull the MongoDB Docker image (mongo:7.0) if not present
- Start a MongoDB container
- Run all tests
- Stop and remove the container

### With Local MongoDB

If you prefer not to use Docker, you can run MongoDB locally:

1. **Start MongoDB locally:**
   ```bash
   # Using Docker manually
   docker run -d -p 27017:27017 --name mongodb-test mongo:7.0
   
   # OR using Podman
   podman run -d -p 27017:27017 --name mongodb-test mongo:7.0
   ```

2. **Disable Testcontainers** by commenting out the `@Testcontainers` annotation and the `@Container` field in the test class

3. **Run the tests:**
   ```bash
   ./mvnw test -Dspring.data.mongodb.uri=mongodb://localhost:27017/test_db
   ```

4. **Clean up:**
   ```bash
   docker stop mongodb-test && docker rm mongodb-test
   # OR
   podman stop mongodb-test && podman rm mongodb-test
   ```

---

## Podman Setup for Testcontainers

If you're using Podman instead of Docker:

### Windows (PowerShell)
```powershell
$env:DOCKER_HOST="unix:///var/run/docker.sock"
$env:TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE="/var/run/docker.sock"
$env:TESTCONTAINERS_RYUK_DISABLED="true"
```

### Linux/macOS (Bash)
```bash
export DOCKER_HOST=unix:///var/run/docker.sock
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
export TESTCONTAINERS_RYUK_DISABLED=true
```

Then run tests as normal:
```bash
./mvnw test
```

---

## Troubleshooting

### Error: "Could not find a valid Docker environment"

**Solution:**
- Ensure Docker Desktop is running
- Check Docker is accessible: `docker ps`
- If using Podman, ensure environment variables are set correctly
- Try restarting your IDE/terminal after starting Docker

### Error: "Cannot connect to MongoDB"

**Solution:**
- If using local MongoDB, ensure it's running on port 27017
- Check MongoDB connection: `mongosh mongodb://localhost:27017`
- Verify firewall settings

### Tests are slow

**First run:** Testcontainers needs to download the MongoDB image (~150MB). Subsequent runs will be much faster.

---

## Running Specific Tests

Run a single test class:
```bash
./mvnw test -Dtest=ItemControllerIntegrationTest
```

Run a specific test method:
```bash
./mvnw test -Dtest=ItemControllerIntegrationTest#createItem_WithValidRequest_ShouldReturnCreatedItem
```

Run all integration tests:
```bash
./mvnw test
```

---

## CI/CD Considerations

For CI/CD pipelines (GitHub Actions, GitLab CI, etc.), Docker is usually available. Testcontainers works out of the box.

Example GitHub Actions:
```yaml
- name: Run tests
  run: ./mvnw test
```

Docker will be automatically available in most CI environments.

---

## Test Configuration

Test configuration is in `src/test/resources/application-test.yaml`:
- MongoDB URI (can be overridden by Testcontainers)
- Logging levels
- Test-specific settings

---

## Current Test Coverage

The `ItemControllerIntegrationTest` includes 17 test cases covering:
- ✅ Create items (valid/invalid/duplicate SKU)
- ✅ Get all items (pagination/filtering/search)
- ✅ Get item by ID
- ✅ Update items
- ✅ Delete items (soft delete/hard delete)
- ✅ Validation errors
- ✅ Not found scenarios

All tests use an isolated MongoDB instance and clean up after execution.

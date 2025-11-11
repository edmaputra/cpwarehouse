# MongoDB User Creation Guide

## Problem
You may encounter "Authentication Failed" errors when running tests or connecting to MongoDB. This happens when the application user hasn't been created properly in MongoDB.

## Solution

### Option 1: Automatic Creation (Recommended for Fresh Start)

When you start MongoDB for the **first time** using docker-compose, the initialization script automatically creates the user:

```powershell
.\docker.ps1 clean    # Remove old data
.\docker.ps1 start    # Start fresh - user will be created automatically
```

Wait about 10-15 seconds for MongoDB to fully initialize.

### Option 2: Manual Creation (For Existing MongoDB)

If MongoDB is already running but the user wasn't created, use the provided script:

```powershell
.\create-mongo-user.ps1
```

Or using the docker management script:

```powershell
.\docker.ps1 createuser
```

### Option 3: Direct MongoDB Command

If the scripts don't work, create the user directly:

```powershell
docker exec -it cpwarehouse-mongodb mongosh admin -u admin -p admin123 --eval "
  db = db.getSiblingDB('cpwarehouse');
  db.createUser({
    user: 'cpwarehouse_user',
    pwd: 'cpwarehouse_pass',
    roles: [
      { role: 'readWrite', db: 'cpwarehouse' },
      { role: 'dbAdmin', db: 'cpwarehouse' }
    ]
  });
  print('User created successfully');
"
```

## Verify User Creation

Test the connection with the created user:

```powershell
docker exec -it cpwarehouse-mongodb mongosh cpwarehouse -u cpwarehouse_user -p cpwarehouse_pass
```

Expected output:
```
Current Mongo DB version: 7.0.x
Connecting to: mongodb://localhost:27017/cpwarehouse
Connected successfully
```

## User Details

- **Username**: `cpwarehouse_user`
- **Password**: `cpwarehouse_pass`
- **Database**: `cpwarehouse`
- **Auth Database**: `cpwarehouse`
- **Roles**: 
  - `readWrite` - Can read and write data
  - `dbAdmin` - Can manage indexes and view statistics

## Connection Strings

### Application (application.yaml)
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://cpwarehouse_user:cpwarehouse_pass@localhost:27017/cpwarehouse?authSource=cpwarehouse
```

### MongoDB Compass / Tools
```
mongodb://cpwarehouse_user:cpwarehouse_pass@localhost:27017/cpwarehouse?authSource=cpwarehouse
```

### Java Connection
```java
mongodb://cpwarehouse_user:cpwarehouse_pass@localhost:27017/cpwarehouse?authSource=cpwarehouse
```

## Troubleshooting

### Error: "Authentication failed"

**Cause**: User doesn't exist or wrong credentials

**Solution**:
1. Check if user exists:
   ```powershell
   docker exec -it cpwarehouse-mongodb mongosh admin -u admin -p admin123 --eval "db.getSiblingDB('cpwarehouse').getUsers()"
   ```

2. If user doesn't exist, create it:
   ```powershell
   .\create-mongo-user.ps1
   ```

3. If user exists but still failing, drop and recreate:
   ```powershell
   docker exec -it cpwarehouse-mongodb mongosh admin -u admin -p admin123 --eval "
     db = db.getSiblingDB('cpwarehouse');
     db.dropUser('cpwarehouse_user');
     db.createUser({
       user: 'cpwarehouse_user',
       pwd: 'cpwarehouse_pass',
       roles: [
         { role: 'readWrite', db: 'cpwarehouse' },
         { role: 'dbAdmin', db: 'cpwarehouse' }
       ]
     });
   "
   ```

### Error: "Container not found"

**Cause**: MongoDB container isn't running

**Solution**:
```powershell
.\docker.ps1 start
```

Wait 10-15 seconds, then create user:
```powershell
.\create-mongo-user.ps1
```

### Error: "Could not connect to server"

**Cause**: MongoDB hasn't finished starting

**Solution**: Wait 10-20 seconds and try again

Check MongoDB logs:
```powershell
.\docker.ps1 logs mongodb
```

Look for: `"msg":"Waiting for connections","attr":{"port":27017}}`

### Tests Still Failing

If integration tests still fail after creating the user:

1. **Verify application.yaml connection string**:
   ```yaml
   spring:
     data:
       mongodb:
         uri: mongodb://cpwarehouse_user:cpwarehouse_pass@localhost:27017/cpwarehouse?authSource=cpwarehouse
   ```

2. **Check test profile (application-test.yaml)**:
   Should use same credentials or Testcontainers

3. **Restart application**:
   ```powershell
   # Stop application if running
   # Then
   .\mvnw.cmd spring-boot:run
   ```

4. **Run tests**:
   ```powershell
   .\mvnw.cmd test
   ```

## Security Notes

⚠️ **These credentials are for LOCAL DEVELOPMENT only!**

For production:
- Use strong, randomly generated passwords
- Use environment variables for credentials
- Enable SSL/TLS
- Use role-based access control (RBAC)
- Consider using MongoDB Atlas or managed services
- Rotate credentials regularly
- Use secrets management (Vault, AWS Secrets Manager, etc.)

## Quick Reference Commands

```powershell
# Start everything fresh
.\docker.ps1 clean && .\docker.ps1 start

# Create user
.\create-mongo-user.ps1

# Test connection
docker exec -it cpwarehouse-mongodb mongosh cpwarehouse -u cpwarehouse_user -p cpwarehouse_pass

# View users
docker exec -it cpwarehouse-mongodb mongosh admin -u admin -p admin123 --eval "db.getSiblingDB('cpwarehouse').getUsers()"

# Drop user (if needed to recreate)
docker exec -it cpwarehouse-mongodb mongosh admin -u admin -p admin123 --eval "db.getSiblingDB('cpwarehouse').dropUser('cpwarehouse_user')"

# Check MongoDB logs
.\docker.ps1 logs mongodb
```

## For CI/CD

In CI/CD pipelines, use Testcontainers instead:

```java
@Container
static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

@DynamicPropertySource
static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
}
```

This automatically handles authentication without manual user creation.

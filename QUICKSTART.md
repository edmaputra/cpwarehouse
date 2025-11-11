# Quick Start Guide - Local Development

## Prerequisites

- Docker Desktop installed and running
- Java 21 or later
- Maven (or use included `mvnw`)

## Step 1: Start MongoDB

### Option A: Using PowerShell Script (Windows - Recommended)
```powershell
.\docker.ps1 start
```

### Option B: Using Docker Compose Directly
```bash
docker-compose up -d
```

### Option C: Using Bash Script (Linux/Mac)
```bash
chmod +x docker.sh
./docker.sh start
```

## Step 2: Verify MongoDB is Running

Check service status:
```powershell
.\docker.ps1 status
```

Or:
```bash
docker-compose ps
```

Expected output:
```
NAME                          STATUS              PORTS
cpwarehouse-mongodb           Up (healthy)        0.0.0.0:27017->27017/tcp
cpwarehouse-mongo-express     Up                  0.0.0.0:8081->8081/tcp
```

## Step 2.5: Create MongoDB User (If Needed)

If you get authentication errors, manually create the application user:

```powershell
.\docker.ps1 createuser
```

Or run directly:
```powershell
.\create-mongo-user.ps1
```

This creates:
- **Username**: cpwarehouse_user
- **Password**: cpwarehouse_pass
- **Roles**: readWrite, dbAdmin on cpwarehouse database

## Step 3: Access Mongo Express (Optional)

Open your browser and navigate to:
- **URL**: http://localhost:8081
- **Username**: admin
- **Password**: admin123

You can browse and manage your database through this web interface.

## Step 4: Run the Application

### Compile the project
```bash
./mvnw clean compile
```

### Run the application
```bash
./mvnw spring-boot:run
```

Or if you're using PowerShell:
```powershell
.\mvnw.cmd spring-boot:run
```

## Step 5: Test the API

### Create an Item
```bash
curl -X POST http://localhost:8080/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "ITEM-001",
    "name": "Sample Item",
    "description": "This is a sample item",
    "basePrice": 99.99
  }'
```

Or using PowerShell:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/items" `
  -Method Post `
  -ContentType "application/json" `
  -Body (@{
    sku = "ITEM-001"
    name = "Sample Item"
    description = "This is a sample item"
    basePrice = 99.99
  } | ConvertTo-Json)
```

### Get All Items
```bash
curl http://localhost:8080/api/v1/items
```

Or PowerShell:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/items"
```

## Connection Details

### MongoDB
- **Host**: localhost
- **Port**: 27017
- **Database**: cpwarehouse
- **Username**: admin
- **Password**: admin123
- **Connection String**: `mongodb://admin:admin123@localhost:27017/cpwarehouse?authSource=admin`

### Mongo Express Web UI
- **URL**: http://localhost:8081
- **Username**: admin
- **Password**: admin123

### Application API
- **Base URL**: http://localhost:8080
- **API Endpoints**: http://localhost:8080/api/v1/items

## Useful Commands

### MongoDB Management

```powershell
# Start services
.\docker.ps1 start

# Stop services
.\docker.ps1 stop

# Restart services
.\docker.ps1 restart

# View logs
.\docker.ps1 logs

# Clean everything (removes data)
.\docker.ps1 clean

# Open MongoDB shell
.\docker.ps1 shell
```

### Application Development

```bash
# Compile
./mvnw clean compile

# Run tests
./mvnw test

# Run application
./mvnw spring-boot:run

# Package application
./mvnw clean package
```

## Troubleshooting

### MongoDB Authentication Failed
If you see "Authentication failed" errors:

1. **Create the user manually:**
   ```powershell
   .\docker.ps1 createuser
   ```
   Or:
   ```powershell
   .\create-mongo-user.ps1
   ```

2. **Verify user was created:**
   ```powershell
   docker exec -it cpwarehouse-mongodb mongosh cpwarehouse -u cpwarehouse_user -p cpwarehouse_pass
   ```

3. **If still failing, recreate everything:**
   ```powershell
   .\docker.ps1 clean
   .\docker.ps1 start
   # Wait 10 seconds for initialization
   .\docker.ps1 createuser
   ```

### MongoDB Connection Error
1. Check if MongoDB is running: `.\docker.ps1 status`
2. Check logs: `.\docker.ps1 logs`
3. Restart: `.\docker.ps1 restart`
4. Clean start: `.\docker.ps1 clean` then `.\docker.ps1 start`

### Port Already in Use
If port 27017 or 8081 is already in use:
1. Stop other MongoDB instances
2. Or edit `docker-compose.yml` to use different ports

### Application Can't Connect
Verify the connection string in `application.yaml`:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://admin:admin123@localhost:27017/cpwarehouse?authSource=admin
```

## Next Steps

1. ✅ MongoDB is running
2. ✅ Application is connected
3. 📝 Explore the API endpoints
4. 🧪 Run integration tests
5. 🚀 Start building features!

For more details, see:
- [DOCKER.md](DOCKER.md) - Docker configuration details
- [README.md](README.md) - Project documentation
- [WAREHOUSE_SERVICE_PLAN.md](WAREHOUSE_SERVICE_PLAN.md) - Service architecture

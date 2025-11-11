# Docker Compose for Local Development

This Docker Compose setup provides MongoDB and Mongo Express for local development and testing.

## Services

### 1. MongoDB
- **Image**: mongo:7.0
- **Port**: 27017
- **Container Name**: cpwarehouse-mongodb
- **Credentials**:
  - Username: `admin`
  - Password: `admin123`
  - Database: `cpwarehouse`

### 2. Mongo Express (Web UI)
- **Image**: mongo-express:1.0.2
- **Port**: 8081
- **Container Name**: cpwarehouse-mongo-express
- **Web UI Credentials**:
  - Username: `admin`
  - Password: `admin123`

## Quick Start

### Start Services
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

### Stop and Remove Volumes (Clean Start)
```bash
docker-compose down -v
```

### View Logs
```bash
# All services
docker-compose logs -f

# MongoDB only
docker-compose logs -f mongodb

# Mongo Express only
docker-compose logs -f mongo-express
```

### Check Service Status
```bash
docker-compose ps
```

## Access Points

### MongoDB Connection
- **Host**: localhost
- **Port**: 27017
- **Connection String**: `mongodb://admin:admin123@localhost:27017/cpwarehouse?authSource=admin`

### Mongo Express Web UI
- **URL**: http://localhost:8081
- **Username**: admin
- **Password**: admin123

## Application Configuration

Update your `application.yaml` to connect to the local MongoDB:

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: cpwarehouse
      username: admin
      password: admin123
      authentication-database: admin
```

Or use connection URI:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://admin:admin123@localhost:27017/cpwarehouse?authSource=admin
```

## Data Initialization

The MongoDB container will automatically execute initialization scripts from:
```
src/main/resources/db/init/
```

These scripts run only when the container is created for the first time.

## Volumes

- **mongodb_data**: Persists MongoDB data between container restarts

## Network

All services run on the `cpwarehouse-network` bridge network, allowing them to communicate with each other.

## Health Check

MongoDB includes a health check that:
- Runs every 10 seconds
- Times out after 5 seconds
- Retries up to 5 times
- Waits 40 seconds before starting checks

Mongo Express waits for MongoDB to be healthy before starting.

## Troubleshooting

### Container won't start
```bash
# Check logs
docker-compose logs

# Remove old containers and volumes
docker-compose down -v

# Start fresh
docker-compose up -d
```

### Can't connect to MongoDB
1. Verify container is running: `docker-compose ps`
2. Check MongoDB logs: `docker-compose logs mongodb`
3. Test connection: `docker exec -it cpwarehouse-mongodb mongosh -u admin -p admin123`

### Reset Everything
```bash
# Stop and remove containers, networks, volumes
docker-compose down -v

# Remove any dangling volumes
docker volume prune

# Start fresh
docker-compose up -d
```

## Production Notes

⚠️ **This configuration is for LOCAL DEVELOPMENT only!**

For production:
- Use stronger passwords
- Configure proper authentication
- Set up replica sets
- Configure backup strategies
- Use environment variables for secrets
- Enable SSL/TLS
- Configure proper resource limits

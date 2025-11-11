# CP Warehouse - Warehouse Management System

A modern warehouse management system built with Spring Boot 3, Java 21, and MongoDB.

## 🚀 Quick Start

See [QUICKSTART.md](QUICKSTART.md) for detailed setup instructions.

**TL;DR:**
```powershell
# 1. Start MongoDB
.\docker.ps1 start

# 2. Run the application
.\mvnw.cmd spring-boot:run

# 3. Test the API
curl http://localhost:8080/api/v1/items
```

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Docker](#docker)
- [Contributing](#contributing)

## ✨ Features

- **Item Management**: CRUD operations for warehouse items
  - Create, Read, Update, Delete items
  - Soft delete with active/inactive status
  - Search by name or SKU
  - Pagination and filtering
  
- **Command Pattern Architecture**: Clean separation of concerns with single-responsibility commands

- **Custom Repository**: MongoTemplate-based dynamic query building

- **Entity-Specific Package Structure**: Scalable organization ready for multiple entities

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.5.7
- **Language**: Java 21
- **Database**: MongoDB 7.0
- **Object Mapping**: MapStruct 1.6.3
- **Build Tool**: Maven
- **Testing**: Testcontainers 1.21.3
- **Containerization**: Docker & Docker Compose

## 🏗 Architecture

### Command Pattern with CommandExecutor

The application uses the Command Pattern for business logic:

```
Controller → CommandExecutor → Command Interface → Command Implementation
```

**Key Benefits:**
- Single Responsibility Principle
- Type-safe command execution
- Easy to test and maintain
- Scalable for multiple entities

### Package Structure

```
src/main/java/io/github/edmaputra/cpwarehouse/
├── common/                          # Shared components
│   ├── Command.java                 # Generic command interface
│   └── CommandExecutor.java         # Centralized command executor
├── controller/                      # REST controllers
├── domain/
│   └── entity/                      # MongoDB entities
├── dto/
│   ├── request/                     # Request DTOs
│   └── response/                    # Response DTOs
├── exception/                       # Custom exceptions
├── mapper/                          # MapStruct mappers
├── repository/                      # Data access layer
│   ├── ItemRepository.java          # Standard repository methods
│   ├── ItemRepositoryCustom.java    # Custom repository interface
│   └── impl/
│       └── ItemRepositoryCustomImpl.java  # MongoTemplate implementation
└── service/
    └── item/
        └── command/                 # Item-specific commands
            ├── CreateItemCommand.java
            ├── GetAllItemsCommand.java
            ├── GetItemByIdCommand.java
            ├── UpdateItemCommand.java
            ├── DeleteItemCommand.java
            └── impl/                # Command implementations
                ├── CreateItemCommandImpl.java
                └── ...
```

## 🚦 Getting Started

### Prerequisites

- Java 21 or later
- Maven 3.9+ (or use included wrapper)
- Docker Desktop (for MongoDB)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/edmaputra/cpwarehouse.git
   cd cpwarehouse
   ```

2. **Start MongoDB**
   ```powershell
   .\docker.ps1 start
   ```

3. **Compile the project**
   ```bash
   ./mvnw clean compile
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start at `http://localhost:8080`

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints

#### Items

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/items` | Create a new item |
| `GET` | `/items` | Get all items (paginated) |
| `GET` | `/items/{id}` | Get item by ID |
| `PUT` | `/items/{id}` | Update item |
| `DELETE` | `/items/{id}` | Soft delete item |
| `DELETE` | `/items/{id}/permanent` | Hard delete item |

#### Query Parameters (GET /items)

- `page` - Page number (default: 0)
- `size` - Page size (default: 20, max: 100)
- `isActive` - Filter by active status (true/false)
- `search` - Search in name or SKU
- `sortBy` - Field to sort by (default: createdAt)
- `sortDir` - Sort direction (ASC/DESC, default: DESC)

### Example Requests

**Create Item:**
```bash
curl -X POST http://localhost:8080/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LAPTOP-HP-001",
    "name": "HP Laptop ProBook 450",
    "description": "Business laptop with Intel i7",
    "basePrice": 899.99
  }'
```

**Get Items with Filters:**
```bash
# Active items only
curl "http://localhost:8080/api/v1/items?isActive=true"

# Search by name or SKU
curl "http://localhost:8080/api/v1/items?search=laptop"

# Pagination
curl "http://localhost:8080/api/v1/items?page=0&size=10"

# Combined filters
curl "http://localhost:8080/api/v1/items?isActive=true&search=laptop&page=0&size=10&sortBy=name&sortDir=ASC"
```

## 📁 Project Structure

```
cpwarehouse/
├── src/
│   ├── main/
│   │   ├── java/                   # Java source files
│   │   └── resources/
│   │       ├── application.yaml    # Application configuration
│   │       └── db/
│   │           └── init/           # MongoDB init scripts
│   └── test/                       # Test files
├── docker-compose.yml              # Docker services
├── docker.ps1                      # Docker management (Windows)
├── docker.sh                       # Docker management (Linux/Mac)
├── pom.xml                         # Maven configuration
├── QUICKSTART.md                   # Quick start guide
├── DOCKER.md                       # Docker documentation
└── WAREHOUSE_SERVICE_PLAN.md       # Service architecture plan
```

## 💻 Development

### Build

```bash
./mvnw clean compile
```

### Run Tests

```bash
./mvnw test
```

### Package

```bash
./mvnw clean package
```

The JAR file will be in `target/cpwarehouse-0.0.1-SNAPSHOT.jar`

### Code Generation (MapStruct)

MapStruct generates mapper implementations at compile time:

```bash
./mvnw clean compile
```

Generated files: `target/generated-sources/annotations/`

## 🧪 Testing

### Unit Tests

```bash
./mvnw test
```

### Integration Tests

Integration tests use Testcontainers for MongoDB:

```bash
./mvnw verify
```

**Note:** Docker must be running for integration tests.

## 🐳 Docker

### Start Services

```powershell
.\docker.ps1 start
```

Services started:
- **MongoDB**: `localhost:27017`
- **Mongo Express**: `http://localhost:8081`

### Management Commands

```powershell
.\docker.ps1 status    # Check status
.\docker.ps1 logs      # View logs
.\docker.ps1 shell     # MongoDB shell
.\docker.ps1 stop      # Stop services
.\docker.ps1 clean     # Clean everything
```

See [DOCKER.md](DOCKER.md) for detailed Docker documentation.

## 📖 Documentation

- [QUICKSTART.md](QUICKSTART.md) - Quick start guide
- [DOCKER.md](DOCKER.md) - Docker setup and usage
- [WAREHOUSE_SERVICE_PLAN.md](WAREHOUSE_SERVICE_PLAN.md) - Service architecture

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👤 Author

**Edmaputra**
- GitHub: [@edmaputra](https://github.com/edmaputra)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- MongoDB team for the powerful database
- MapStruct team for compile-time mapping
- Testcontainers for integration testing

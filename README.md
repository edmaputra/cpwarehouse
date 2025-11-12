# CP Warehouse - E-Commerce Stock Management System

A robust warehouse management system designed for e-commerce platforms, featuring stock management, checkout/payment workflows, and concurrent transaction handling with optimistic locking.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Manual Setup](#manual-setup)
  - [Automated Setup](#automated-setup)
- [Design Decisions](#design-decisions)
- [Architecture](#architecture)
- [Assumptions](#assumptions)
- [API Documentation](#api-documentation)
- [Testing](#testing)

## Features

✅ **Item & Variant Management** - Manage products with multiple variants (e.g., size, color, configuration)  
✅ **Stock Management** - Track inventory with reservation and release capabilities  
✅ **Checkout Flow** - Reserve stock before payment to prevent overselling  
✅ **Payment Processing** - Validate payment and commit/release stock accordingly  
✅ **Concurrent Operations** - Handle multiple simultaneous transactions safely with optimistic locking  
✅ **Audit Trail** - Track all stock movements (IN, OUT, RESERVATION, RELEASE)  

## Tech Stack

- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 3.5.7** - Framework for building production-ready applications
- **MongoDB 7.0** - NoSQL database for flexible data modeling
- **Spring Data MongoDB** - Data access abstraction
- **Spring Retry** - Automatic retry for optimistic locking failures
- **Lombok** - Reduce boilerplate code
- **JUnit 5** - Unit and integration testing
- **Testcontainers** - Integration tests with real MongoDB instance

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+** (or use included Maven wrapper)
- **Docker** or **Podman** (for running MongoDB)

### Manual Setup

#### 1. Start MongoDB

**Using Docker:**
```bash
docker-compose up -d
```

**Using Podman:**
```bash
podman-compose up -d
```

This will start:
- MongoDB on port `27017`
- Mongo Express (web UI) on port `8081` (credentials: admin/admin123)

#### 2. Verify Database Connection

Wait for MongoDB to be healthy:
```bash
# Docker
docker-compose ps

# Podman
podman-compose ps
```

Access Mongo Express at: http://localhost:8081

#### 3. Build the Application

```bash
# Windows
.\mvnw.cmd clean package

# Linux/Mac
./mvnw clean package
```

#### 4. Run the Application

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

The application will start on http://localhost:8080

### Automated Setup

For a completely automated setup, you can use this script:

**Linux/Mac (setup.sh):**
```bash
#!/bin/bash

echo "=== CP Warehouse Setup ==="

# Detect container runtime
if command -v docker &> /dev/null; then
    CONTAINER_CMD="docker"
    COMPOSE_CMD="docker-compose"
elif command -v podman &> /dev/null; then
    CONTAINER_CMD="podman"
    COMPOSE_CMD="podman-compose"
else
    echo "Error: Neither docker nor podman found. Please install one of them."
    exit 1
fi

echo "Using container runtime: $CONTAINER_CMD"

# Start containers
echo "Starting MongoDB..."
$COMPOSE_CMD up -d

# Wait for MongoDB to be ready
echo "Waiting for MongoDB to be ready..."
sleep 10

# Build application
echo "Building application..."
./mvnw clean package -DskipTests

# Run application
echo "Starting application..."
./mvnw spring-boot:run
```

**Windows (setup.ps1):**
```powershell
Write-Host "=== CP Warehouse Setup ===" -ForegroundColor Green

# Detect container runtime
$containerCmd = $null
$composeCmd = $null

if (Get-Command docker -ErrorAction SilentlyContinue) {
    $containerCmd = "docker"
    $composeCmd = "docker-compose"
} elseif (Get-Command podman -ErrorAction SilentlyContinue) {
    $containerCmd = "podman"
    $composeCmd = "podman-compose"
} else {
    Write-Host "Error: Neither docker nor podman found. Please install one of them." -ForegroundColor Red
    exit 1
}

Write-Host "Using container runtime: $containerCmd" -ForegroundColor Cyan

# Start containers
Write-Host "Starting MongoDB..." -ForegroundColor Cyan
& $composeCmd up -d

# Wait for MongoDB to be ready
Write-Host "Waiting for MongoDB to be ready..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

# Build application
Write-Host "Building application..." -ForegroundColor Cyan
.\mvnw.cmd clean package -DskipTests

# Run application
Write-Host "Starting application..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run
```

Make the script executable and run:
```bash
# Linux/Mac
chmod +x setup.sh
./setup.sh

# Windows PowerShell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\setup.ps1
```

## Design Decisions

### 1. Command Pattern

We implemented the **Command Pattern** to encapsulate business operations as command objects. This provides several benefits:

**Benefits:**
- ✅ **Single Responsibility Principle (SRP)** - Each command handles one specific operation
- ✅ **Separation of Concerns** - Business logic separated from controllers and repositories
- ✅ **Testability** - Easy to unit test individual commands in isolation
- ✅ **Extensibility** - New commands can be added without modifying existing code
- ✅ **Consistency** - Uniform way to execute operations through CommandExecutor

**Implementation:**
```java
// Command interface
public interface Command<I, O> {
  O execute(I input);
}

// Command executor
public class CommandExecutor {
  public <I, O> O execute(Class<? extends Command<I, O>> commandClass, I input) {
    Command<I, O> command = applicationContext.getBean(commandClass);
    return command.execute(input);
  }
}
```

**Example Commands:**
- `GetItemByIdCommand` - Retrieve item details
- `GetVariantByIdCommand` - Retrieve variant details
- `ProcessCheckoutCommand` - Execute checkout workflow
- `ProcessPaymentCommand` - Execute payment workflow

### 2. Single Responsibility Principle (SRP)

Each class has **one reason to change**:

- **Controllers** - Only handle HTTP requests/responses
- **Commands** - Only execute specific business logic
- **Repositories** - Only handle data access
- **Entities** - Only represent domain models
- **DTOs** - Only transfer data between layers

**Example:**
```java
// ProcessCheckoutCommandImpl has ONE responsibility:
// Execute the checkout workflow (validate, calculate, reserve stock)
@Service
public class ProcessCheckoutCommandImpl implements ProcessCheckoutCommand {
  @Override
  public CheckoutResponse execute(Request request) {
    // 1. Validate item and variant using commands
    // 2. Calculate price
    // 3. Check availability
    // 4. Reserve stock
    // 5. Create checkout record
  }
}
```

### 3. Optimistic Locking

We use **@Version** annotation for concurrent transaction handling:

**Why Optimistic Locking?**
- ✅ Better performance than pessimistic locking (no database locks)
- ✅ Suitable for e-commerce (conflicts are relatively rare)
- ✅ Allows multiple users to read simultaneously
- ✅ Detects concurrent modifications automatically

**Implementation:**
```java
@Document(collection = "stock")
public class Stock {
  @Version
  private Long version;  // Automatically managed by Spring Data
  
  private Integer quantity;
  private Integer reservedQuantity;
}
```

**Retry Strategy:**
- Automatic retry on `OptimisticLockingFailureException`
- Maximum 5 attempts with exponential backoff (100ms to 800ms)
- Configured via `@Retryable` annotation

### 4. Stock Reservation Pattern

**Two-phase commit approach:**

**Phase 1 - Checkout (Reserve):**
- Validate item availability
- Reserve stock (increase `reservedQuantity`, keep `quantity` unchanged)
- Create RESERVATION movement
- Create CheckoutItem with PENDING status

**Phase 2 - Payment (Commit):**
- Decrease both `reservedQuantity` and `quantity`
- Create OUT movement to complete the transaction

**Benefits:**
- ✅ Prevents overselling (stock reserved before payment)
- ✅ Handles payment failures gracefully
- ✅ Maintains accurate inventory
- ✅ Complete audit trail

### 5. Layered Architecture

```
┌─────────────────────────────────────┐
│         Controller Layer             │  - REST endpoints
│  (CheckoutController, etc.)          │  - Request/Response handling
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Service Layer                │  - Business logic
│  (Command implementations)           │  - Validation
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Repository Layer             │  - Data access
│  (ItemRepository, etc.)              │  - CRUD operations
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Database Layer               │  - MongoDB
│  (Items, Variants, Stock, etc.)     │  - Persistence
└─────────────────────────────────────┘
```

### 6. DTO Pattern

**Separation between entities and API contracts:**
- Entities represent database structure
- DTOs represent API contracts
- Mappers convert between layers
- Prevents exposing internal structure
- Allows independent evolution

### 7. Audit Trail with Stock Movements

Every stock change is recorded:
- **IN** - Stock added to warehouse
- **OUT** - Stock sold/shipped
- **RESERVATION** - Stock reserved for pending payment

**Fields tracked:**
- Movement type
- Quantity changed
- Previous and new quantities
- Reference number (order/checkout ID)
- Timestamp
- User who performed action
- Related movement (for OUT linking back to RESERVATION)

## Assumptions

### Business Logic Assumptions

1. **Price Validation**
   - Payment amount must be greater than or equal to checkout total price
   - Overpayment is allowed (customer might add tip or choose higher payment option)

2. **Stock Reservation**
   - Reserved stock is held indefinitely until payment processed
   - No automatic expiration/timeout for reservations (would be added in production)
   - Reserved stock is not available for new checkouts

3. **Variant Management**
   - Items can exist without variants (base item only)
   - Variants must belong to an active item
   - Each variant has a price adjustment (can be 0, positive, or negative)
   - Final price = Base price + Variant price adjustment

4. **Stock Management**
   - Stock is tracked at item+variant level
   - Items without variants have stock with null variantId
   - Reserved quantity is always ≤ total quantity
   - Available quantity = quantity - reservedQuantity

5. **Concurrent Operations**
   - Multiple users can checkout simultaneously
   - Optimistic locking handles race conditions
   - Failed transactions are automatically retried (up to 5 times)
   - Last successful transaction wins

6. **Payment Processing**
   - Payment processing assumes success (no partial payments)
   - Payment validation is external (not implemented in this system)
   - Successful payments commit stock (reduce total quantity)

### Technical Assumptions

1. **Database**
   - MongoDB is available and running
   - Single database instance (no sharding/replication considerations)
   - No multi-database transactions needed

2. **Performance**
   - Expected concurrent users: Low to moderate
   - Stock conflicts: Occasional (optimistic locking suitable)
   - Read-heavy workload (more reads than writes)

3. **Data Integrity**
   - Application enforces referential integrity (MongoDB doesn't have foreign keys)
   - Cleanup of orphaned records is manual (no CASCADE delete)

4. **Error Handling**
   - Network failures are transient and retryable
   - Database is available (no circuit breaker implemented)
   - Invalid data returns 400 Bad Request
   - Not found resources return 404 Not Found
   - Conflicts return 409 Conflict

5. **Security**
   - Authentication/Authorization not implemented (would use Spring Security in production)
   - All endpoints are public
   - No rate limiting
   - No input sanitization (beyond validation)

6. **Monitoring**
   - Logging is sufficient for debugging
   - No distributed tracing (would add OpenTelemetry in production)
   - No metrics collection (would add Micrometer in production)

## API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### 1. Item Management

#### Get All Items
```bash
curl -X GET http://localhost:8080/api/v1/items
```

Response:
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "sku": "LAPTOP-001",
    "name": "Premium Laptop",
    "description": "High-performance laptop for professionals",
    "basePrice": 999.00,
    "isActive": true,
    "createdAt": 1699564800000,
    "updatedAt": 1699564800000
  }
]
```

#### Get Item by ID
```bash
curl -X GET http://localhost:8080/api/v1/items/{itemId}
```

### 2. Variant Management

#### Get Variants by Item
```bash
curl -X GET http://localhost:8080/api/v1/items/{itemId}/variants
```

Response:
```json
[
  {
    "id": "507f1f77bcf86cd799439012",
    "itemId": "507f1f77bcf86cd799439011",
    "variantSku": "LAPTOP-001-16-512",
    "variantName": "16GB RAM / 512GB SSD",
    "attributes": {
      "ram": "16GB",
      "storage": "512GB"
    },
    "priceAdjustment": 100.00,
    "finalPrice": 1099.00,
    "isActive": true
  }
]
```

### 3. Stock Management

#### Get Stock for Item
```bash
curl -X GET http://localhost:8080/api/v1/stock/item/{itemId}
```

Response:
```json
[
  {
    "id": "507f1f77bcf86cd799439013",
    "itemId": "507f1f77bcf86cd799439011",
    "variantId": "507f1f77bcf86cd799439012",
    "quantity": 50,
    "reservedQuantity": 5,
    "availableQuantity": 45,
    "warehouseLocation": "WH-A-01-01"
  }
]
```

#### Create Stock
```bash
curl -X POST http://localhost:8080/api/v1/stock \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "507f1f77bcf86cd799439011",
    "variantId": "507f1f77bcf86cd799439012",
    "quantity": 100,
    "warehouseLocation": "WH-A-01-01"
  }'
```

#### Add Stock (IN movement)
```bash
curl -X POST http://localhost:8080/api/v1/stock/{stockId}/in \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 50,
    "referenceNumber": "PO-2024-001",
    "createdBy": "warehouse_staff_001"
  }'
```

Response:
```json
{
  "id": "507f1f77bcf86cd799439014",
  "stockId": "507f1f77bcf86cd799439013",
  "movementType": "IN",
  "quantity": 50,
  "previousQuantity": 100,
  "newQuantity": 150,
  "referenceNumber": "PO-2024-001",
  "createdBy": "warehouse_staff_001",
  "createdAt": 1699564800000
}
```

#### Release Stock (OUT movement - Manual)
```bash
curl -X POST http://localhost:8080/api/v1/stock/{stockId}/out \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 10,
    "referenceNumber": "MANUAL-OUT-001",
    "createdBy": "warehouse_staff_001"
  }'
```

#### Reserve Stock (RESERVATION movement - Manual)
```bash
curl -X POST http://localhost:8080/api/v1/stock/{stockId}/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5,
    "referenceNumber": "MANUAL-RES-001",
    "createdBy": "system"
  }'
```

#### Release Reserved Stock (RELEASE movement - Manual)
```bash
curl -X POST http://localhost:8080/api/v1/stock/{stockId}/release \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5,
    "referenceNumber": "MANUAL-REL-001",
    "createdBy": "system"
  }'
```

### 4. Checkout Flow

#### Checkout (Reserve Stock) - With Stock
```bash
curl -X POST http://localhost:8080/api/v1/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "507f1f77bcf86cd799439011",
    "variantId": "507f1f77bcf86cd799439012",
    "quantity": 2,
    "customerId": "CUST-001",
    "checkoutReference": "ORDER-2024-001"
  }'
```

Response (Success):
```json
{
  "checkoutId": "507f1f77bcf86cd799439015",
  "itemId": "507f1f77bcf86cd799439011",
  "variantId": "507f1f77bcf86cd799439012",
  "quantity": 2,
  "pricePerUnit": 1099.00,
  "totalPrice": 2198.00,
  "status": "PENDING",
  "customerId": "CUST-001",
  "checkoutReference": "ORDER-2024-001",
  "reservationId": "507f1f77bcf86cd799439016",
  "message": "Stock reserved successfully. Proceed with payment.",
  "createdAt": 1699564800000
}
```

#### Checkout - Out of Stock
```bash
curl -X POST http://localhost:8080/api/v1/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "507f1f77bcf86cd799439011",
    "variantId": "507f1f77bcf86cd799439012",
    "quantity": 1000,
    "customerId": "CUST-002",
    "checkoutReference": "ORDER-2024-002"
  }'
```

Response (Error 409):
```json
{
  "timestamp": "2024-11-12T10:30:00.000+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Insufficient stock. Available: 45, Requested: 1000",
  "path": "/api/v1/checkout"
}
```

### 5. Payment Processing

#### Process Payment (Commit Stock)
```bash
curl -X POST http://localhost:8080/api/v1/checkout/{checkoutId}/payment \
  -H "Content-Type: application/json" \
  -d '{
    "paymentAmount": 2198.00,
    "paymentReference": "PAY-2024-001",
    "processedBy": "payment_gateway"
  }'
```

Response:
```json
{
  "checkoutId": "507f1f77bcf86cd799439015",
  "status": "COMPLETED",
  "totalPrice": 2198.00,
  "paidAmount": 2198.00,
  "paymentReference": "PAY-2024-001",
  "message": "Payment successful. Stock committed.",
  "processedAt": 1699564800000
}
```

#### Payment - Invalid Amount (Less than Total)
```bash
curl -X POST http://localhost:8080/api/v1/checkout/{checkoutId}/payment \
  -H "Content-Type: application/json" \
  -d '{
    "paymentAmount": 1000.00,
    "paymentReference": "PAY-2024-003",
    "processedBy": "payment_gateway"
  }'
```

Response (Error 400):
```json
{
  "timestamp": "2024-11-12T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Payment amount 1000.00 is less than required amount 2198.00",
  "path": "/api/v1/checkout/{checkoutId}/payment"
}
```

### 6. Complete E-Commerce Flow Example

```bash
# Step 1: Browse items
curl -X GET http://localhost:8080/api/v1/items

# Step 2: Get item details with variants
curl -X GET http://localhost:8080/api/v1/items/507f1f77bcf86cd799439011

# Step 3: Check stock availability
curl -X GET http://localhost:8080/api/v1/stock/item/507f1f77bcf86cd799439011

# Step 4: Checkout (reserve stock)
curl -X POST http://localhost:8080/api/v1/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "507f1f77bcf86cd799439011",
    "variantId": "507f1f77bcf86cd799439012",
    "quantity": 1,
    "customerId": "CUST-001",
    "checkoutReference": "ORDER-2024-001"
  }'

# Step 5: Process payment (commit stock)
curl -X POST http://localhost:8080/api/v1/checkout/507f1f77bcf86cd799439015/payment \
  -H "Content-Type: application/json" \
  -d '{
    "paymentAmount": 1099.00,
    "paymentReference": "PAY-2024-001",
    "processedBy": "stripe"
  }'

# Step 6: Verify stock was deducted
curl -X GET http://localhost:8080/api/v1/stock/item/507f1f77bcf86cd799439011
```

## Testing

### Run All Tests
```bash
# Windows
.\mvnw.cmd test

# Linux/Mac
./mvnw test
```

### Run Specific Test Suite
```bash
# Checkout and Payment tests
.\mvnw.cmd test -Dtest="CheckoutPayment*"

# Stock concurrency tests
.\mvnw.cmd test -Dtest="StockConcurrency*"

# Integration tests only
.\mvnw.cmd test -Dtest="*IntegrationTest"
```

### Test Coverage

- **Unit Tests** - Individual command logic
- **Integration Tests** - Full workflow with MongoDB
- **Concurrency Tests** - Multiple simultaneous transactions
- **Edge Cases** - Out of stock, invalid data, payment failures

**Test Results:**
```
✅ ProcessCheckoutCommandTest
✅ ProcessPaymentCommandTest
✅ CheckoutPaymentIntegrationTest (5 tests)
✅ CheckoutPaymentConcurrencyTest (4 tests)
✅ StockConcurrencyTest (4 tests)
```

## Project Structure

```
cpwarehouse/
├── src/
│   ├── main/
│   │   ├── java/io/github/edmaputra/cpwarehouse/
│   │   │   ├── common/              # CommandExecutor
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── domain/
│   │   │   │   └── entity/          # MongoDB documents
│   │   │   ├── dto/
│   │   │   │   ├── request/         # API request DTOs
│   │   │   │   └── response/        # API response DTOs
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── mapper/              # Entity-DTO mappers
│   │   │   ├── migration/           # Data seeding
│   │   │   ├── repository/          # Spring Data repositories
│   │   │   └── service/             # Business logic (Commands)
│   │   │       ├── checkout/        # Checkout commands
│   │   │       ├── item/            # Item commands
│   │   │       ├── stock/           # Stock commands
│   │   │       └── variant/         # Variant commands
│   │   └── resources/
│   │       ├── application.yaml     # Application config
│   │       └── db/init/             # MongoDB init scripts
│   └── test/
│       └── java/io/github/edmaputra/cpwarehouse/
│           └── integration/         # Integration tests
├── docker-compose.yml               # MongoDB setup
├── pom.xml                          # Maven dependencies
└── README.md                        # This file
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is for educational purposes.

---

**Built with ❤️ using Spring Boot and MongoDB**

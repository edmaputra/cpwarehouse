# CP Warehouse -  Stock Management System

A robust warehouse management system designed for e-commerce platforms, featuring stock management, concurrent transaction handling with optimistic locking.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Manual Setup](#manual-setup)
  - [Automated Setup](#automated-setup)
- [Assumptions](#assumptions)
- [Design Decisions](#design-decisions)
- [API Documentation](#api-documentation)
- [Testing](#testing)

## Features

✅ **Item & Variant Management** - Manage products with multiple variants (e.g., size, color, configuration)  
✅ **Stock Management** - Track inventory with reservation and release capabilities  
✅ **Audit Trail** - Track all stock movements (IN, OUT, RESERVATION, RELEASE)  
✅ **Concurrent Operations** - Handle multiple simultaneous transactions safely with optimistic locking

### Support for Demonstration
✅ **Checkout Flow** - Reserve stock before payment to prevent overselling  
✅ **Payment Processing** - Validate payment and commit/release stock accordingly

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

#### 2. Run the Application

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
#./run.sh
```

### Testing
#### 1. Start mongodb for Test
**Using Docker:**
```bash
docker-compose -f docker-compose.test.yml up
```

**Using Podman:**
```bash
podman-compose -f docker-compose.test.yml up
```
#### 2. Run the test
Run from your IDE, or execute the following command:
```bash
# Windows
.\mvnw.cmd test

# Linux/Mac
./mvnw test
```
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

6. **Payment Processing**
   - Payment processing assumes success (no partial payments)
   - Payment validation is external (not implemented in this system)
   - Successful payments commit stock (reduce total quantity)

## Design

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

### 5. Audit Trail with Stock Movements

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


## API Documentation

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

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

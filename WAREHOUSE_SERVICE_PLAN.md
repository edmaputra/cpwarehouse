# Warehouse Service - Implementation Plan

## Project Overview
A warehouse management system built with Spring Boot 3.5.7, Java 21, PostgreSQL, and Liquibase for tracking items, variants, pricing, and stock levels.

---

## 1. Database Schema Design

### 1.1 Tables Structure

#### **items**
Stores the base item information.

```sql
CREATE TABLE items (
    id              BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(100) UNIQUE NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    base_price      DECIMAL(19, 2) NOT NULL,
    is_active       BOOLEAN DEFAULT true,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_base_price_positive CHECK (base_price >= 0)
);

CREATE INDEX idx_items_sku ON items(sku);
CREATE INDEX idx_items_is_active ON items(is_active);
```

#### **variants**
Stores item variants (sizes, colors, etc.).

```sql
CREATE TABLE variants (
    id              BIGSERIAL PRIMARY KEY,
    item_id         BIGINT NOT NULL,
    variant_sku     VARCHAR(100) UNIQUE NOT NULL,
    variant_name    VARCHAR(255) NOT NULL,
    attributes      JSONB,  -- For flexible attributes like {"size": "L", "color": "Red"}
    price_adjustment DECIMAL(19, 2) DEFAULT 0,
    is_active       BOOLEAN DEFAULT true,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_variant_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT chk_price_adjustment CHECK (price_adjustment >= -base_price)
);

CREATE INDEX idx_variants_item_id ON variants(item_id);
CREATE INDEX idx_variants_variant_sku ON variants(variant_sku);
CREATE INDEX idx_variants_is_active ON variants(is_active);
CREATE INDEX idx_variants_attributes ON variants USING GIN(attributes);
```

**Note**: Final price for variant = item.base_price + variant.price_adjustment

#### **stock**
Tracks inventory levels for items and their variants.

```sql
CREATE TABLE stock (
    id              BIGSERIAL PRIMARY KEY,
    item_id         BIGINT NOT NULL,
    variant_id      BIGINT,  -- NULL means stock for item without variant
    quantity        INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,  -- For pending orders
    warehouse_location VARCHAR(100),
    last_restocked_at TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_variant FOREIGN KEY (variant_id) REFERENCES variants(id) ON DELETE CASCADE,
    CONSTRAINT chk_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT chk_reserved_non_negative CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_reserved_not_exceed_quantity CHECK (reserved_quantity <= quantity),
    CONSTRAINT uq_stock_item_variant UNIQUE (item_id, variant_id)
);

CREATE INDEX idx_stock_item_id ON stock(item_id);
CREATE INDEX idx_stock_variant_id ON stock(variant_id);
CREATE INDEX idx_stock_quantity ON stock(quantity) WHERE quantity > 0;
```

**Available quantity** = quantity - reserved_quantity

#### **stock_movements**
Audit trail for stock changes.

```sql
CREATE TABLE stock_movements (
    id              BIGSERIAL PRIMARY KEY,
    stock_id        BIGINT NOT NULL,
    movement_type   VARCHAR(50) NOT NULL,  -- IN, OUT, ADJUSTMENT, RESERVATION, RELEASE
    quantity        INTEGER NOT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity    INTEGER NOT NULL,
    reference_number VARCHAR(100),
    notes           TEXT,
    created_by      VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movement_stock FOREIGN KEY (stock_id) REFERENCES stock(id) ON DELETE CASCADE
);

CREATE INDEX idx_stock_movements_stock_id ON stock_movements(stock_id);
CREATE INDEX idx_stock_movements_type ON stock_movements(movement_type);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at);
```

### 1.2 Database Relationships

```
items (1) ----< (N) variants
items (1) ----< (N) stock
variants (1) ----< (N) stock
stock (1) ----< (N) stock_movements
```

---

## 2. Domain Model Design

### 2.1 Entity Classes

```
io.github.edmaputra.cpwarehouse.domain.entity
├── Item
├── Variant
├── Stock
└── StockMovement
```

**Key Design Decisions**:
- Use Lombok for boilerplate reduction (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- Use JPA annotations (@Entity, @Table, @Id, @GeneratedValue)
- Implement soft delete using `is_active` flag where applicable
- Add @PreUpdate annotation for automatic `updated_at` timestamp updates
- Use @JsonIgnore for bidirectional relationships to avoid serialization issues

### 2.2 DTOs (Data Transfer Objects)

```
io.github.edmaputra.cpwarehouse.dto.request
├── ItemCreateRequest
├── ItemUpdateRequest
├── VariantCreateRequest
├── VariantUpdateRequest
├── StockUpdateRequest
└── StockReservationRequest

io.github.edmaputra.cpwarehouse.dto.response
├── ItemResponse
├── ItemDetailResponse
├── VariantResponse
├── StockResponse
├── StockAvailabilityResponse
└── ApiResponse<T>
```

---

## 3. API Endpoint Design

### 3.1 Items API

#### `POST /api/v1/items`
Create a new item.

**Request Body**:
```json
{
  "sku": "ITEM-001",
  "name": "Basic T-Shirt",
  "description": "Cotton T-Shirt",
  "basePrice": 19.99
}
```

**Response**: `201 Created` with `ItemResponse`

---

#### `GET /api/v1/items`
Get all items (with pagination).

**Query Parameters**:
- `page` (default: 0)
- `size` (default: 20)
- `isActive` (optional, filter by active status)
- `search` (optional, search in name/sku)

**Response**: `200 OK` with paginated `ItemResponse` list

---

#### `GET /api/v1/items/{id}`
Get item by ID with variants and stock information.

**Response**: `200 OK` with `ItemDetailResponse`

---

#### `PUT /api/v1/items/{id}`
Update an item.

**Request Body**:
```json
{
  "name": "Premium T-Shirt",
  "description": "Premium Cotton T-Shirt",
  "basePrice": 29.99,
  "isActive": true
}
```

**Response**: `200 OK` with updated `ItemResponse`

---

#### `DELETE /api/v1/items/{id}`
Soft delete an item (set `is_active` to false).

**Response**: `204 No Content`

---

### 3.2 Variants API

#### `POST /api/v1/items/{itemId}/variants`
Create a variant for an item.

**Request Body**:
```json
{
  "variantSku": "ITEM-001-L-RED",
  "variantName": "Large Red",
  "attributes": {
    "size": "L",
    "color": "Red"
  },
  "priceAdjustment": 5.00
}
```

**Response**: `201 Created` with `VariantResponse`

---

#### `GET /api/v1/items/{itemId}/variants`
Get all variants for an item.

**Response**: `200 OK` with `VariantResponse` list

---

#### `GET /api/v1/variants/{id}`
Get variant by ID.

**Response**: `200 OK` with `VariantResponse`

---

#### `PUT /api/v1/variants/{id}`
Update a variant.

**Request Body**:
```json
{
  "variantName": "Extra Large Red",
  "attributes": {
    "size": "XL",
    "color": "Red"
  },
  "priceAdjustment": 7.50,
  "isActive": true
}
```

**Response**: `200 OK` with updated `VariantResponse`

---

#### `DELETE /api/v1/variants/{id}`
Soft delete a variant.

**Response**: `204 No Content`

---

### 3.3 Stock API

#### `POST /api/v1/stock`
Create or initialize stock record.

**Request Body**:
```json
{
  "itemId": 1,
  "variantId": 2,  // optional
  "quantity": 100,
  "warehouseLocation": "A-01-05"
}
```

**Response**: `201 Created` with `StockResponse`

---

#### `PUT /api/v1/stock/{id}/adjust`
Adjust stock quantity (IN/OUT/ADJUSTMENT).

**Request Body**:
```json
{
  "movementType": "IN",
  "quantity": 50,
  "referenceNumber": "PO-2024-001",
  "notes": "Restock from supplier",
  "createdBy": "admin"
}
```

**Response**: `200 OK` with updated `StockResponse`

---

#### `POST /api/v1/stock/{id}/reserve`
Reserve stock for an order.

**Request Body**:
```json
{
  "quantity": 5,
  "referenceNumber": "ORDER-2024-001",
  "createdBy": "sales-system"
}
```

**Response**: `200 OK` with updated `StockResponse`

---

#### `POST /api/v1/stock/{id}/release`
Release reserved stock (cancel order or complete order).

**Request Body**:
```json
{
  "quantity": 5,
  "movementType": "RELEASE",  // or "OUT" for completed orders
  "referenceNumber": "ORDER-2024-001",
  "createdBy": "sales-system"
}
```

**Response**: `200 OK` with updated `StockResponse`

---

#### `GET /api/v1/stock/item/{itemId}`
Get stock information for an item (including all variants).

**Response**: `200 OK` with `StockResponse` list

---

#### `GET /api/v1/stock/variant/{variantId}`
Get stock information for a specific variant.

**Response**: `200 OK` with `StockResponse`

---

#### `GET /api/v1/stock/{id}/availability`
Check stock availability.

**Response**: `200 OK`
```json
{
  "stockId": 1,
  "quantity": 100,
  "reservedQuantity": 20,
  "availableQuantity": 80,
  "isAvailable": true
}
```

---

#### `GET /api/v1/stock/{id}/movements`
Get stock movement history.

**Query Parameters**:
- `page` (default: 0)
- `size` (default: 50)
- `movementType` (optional filter)

**Response**: `200 OK` with paginated stock movements

---

## 4. Validation Rules

### 4.1 Item Validation

| Field | Validation Rules |
|-------|-----------------|
| sku | - Required<br>- Unique<br>- Max 100 characters<br>- Pattern: `^[A-Z0-9-]+$` |
| name | - Required<br>- Max 255 characters<br>- Min 3 characters |
| description | - Optional<br>- Max 2000 characters |
| basePrice | - Required<br>- Must be >= 0<br>- Max 2 decimal places<br>- Max value: 9999999999.99 |

### 4.2 Variant Validation

| Field | Validation Rules |
|-------|-----------------|
| variantSku | - Required<br>- Unique<br>- Max 100 characters<br>- Pattern: `^[A-Z0-9-]+$` |
| variantName | - Required<br>- Max 255 characters<br>- Min 3 characters |
| attributes | - Optional<br>- Valid JSON object |
| priceAdjustment | - Optional (default: 0)<br>- Can be negative but final price must be >= 0<br>- Max 2 decimal places |
| itemId | - Required<br>- Must reference existing active item |

### 4.3 Stock Validation

| Field | Validation Rules |
|-------|-----------------|
| itemId | - Required<br>- Must reference existing item |
| variantId | - Optional<br>- If provided, must reference existing variant of the item |
| quantity | - Required<br>- Must be >= 0<br>- Max value: 2147483647 |
| reservedQuantity | - Auto-managed<br>- Must be >= 0<br>- Must be <= quantity |
| warehouseLocation | - Optional<br>- Max 100 characters |

### 4.4 Stock Movement Validation

| Field | Validation Rules |
|-------|-----------------|
| movementType | - Required<br>- Enum: IN, OUT, ADJUSTMENT, RESERVATION, RELEASE |
| quantity | - Required<br>- Must be > 0 |
| referenceNumber | - Optional<br>- Max 100 characters |
| notes | - Optional<br>- Max 2000 characters |

### 4.5 Business Rules

1. **Stock Reservation**:
   - Can only reserve up to available quantity (quantity - reserved_quantity)
   - Cannot sell/reserve more than available stock

2. **Stock Out**:
   - For completed orders, use movement type "OUT"
   - Decreases both reserved_quantity and quantity

3. **Price Calculation**:
   - Variant final price = item.basePrice + variant.priceAdjustment
   - Must always be >= 0

4. **Unique Constraints**:
   - Only one stock record per item-variant combination
   - If variant_id is NULL, represents stock for base item without variant

---

## 5. Layer Architecture

```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (PostgreSQL)
```

### 5.1 Package Structure

```
io.github.edmaputra.cpwarehouse
├── controller
│   ├── ItemController
│   ├── VariantController
│   └── StockController
├── service
│   ├── ItemService
│   ├── VariantService
│   └── StockService
├── repository
│   ├── ItemRepository
│   ├── VariantRepository
│   ├── StockRepository
│   └── StockMovementRepository
├── domain
│   └── entity
│       ├── Item
│       ├── Variant
│       ├── Stock
│       └── StockMovement
├── dto
│   ├── request
│   │   ├── ItemCreateRequest
│   │   ├── ItemUpdateRequest
│   │   ├── VariantCreateRequest
│   │   ├── VariantUpdateRequest
│   │   ├── StockUpdateRequest
│   │   └── StockReservationRequest
│   └── response
│       ├── ItemResponse
│       ├── ItemDetailResponse
│       ├── VariantResponse
│       ├── StockResponse
│       ├── StockAvailabilityResponse
│       └── ApiResponse
├── exception
│   ├── ResourceNotFoundException
│   ├── InsufficientStockException
│   ├── InvalidOperationException
│   └── GlobalExceptionHandler
├── mapper
│   ├── ItemMapper
│   ├── VariantMapper
│   └── StockMapper
└── config
    ├── DatabaseConfig
    └── WebConfig
```

---

## 6. Exception Handling

### 6.1 Custom Exceptions

| Exception | HTTP Status | Use Case |
|-----------|-------------|----------|
| ResourceNotFoundException | 404 | Item/Variant/Stock not found |
| InsufficientStockException | 409 | Attempting to reserve/sell more than available |
| InvalidOperationException | 400 | Invalid business operation |
| DuplicateResourceException | 409 | Duplicate SKU |
| ValidationException | 400 | Request validation failure |

### 6.2 Error Response Format

```json
{
  "timestamp": "2025-11-11T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Item with ID 123 not found",
  "path": "/api/v1/items/123"
}
```

---

## 7. Implementation Phases

### Phase 1: Database Setup
1. Add Spring Data JPA dependency to pom.xml
2. Configure PostgreSQL connection in application.yaml
3. Create Liquibase changelog files:
   - `db/changelog/changelog-001-create-items-table.yaml`
   - `db/changelog/changelog-002-create-variants-table.yaml`
   - `db/changelog/changelog-003-create-stock-table.yaml`
   - `db/changelog/changelog-004-create-stock-movements-table.yaml`
   - `db/changelog/db.changelog-master.yaml` (master file)
4. Test database migrations

### Phase 2: Domain Model
1. Create entity classes (Item, Variant, Stock, StockMovement)
2. Create repository interfaces
3. Write unit tests for entities

### Phase 3: DTOs and Mappers
1. Create request DTOs with validation annotations
2. Create response DTOs
3. Implement MapStruct or manual mappers
4. Add validation annotations (@Valid, @NotNull, @Pattern, etc.)

### Phase 4: Service Layer - Items
1. Implement ItemService with CRUD operations
2. Add business logic and validations
3. Write unit tests with Mockito
4. Write integration tests

### Phase 5: Service Layer - Variants
1. Implement VariantService with CRUD operations
2. Add business logic for variant-item relationship
3. Write unit tests
4. Write integration tests

### Phase 6: Service Layer - Stock
1. Implement StockService with:
   - Stock creation/update
   - Stock reservation logic
   - Stock movement tracking
   - Availability checks
2. Implement transactional operations with @Transactional
3. Add pessimistic locking for concurrent stock updates
4. Write comprehensive unit tests
5. Write integration tests for concurrent scenarios

### Phase 7: REST Controllers
1. Implement ItemController
2. Implement VariantController
3. Implement StockController
4. Add @RestControllerAdvice for global exception handling
5. Add request validation
6. Write controller tests (MockMvc)

### Phase 8: Exception Handling
1. Create custom exception classes
2. Implement GlobalExceptionHandler
3. Test error scenarios

### Phase 9: Testing & Documentation
1. Integration tests for complete flows
2. API documentation (OpenAPI/Swagger)
3. Add README with API usage examples
4. Performance testing for stock operations

### Phase 10: Additional Features (Optional)
1. Add pagination and sorting
2. Add filtering and search capabilities
3. Add API versioning
4. Add rate limiting
5. Add caching for frequently accessed data

---

## 8. Required Dependencies

Add to `pom.xml`:

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- MapStruct for DTO mapping -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>

<!-- OpenAPI/Swagger Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

---

## 9. Configuration Checklist

### 9.1 application.yaml

```yaml
spring:
  application:
    name: cpwarehouse
  
  datasource:
    url: jdbc:postgresql://localhost:5432/cpwarehouse_db
    username: ${DB_USERNAME:cpwarehouse}
    password: ${DB_PASSWORD:cpwarehouse}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # Let Liquibase handle schema
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
    drop-first: false

# Pagination defaults
application:
  pagination:
    default-page-size: 20
    max-page-size: 100

# API Documentation
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method

logging:
  level:
    io.github.edmaputra.cpwarehouse: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 9.2 Environment-Specific Configs

- `application-dev.yaml` - Development settings
- `application-test.yaml` - Test settings with H2 in-memory DB
- `application-prod.yaml` - Production settings

---

## 10. Testing Strategy

### 10.1 Unit Tests
- Test service layer business logic
- Mock repository dependencies
- Test edge cases and error scenarios
- Target: 80%+ code coverage

### 10.2 Integration Tests
- Test complete flows with real database (TestContainers)
- Test concurrent stock operations
- Test transactional behavior
- Test Liquibase migrations

### 10.3 API Tests
- Test REST endpoints with MockMvc
- Test request validation
- Test error responses
- Test pagination and filtering

### 10.4 Test Data
- Use Liquibase for test data seeding
- Create fixtures for common test scenarios

---

## 11. Security Considerations (Future)

While not required initially, consider these for production:

1. **Authentication & Authorization**: Spring Security with JWT
2. **Rate Limiting**: Prevent API abuse
3. **Input Sanitization**: Prevent SQL injection (JPA handles this)
4. **Audit Logging**: Track who made changes and when
5. **HTTPS**: Enforce encrypted connections

---

## 12. Performance Optimization

1. **Database Indexes**: Already defined in schema
2. **Connection Pooling**: Configure HikariCP
3. **Caching**: Redis for frequently accessed items
4. **Batch Operations**: Use JPA batch inserts/updates
5. **Pessimistic Locking**: For stock updates to prevent race conditions
6. **Query Optimization**: Use fetch joins to avoid N+1 queries

---

## 13. Monitoring & Observability (Future)

1. **Actuator Endpoints**: Health checks, metrics
2. **Logging**: Structured logging with correlation IDs
3. **Metrics**: Track API response times, stock operations
4. **Alerts**: Low stock notifications

---

## 14. API Response Standards

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully"
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "totalPages": 5,
      "totalElements": 95
    }
  }
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "Cannot reserve 10 units. Only 5 available.",
    "details": {
      "requested": 10,
      "available": 5
    }
  },
  "timestamp": "2025-11-11T10:30:00Z",
  "path": "/api/v1/stock/1/reserve"
}
```

---

## 15. Next Steps

After approval of this plan:

1. Review and confirm database schema
2. Review and confirm API endpoints
3. Review validation rules
4. Begin Phase 1: Database Setup
5. Implement incrementally following the phases
6. Test each phase before moving to the next

---

## 16. Assumptions

1. PostgreSQL database is available and accessible
2. Using Spring Boot 3.5.7 with Java 21
3. RESTful API follows JSON format
4. Stock operations require immediate consistency (not eventual)
5. Single warehouse initially (can be extended)
6. Prices in single currency (can be extended)
7. No authentication/authorization initially
8. Basic error handling sufficient for MVP

---

## 17. Out of Scope (For Now)

1. User authentication and authorization
2. Multi-warehouse support
3. Multi-currency support
4. Order management system integration
5. Barcode/QR code generation
6. Email notifications
7. Advanced reporting and analytics
8. Bulk import/export functionality
9. Image upload for items
10. Shopping cart functionality

These can be added in future iterations based on requirements.

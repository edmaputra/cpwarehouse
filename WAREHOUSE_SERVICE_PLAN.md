# Warehouse Service - Implementation Plan

## Project Overview
A warehouse management system built with Spring Boot 3.5.7, Java 21, and MongoDB for tracking items, variants, pricing, and stock levels.

---

## 1. Database Schema Design

### 1.1 Collections Structure

#### **items**
Stores the base item information.

```json
{
  "_id": "ObjectId",
  "sku": "ITEM-001",
  "name": "Basic T-Shirt",
  "description": "Cotton T-Shirt",
  "basePrice": 19.99,
  "isActive": true,
  "createdAt": 1699704000000,
  "updatedAt": 1699704000000
}
```

**Note**: `createdAt` and `updatedAt` are stored as Long (Epoch timestamp in milliseconds)

**Indexes:**
```javascript
db.items.createIndex({ "sku": 1 }, { unique: true })
db.items.createIndex({ "isActive": 1 })
db.items.createIndex({ "name": "text", "description": "text" })
```

**Validation Rules:**
```javascript
db.createCollection("items", {
  validator: {
    $jsonSchema: {
      required: ["sku", "name", "basePrice"],
      properties: {
        sku: { bsonType: "string", maxLength: 100 },
        name: { bsonType: "string", maxLength: 255, minLength: 3 },
        description: { bsonType: "string", maxLength: 2000 },
        basePrice: { bsonType: "decimal", minimum: 0 },
        isActive: { bsonType: "bool" }
      }
    }
  }
})
```

#### **variants**
Stores item variants (sizes, colors, etc.).

```json
{
  "_id": "ObjectId",
  "itemId": "ObjectId (reference to items)",
  "variantSku": "ITEM-001-L-RED",
  "variantName": "Large Red",
  "attributes": {
    "size": "L",
    "color": "Red"
  },
  "priceAdjustment": 5.00,
  "isActive": true,
  "createdAt": 1699704000000,
  "updatedAt": 1699704000000
}
```

**Note**: `createdAt` and `updatedAt` are stored as Long (Epoch timestamp in milliseconds)

**Indexes:**
```javascript
db.variants.createIndex({ "itemId": 1 })
db.variants.createIndex({ "variantSku": 1 }, { unique: true })
db.variants.createIndex({ "isActive": 1 })
db.variants.createIndex({ "attributes": 1 })
```

**Note**: Final price for variant = item.basePrice + variant.priceAdjustment

#### **stock**
Tracks inventory levels for items and their variants.

```json
{
  "_id": "ObjectId",
  "itemId": "ObjectId (reference to items)",
  "variantId": "ObjectId (reference to variants, null if no variant)",
  "quantity": 100,
  "reservedQuantity": 20,
  "warehouseLocation": "A-01-05",
  "lastRestockedAt": 1699704000000,
  "createdAt": 1699704000000,
  "updatedAt": 1699704000000
}
```

**Note**: `lastRestockedAt`, `createdAt`, and `updatedAt` are stored as Long (Epoch timestamp in milliseconds)

**Indexes:**
```javascript
db.stock.createIndex({ "itemId": 1, "variantId": 1 }, { unique: true })
db.stock.createIndex({ "itemId": 1 })
db.stock.createIndex({ "variantId": 1 })
db.stock.createIndex({ "quantity": 1 })
```

**Available quantity** = quantity - reservedQuantity

#### **stock_movements**
Audit trail for stock changes.

```json
{
  "_id": "ObjectId",
  "stockId": "ObjectId (reference to stock)",
  "movementType": "IN",
  "quantity": 50,
  "previousQuantity": 100,
  "newQuantity": 150,
  "referenceNumber": "PO-2024-001",
  "notes": "Restock from supplier",
  "createdBy": "admin",
  "createdAt": 1699704000000
}
```

**Note**: `createdAt` is stored as Long (Epoch timestamp in milliseconds)

**Indexes:**
```javascript
db.stock_movements.createIndex({ "stockId": 1 })
db.stock_movements.createIndex({ "movementType": 1 })
db.stock_movements.createIndex({ "createdAt": -1 })
db.stock_movements.createIndex({ "referenceNumber": 1 })
```

### 1.2 Database Relationships

```
items (1) ----< (N) variants (referenced by itemId)
items (1) ----< (N) stock (referenced by itemId)
variants (1) ----< (N) stock (referenced by variantId)
stock (1) ----< (N) stock_movements (referenced by stockId)
```

**Note**: MongoDB uses document references instead of foreign keys. Application-level referential integrity must be maintained.

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
- Use Spring Data MongoDB annotations (@Document, @Id, @Indexed, @DBRef)
- Use `String` with ObjectId for `_id` fields or use `ObjectId` type directly
- Implement soft delete using `isActive` flag where applicable
- Use `Long` type for timestamp fields (`createdAt`, `updatedAt`, `lastRestockedAt`) to store Epoch time in milliseconds
- Manually manage timestamps using `System.currentTimeMillis()` or use lifecycle callbacks
- Use `@DBRef` for document references (optional, can use manual reference with String IDs)
- Consider embedded documents vs references based on access patterns

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
  "timestamp": 1699704600000,
  "status": 404,
  "error": "Not Found",
  "message": "Item with ID 123 not found",
  "path": "/api/v1/items/123"
}
```

**Note**: `timestamp` is in Epoch milliseconds format

---

## 7. Implementation Phases

### Phase 1: Database Setup
1. Add Spring Data MongoDB dependency to pom.xml
2. Configure MongoDB connection in application.yaml
3. Create MongoDB initialization scripts (optional):
   - `db/init/001-create-indexes.js`
   - `db/init/002-create-validators.js`
   - `db/init/003-seed-data.js` (for development)
4. Test database connection and indexes
5. Configure MongoDB indexes programmatically using `@Indexed` annotations or `MongoTemplate`

### Phase 2: Domain Model
1. Create document classes with @Document annotation (Item, Variant, Stock, StockMovement)
2. Create repository interfaces extending MongoRepository
3. Add indexes using @Indexed annotations
4. Write unit tests for entities

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
2. Implement transactional operations with @Transactional (MongoDB 4.0+ with replica set)
3. Use optimistic locking with @Version annotation for concurrent stock updates
4. Implement retry logic for optimistic lock failures
5. Write comprehensive unit tests
6. Write integration tests for concurrent scenarios

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
1. Integration tests for complete flows using embedded MongoDB (Flapdoodle)
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
<!-- Spring Data MongoDB -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- MapStruct for DTO mapping (Optional) -->
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

<!-- Embedded MongoDB for Testing -->
<dependency>
    <groupId>de.flapdoodle.embed</groupId>
    <artifactId>de.flapdoodle.embed.mongo.spring30x</artifactId>
    <version>4.9.2</version>
    <scope>test</scope>
</dependency>
```

---

## 9. Configuration Checklist

### 9.1 application.yaml

```yaml
spring:
  application:
    name: cpwarehouse
  
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/cpwarehouse_db}
      # Alternative configuration:
      # host: ${MONGODB_HOST:localhost}
      # port: ${MONGODB_PORT:27017}
      # database: ${MONGODB_DATABASE:cpwarehouse_db}
      # username: ${MONGODB_USERNAME:}
      # password: ${MONGODB_PASSWORD:}
      # authentication-database: admin
      auto-index-creation: true  # Automatically create indexes from @Indexed annotations

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
    org.springframework.data.mongodb.core.MongoTemplate: DEBUG
```

### 9.2 Environment-Specific Configs

- `application-dev.yaml` - Development settings (local MongoDB)
- `application-test.yaml` - Test settings with embedded MongoDB (Flapdoodle)
- `application-prod.yaml` - Production settings (MongoDB Atlas or production cluster)

**Example application-test.yaml:**
```yaml
spring:
  data:
    mongodb:
      # Embedded MongoDB will be used automatically for tests with Flapdoodle dependency
      uri: mongodb://localhost:27017/cpwarehouse_test_db
```

**Example application-prod.yaml:**
```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}  # Use environment variable for production
      # For MongoDB Atlas:
      # uri: mongodb+srv://<username>:<password>@cluster.mongodb.net/cpwarehouse_db?retryWrites=true&w=majority
```

---

## 10. Testing Strategy

### 10.1 Unit Tests
- Test service layer business logic
- Mock repository dependencies
- Test edge cases and error scenarios
- Target: 80%+ code coverage

### 10.2 Integration Tests
- Test complete flows with embedded MongoDB (Flapdoodle)
- Test concurrent stock operations with optimistic locking
- Test transactional behavior (requires MongoDB replica set)
- Test index creation and queries

### 10.3 API Tests
- Test REST endpoints with MockMvc
- Test request validation
- Test error responses
- Test pagination and filtering

### 10.4 Test Data
- Use @BeforeEach methods to seed test data
- Create fixtures for common test scenarios
- Use test data builders or factories
- Clean up test data with @AfterEach or use embedded MongoDB per test class

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

1. **Database Indexes**: Use @Indexed annotations and compound indexes for frequently queried fields
2. **Connection Pooling**: Configure MongoDB connection pool settings
3. **Caching**: Redis for frequently accessed items
4. **Batch Operations**: Use MongoTemplate bulkOps for batch inserts/updates
5. **Optimistic Locking**: Use @Version annotation for stock updates to prevent race conditions
6. **Query Optimization**: 
   - Use projection to fetch only required fields
   - Use aggregation pipelines for complex queries
   - Avoid fetching large embedded arrays
7. **Sharding**: Consider sharding strategy for horizontal scaling (if needed)
8. **Read Preference**: Configure read preference for replica sets (primary, secondary, etc.)

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
  "timestamp": 1699704600000,
  "path": "/api/v1/stock/1/reserve"
}
```

**Note**: All timestamps in API responses use Epoch milliseconds format (Long)

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

1. MongoDB 4.0+ is available and accessible (4.0+ required for multi-document transactions)
2. Using Spring Boot 3.5.7 with Java 21
3. RESTful API follows JSON format
4. Stock operations require immediate consistency (not eventual)
5. Single warehouse initially (can be extended)
6. Prices in single currency (can be extended)
7. No authentication/authorization initially
8. Basic error handling sufficient for MVP
9. MongoDB replica set configuration for transactions (single node replica set acceptable for development)
10. Document size limits (16MB per document) are sufficient for our use cases

---

## 17. MongoDB-Specific Considerations

### 17.1 Transaction Support
- MongoDB transactions require a replica set (even single-node replica set for development)
- Use `@Transactional` for multi-document operations
- Consider compensating transactions for cross-collection operations
- Handle `TransientTransactionError` with retry logic

### 17.2 Schema Design Choices

**Document References vs Embedding:**
- **Items & Variants**: Use separate collections with references (easier to query and update)
- **Stock & Items/Variants**: Use references (normalized approach for flexibility)
- **Stock Movements**: Separate collection with reference to stock (audit trail)

**Alternative Embedded Approach** (for consideration):
- Could embed variants within items document for read-heavy workloads
- Could embed recent stock movements within stock document

### 17.3 Index Strategy
- Create compound indexes for common query patterns
- Use text indexes for search functionality
- Monitor index usage with `explain()` plans
- Avoid over-indexing (impacts write performance)

### 17.4 Data Migration from PostgreSQL
If migrating from existing PostgreSQL data:
1. Export data to JSON/CSV
2. Transform data structure (snake_case to camelCase, BIGSERIAL to ObjectId)
3. Import using mongoimport or custom migration scripts
4. Validate data integrity
5. Test application functionality

### 17.5 Backup Strategy
- Use mongodump/mongorestore for backups
- Consider MongoDB Atlas automated backups
- Point-in-time recovery for production

---

## 18. Out of Scope (For Now)

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
11. MongoDB Change Streams for real-time updates
12. Geospatial queries for warehouse locations
13. Time-series data for stock movement analytics

These can be added in future iterations based on requirements.

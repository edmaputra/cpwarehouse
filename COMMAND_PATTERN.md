# Command Pattern Implementation

## Overview

This project implements the **Command Pattern** for the business/service layer, providing better separation of concerns and adhering to the **Single Responsibility Principle (SRP)**.

Each business operation is encapsulated in its own command class, making the codebase more maintainable, testable, and extensible.

## Architecture

### Core Components

1. **`Command<R, T>` Interface** (`common/Command.java`)
   - Generic interface for all commands
   - `R`: Request type (DTO/POJO)
   - `T`: Response type (DTO/POJO)
   - Single method: `T execute(R request)`

2. **`CommandExecutor`** (`common/CommandExecutor.java`)
   - Centralized command execution service
   - Retrieves command beans from Spring ApplicationContext
   - Executes commands with type safety
   - Handles dependency injection for commands

3. **Service Implementation** (`service/ItemServiceImpl.java`)
   - Implements the service interface
   - Delegates all operations to commands via `CommandExecutor`
   - Thin orchestration layer

4. **Individual Commands** (`service/command/*.java`)
   - Each command handles one specific operation
   - Contains business logic for that operation
   - Can inject repositories, mappers, and other services

## Benefits

### ✅ Single Responsibility Principle
Each command class has one reason to change - when the specific business logic for that operation changes.

### ✅ Better Testability
- Commands can be tested independently
- Easy to mock individual commands
- No need to set up entire service with all dependencies

### ✅ Easier Maintenance
- Changes to one operation don't affect others
- Clear separation of concerns
- Easy to locate and modify specific business logic

### ✅ Extensibility
- Adding new operations is simple: create a new command
- No need to modify existing service class
- Open/Closed Principle compliance

### ✅ Reusability
- Commands can be reused in different contexts
- Can be composed to create complex operations

## Item Commands

### 1. ItemCreateCommand
- **Request**: `ItemCreateRequest`
- **Response**: `ItemResponse`
- **Purpose**: Create a new item
- **Business Logic**:
  - Validates SKU uniqueness
  - Maps DTO to entity
  - Sets initial timestamps
  - Saves to database

### 2. ItemGetAllCommand
- **Request**: `ItemGetAllCommand.Request` (pageable, activeOnly, search)
- **Response**: `Page<ItemResponse>`
- **Purpose**: Get paginated items with optional filtering
- **Business Logic**:
  - Supports search by name or SKU
  - Filters by active status
  - Returns paginated results

### 3. ItemGetByIdCommand
- **Request**: `String` (item ID)
- **Response**: `ItemDetailResponse`
- **Purpose**: Get detailed item information by ID
- **Business Logic**:
  - Finds item by ID
  - Returns detailed response
  - Throws exception if not found

### 4. ItemGetBySkuCommand
- **Request**: `String` (SKU)
- **Response**: `ItemResponse`
- **Purpose**: Get item by SKU
- **Business Logic**:
  - Finds item by SKU
  - Returns standard response
  - Throws exception if not found

### 5. ItemUpdateCommand
- **Request**: `ItemUpdateCommand.Request` (id, updateRequest)
- **Response**: `ItemResponse`
- **Purpose**: Update existing item
- **Business Logic**:
  - Finds existing item
  - Updates fields via MapStruct
  - Updates timestamp
  - Saves changes

### 6. ItemDeleteCommand
- **Request**: `String` (item ID)
- **Response**: `Void`
- **Purpose**: Soft delete item (set isActive = false)
- **Business Logic**:
  - Finds item
  - Sets isActive to false
  - Updates timestamp
  - Saves changes

### 7. ItemHardDeleteCommand
- **Request**: `String` (item ID)
- **Response**: `Void`
- **Purpose**: Permanently delete item
- **Business Logic**:
  - Checks if item exists
  - Deletes from database

## Usage Example

### In Service Layer

```java
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    
    private final CommandExecutor commandExecutor;
    
    @Override
    public ItemResponse createItem(ItemCreateRequest request) {
        return commandExecutor.execute(ItemCreateCommand.class, request);
    }
    
    @Override
    public Page<ItemResponse> getAllItems(Boolean isActive, String search, Pageable pageable) {
        ItemGetAllCommand.Request request = new ItemGetAllCommand.Request(pageable, isActive, search);
        return commandExecutor.execute(ItemGetAllCommand.class, request);
    }
}
```

### Creating a New Command

1. **Create the command class** in `service/command/`:

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class MyNewCommand implements Command<MyRequest, MyResponse> {
    
    private final MyRepository myRepository;
    
    @Override
    @Transactional
    public MyResponse execute(MyRequest request) {
        // Implement business logic here
        log.info("Executing my new command");
        // ...
        return response;
    }
}
```

2. **Use it in the service**:

```java
@Override
public MyResponse myNewOperation(MyRequest request) {
    return commandExecutor.execute(MyNewCommand.class, request);
}
```

## Testing

### Testing Individual Commands

```java
@ExtendWith(MockitoExtension.class)
class ItemCreateCommandTest {
    
    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private ItemMapper itemMapper;
    
    @InjectMocks
    private ItemCreateCommand command;
    
    @Test
    void shouldCreateItem() {
        // Given
        ItemCreateRequest request = new ItemCreateRequest("SKU-001", "Item Name", ...);
        
        // When
        ItemResponse response = command.execute(request);
        
        // Then
        assertThat(response).isNotNull();
        verify(itemRepository).save(any(Item.class));
    }
}
```

### Testing Service with Command Executor

```java
@SpringBootTest
class ItemServiceImplIntegrationTest {
    
    @Autowired
    private ItemService itemService;
    
    @Test
    void shouldCreateItemViaService() {
        ItemCreateRequest request = new ItemCreateRequest(...);
        ItemResponse response = itemService.createItem(request);
        assertThat(response).isNotNull();
    }
}
```

## Design Patterns Used

### 1. Command Pattern
Encapsulates a request as an object, allowing parameterization of clients with different requests.

### 2. Dependency Injection
Commands are Spring beans, managed by ApplicationContext with automatic dependency injection.

### 3. Strategy Pattern
Different commands represent different strategies for handling operations.

### 4. Template Method Pattern
The `Command<R, T>` interface defines the template for all commands.

## Migration Path

### From Traditional Service

**Before:**
```java
@Service
public class ItemService {
    // All business logic in one class
    // Multiple responsibilities
    // Hard to test individual operations
    
    public ItemResponse createItem(ItemCreateRequest request) {
        // 30 lines of business logic
    }
    
    public ItemResponse updateItem(String id, ItemUpdateRequest request) {
        // 40 lines of business logic
    }
    
    // ... 10 more methods
}
```

**After:**
```java
@Service
public class ItemServiceImpl implements ItemService {
    private final CommandExecutor commandExecutor;
    
    // Thin orchestration layer
    // Single responsibility: delegate to commands
    
    public ItemResponse createItem(ItemCreateRequest request) {
        return commandExecutor.execute(ItemCreateCommand.class, request);
    }
    
    public ItemResponse updateItem(String id, ItemUpdateRequest request) {
        return commandExecutor.execute(ItemUpdateCommand.class, 
            new ItemUpdateCommand.Request(id, request));
    }
}
```

## Best Practices

### ✅ DO

- Keep commands focused on a single operation
- Use meaningful command names (e.g., `ItemCreateCommand`)
- Inject only the dependencies needed for that command
- Use `@Transactional` at the command level if needed
- Document business logic within the command
- Create nested `Request` records for complex parameters

### ❌ DON'T

- Put multiple operations in one command
- Share mutable state between command instances
- Call other commands directly (use CommandExecutor)
- Put validation logic in commands (use DTOs with `@Valid`)

## Future Enhancements

### 1. Command Interceptors
Add cross-cutting concerns (logging, metrics, caching):

```java
public interface CommandInterceptor {
    <R, T> T intercept(Command<R, T> command, R request);
}
```

### 2. Async Commands
Support asynchronous command execution:

```java
public <R, T> CompletableFuture<T> executeAsync(
    Class<? extends Command<R, T>> commandClass, R request);
```

### 3. Command History/Audit
Track command execution for audit trails:

```java
@Aspect
public class CommandAuditAspect {
    @Around("@within(Command)")
    public Object audit(ProceedingJoinPoint joinPoint) { ... }
}
```

### 4. Retry Logic
Add automatic retry for failed commands:

```java
@Retryable(maxAttempts = 3)
public class ResilientCommand implements Command<R, T> { ... }
```

## Related Documentation

- [MapStruct Integration](MAPSTRUCT.md) - How commands use MapStruct mappers
- [Running Tests](RUNNING_TESTS.md) - Testing commands and services
- [Warehouse Service Plan](WAREHOUSE_SERVICE_PLAN.md) - Overall architecture

## Conclusion

The Command Pattern implementation provides a clean, maintainable, and extensible architecture for the business layer. Each operation is isolated in its own command, making the codebase easier to understand, test, and modify.

This pattern is particularly valuable as the application grows, preventing the service layer from becoming a monolithic "god class" with hundreds of lines of complex business logic.

# Command Executor Architecture

## Overview

This project implements a **Clean Architecture / CQRS pattern** using:
- **Command Pattern** with generic `Command<R,T>` interface
- **CommandExecutor** for centralized command execution
- **Single Responsibility Principle** - each command handles one use case
- **Dependency Inversion** - controller depends on abstractions (CommandExecutor + Command interfaces)

## Architecture Components

### 1. Command Interface (`Command<R,T>`)

Generic interface that all commands must implement:

```java
public interface Command<R, T> {
    /**
     * Execute the command with the given request.
     * @param request the request object of type R
     * @return the response of type T
     */
    T execute(R request);
}
```

**Generic Parameters:**
- `R` = Request type (input)
- `T` = Response type (output)

### 2. CommandExecutor Service

Centralized service for executing commands with type safety:

```java
@Service
@RequiredArgsConstructor
public class CommandExecutor {
    private final ApplicationContext applicationContext;
    
    /**
     * Execute a command by resolving its implementation from Spring context.
     * This allows runtime resolution of command implementations while maintaining type safety.
     */
    public <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request) {
        Command<R, T> command = applicationContext.getBean(commandClass);
        return command.execute(request);
    }
}
```

**Benefits:**
- Single point of command execution
- Spring resolves implementations at runtime
- Type-safe through generics
- Easy to test and mock
- Supports AOP and cross-cutting concerns

### 3. Command Interfaces

Each use case has its own command interface extending `Command<R,T>`:

#### Example: Create Item
```java
public interface CreateItemCommand extends Command<ItemCreateRequest, ItemResponse> {
    @Override
    ItemResponse execute(ItemCreateRequest request);
}
```

#### Example: Get Item By ID
```java
public interface GetItemByIdCommand extends Command<String, ItemDetailResponse> {
    @Override
    ItemDetailResponse execute(String id);
}
```

#### Example: Multi-Parameter Commands (using Request wrapper)
```java
public interface UpdateItemCommand extends Command<UpdateItemCommand.Request, ItemResponse> {
    @Override
    ItemResponse execute(Request request);
    
    record Request(String id, ItemUpdateRequest updateRequest) {}
}
```

**Pattern for multi-parameter commands:**
- Wrap parameters in a nested `record Request(...)`
- Maintains single parameter requirement of `Command<R,T>`
- Type-safe and immutable

### 4. Command Implementations

Concrete implementations annotated with `@Service`:

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateItemCommandImpl implements CreateItemCommand {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    
    @Override
    @Transactional
    public ItemResponse execute(ItemCreateRequest request) {
        // Business logic here
    }
}
```

**Naming Convention:** `{CommandName}Impl`

### 5. Controller Layer

Controllers inject only `CommandExecutor` and use it to execute commands:

```java
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {
    private final CommandExecutor commandExecutor;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(
            @Valid @RequestBody ItemCreateRequest request) {
        
        ItemResponse response = commandExecutor.execute(CreateItemCommand.class, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Item created successfully"));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable String id,
            @Valid @RequestBody ItemUpdateRequest request) {
        
        UpdateItemCommand.Request commandRequest = new UpdateItemCommand.Request(id, request);
        ItemResponse response = commandExecutor.execute(UpdateItemCommand.class, commandRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Item updated successfully"));
    }
}
```

**Controller Benefits:**
- Single dependency: `CommandExecutor`
- No direct coupling to command implementations
- Clean and testable
- Easy to add new commands without modifying controller's dependencies

## Command Catalog

### Item Commands

| Command | Request Type | Response Type | Use Case |
|---------|-------------|--------------|----------|
| `CreateItemCommand` | `ItemCreateRequest` | `ItemResponse` | Create new item |
| `GetAllItemsCommand` | `GetAllItemsCommand.Request` | `Page<ItemResponse>` | Get paginated items with filters |
| `GetItemByIdCommand` | `String` (id) | `ItemDetailResponse` | Get item details by ID |
| `GetItemBySkuCommand` | `String` (sku) | `ItemResponse` | Get item by SKU |
| `UpdateItemCommand` | `UpdateItemCommand.Request` | `ItemResponse` | Update existing item |
| `DeleteItemCommand` | `String` (id) | `Void` | Soft delete item |
| `HardDeleteItemCommand` | `String` (id) | `Void` | Permanently delete item |

## Design Patterns Applied

### 1. Command Pattern
- Encapsulates requests as objects
- Single responsibility per command
- Easy to add new commands

### 2. Dependency Inversion Principle
- High-level modules (Controller) depend on abstractions (CommandExecutor, Command interfaces)
- Low-level modules (Command implementations) depend on abstractions
- Both can vary independently

### 3. Single Responsibility Principle
- Each command handles exactly one use case
- Clear separation of concerns

### 4. Open/Closed Principle
- System is open for extension (add new commands)
- Closed for modification (no need to change existing code)

### 5. Repository Pattern
- Data access abstracted through repositories
- Commands use repositories for persistence

### 6. DTO Pattern
- Data transfer via request/response DTOs
- Separation between domain entities and API contracts

### 7. Mapper Pattern (MapStruct)
- Compile-time mapping between DTOs and entities
- Type-safe and performant

## Benefits of This Architecture

### ✅ Testability
- Commands can be tested in isolation
- Controller tests can mock `CommandExecutor`
- Easy to write unit and integration tests

### ✅ Maintainability
- Clear separation of concerns
- Each command in its own file
- Easy to understand and modify

### ✅ Scalability
- Add new commands without touching existing code
- Easy to add cross-cutting concerns (logging, security, caching)
- Supports future microservices split

### ✅ Type Safety
- Generic `Command<R,T>` provides compile-time type checking
- No runtime type casting
- IDE autocomplete support

### ✅ Clean Code
- Small, focused classes
- Clear naming conventions
- Self-documenting code structure

### ✅ SOLID Principles
- All 5 SOLID principles applied
- Professional enterprise architecture

## Testing Strategy

### Unit Testing Commands
```java
@ExtendWith(MockitoExtension.class)
class CreateItemCommandImplTest {
    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private ItemMapper itemMapper;
    
    @InjectMocks
    private CreateItemCommandImpl command;
    
    @Test
    void shouldCreateItem() {
        // Test command in isolation
    }
}
```

### Integration Testing Controller
```java
@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateItemViaApi() {
        // Test full stack including CommandExecutor
    }
}
```

## Future Enhancements

### 1. Command Events
Add event publishing after command execution:
```java
public <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request) {
    Command<R, T> command = applicationContext.getBean(commandClass);
    T result = command.execute(request);
    eventPublisher.publishEvent(new CommandExecutedEvent(commandClass, request, result));
    return result;
}
```

### 2. Command Validation
Add pre-execution validation:
```java
public <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request) {
    validator.validate(request);
    Command<R, T> command = applicationContext.getBean(commandClass);
    return command.execute(request);
}
```

### 3. Command Audit
Add audit logging:
```java
public <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request) {
    auditService.logCommandExecution(commandClass, request);
    Command<R, T> command = applicationContext.getBean(commandClass);
    T result = command.execute(request);
    auditService.logCommandResult(commandClass, result);
    return result;
}
```

### 4. Async Commands
Support asynchronous execution:
```java
public <R, T> CompletableFuture<T> executeAsync(Class<? extends Command<R, T>> commandClass, R request) {
    return CompletableFuture.supplyAsync(() -> execute(commandClass, request), executor);
}
```

## File Structure

```
src/main/java/
├── common/
│   ├── Command.java                    # Generic command interface
│   └── CommandExecutor.java            # Command execution service
├── controller/
│   └── ItemController.java             # REST endpoints using CommandExecutor
├── service/
│   └── command/
│       ├── CreateItemCommand.java      # Command interface
│       ├── CreateItemCommandImpl.java  # Command implementation
│       ├── GetAllItemsCommand.java
│       ├── GetAllItemsCommandImpl.java
│       ├── GetItemByIdCommand.java
│       ├── GetItemByIdCommandImpl.java
│       ├── GetItemBySkuCommand.java
│       ├── GetItemBySkuCommandImpl.java
│       ├── UpdateItemCommand.java
│       ├── UpdateItemCommandImpl.java
│       ├── DeleteItemCommand.java
│       ├── DeleteItemCommandImpl.java
│       ├── HardDeleteItemCommand.java
│       └── HardDeleteItemCommandImpl.java
├── domain/
│   └── entity/
│       └── Item.java                   # Domain entity
├── repository/
│   └── ItemRepository.java             # Data access
├── dto/
│   ├── request/
│   │   ├── ItemCreateRequest.java
│   │   └── ItemUpdateRequest.java
│   └── response/
│       ├── ItemResponse.java
│       └── ItemDetailResponse.java
└── mapper/
    └── ItemMapper.java                 # MapStruct mapper
```

## Conclusion

This architecture provides a **clean, maintainable, and scalable** foundation for the warehouse management system. It follows industry best practices and SOLID principles, making it easy to extend and maintain as the system grows.

The **CommandExecutor pattern** provides centralized control over command execution while maintaining type safety and allowing for future enhancements like event publishing, auditing, and async execution.

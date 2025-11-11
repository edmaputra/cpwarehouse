# MapStruct Integration

This project uses **MapStruct 1.6.3** for type-safe, compile-time bean mapping between entities and DTOs.

## Benefits of MapStruct

- ✅ **Compile-time generation** - No reflection at runtime
- ✅ **Type-safe** - Compilation errors for incorrect mappings
- ✅ **Fast** - Generated code is as fast as hand-written code
- ✅ **Spring Integration** - Auto-configured as Spring beans
- ✅ **Lombok Support** - Works seamlessly with Lombok's builders

## Configuration

### Maven Dependencies

```xml
<properties>
    <mapstruct.version>1.6.3</mapstruct.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>
</dependencies>
```

### Annotation Processor Configuration

The annotation processor is configured in the `maven-compiler-plugin`:

```xml
<annotationProcessorPaths>
    <!-- MapStruct processor must come BEFORE Lombok -->
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <!-- Binding to make MapStruct work with Lombok -->
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-mapstruct-binding</artifactId>
        <version>0.2.0</version>
    </path>
</annotationProcessorPaths>
```

**Important:** MapStruct processor must come before Lombok in the processor paths!

## ItemMapper Example

### Interface Definition

```java
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Item toEntity(ItemCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ItemUpdateRequest request, @MappingTarget Item item);

    ItemResponse toResponse(Item item);
    
    ItemDetailResponse toDetailResponse(Item item);
}
```

### Key Annotations

- **`@Mapper`** - Marks interface as MapStruct mapper
  - `componentModel = "spring"` - Generates Spring `@Component`
  - `unmappedTargetPolicy = IGNORE` - Doesn't require mapping all target fields
  - `nullValuePropertyMappingStrategy = IGNORE` - Skips null values in updates

- **`@Mapping`** - Configures field mapping
  - `target` - The target field name
  - `ignore = true` - Don't map this field
  - `constant = "value"` - Set constant value

- **`@MappingTarget`** - Updates existing object instead of creating new one

### Generated Implementation

MapStruct generates `ItemMapperImpl.java` at compile time:

```java
@Component
public class ItemMapperImpl implements ItemMapper {
    // Generated implementation with null checks
    // No reflection - just plain Java code
    // Optimized for performance
}
```

Location: `target/generated-sources/annotations/io/github/edmaputra/cpwarehouse/mapper/ItemMapperImpl.java`

## Usage in Services

```java
@Service
@RequiredArgsConstructor
public class ItemService {
    
    private final ItemMapper itemMapper;  // Injected by Spring
    
    public ItemResponse createItem(ItemCreateRequest request) {
        // Convert request to entity
        Item item = itemMapper.toEntity(request);
        
        // Save and return response
        Item savedItem = itemRepository.save(item);
        return itemMapper.toResponse(savedItem);
    }
    
    public ItemResponse updateItem(String id, ItemUpdateRequest request) {
        Item item = findItemById(id);
        
        // Update only non-null fields
        itemMapper.updateEntityFromRequest(request, item);
        
        Item updatedItem = itemRepository.save(item);
        return itemMapper.toResponse(updatedItem);
    }
}
```

## Building the Project

MapStruct code generation happens during compilation:

```bash
# Clean and compile
./mvnw clean compile

# The generated mapper will be in:
# target/generated-sources/annotations/
```

## IDE Integration

### IntelliJ IDEA

1. **Enable Annotation Processing:**
   - Settings → Build → Compiler → Annotation Processors
   - ✅ Enable annotation processing

2. **Configure Processor Path:**
   - Automatically configured via Maven
   - IntelliJ will detect annotation processors from pom.xml

3. **Rebuild Project:**
   - Build → Rebuild Project

### VS Code

1. **Install Extensions:**
   - Java Extension Pack
   - Maven for Java

2. **Build Project:**
   - Run Maven compile task
   - Generated sources will be in target/

## Advanced Features

### Custom Mapping Methods

```java
@Mapper(componentModel = "spring")
public interface ItemMapper {
    
    @Mapping(target = "formattedPrice", source = "basePrice", qualifiedByName = "formatPrice")
    ItemResponse toResponse(Item item);
    
    @Named("formatPrice")
    default String formatPrice(BigDecimal price) {
        return "$" + price.setScale(2, RoundingMode.HALF_UP);
    }
}
```

### Multiple Source Parameters

```java
@Mapping(target = "totalPrice", expression = "java(item.getBasePrice().multiply(quantity))")
OrderItemResponse toOrderItem(Item item, BigDecimal quantity);
```

### Collection Mapping

```java
List<ItemResponse> toResponseList(List<Item> items);
```

MapStruct automatically generates collection mappers!

## Troubleshooting

### Mapper Not Found at Runtime

**Solution:** Ensure `componentModel = "spring"` is set in `@Mapper`

### Changes Not Reflected

**Solution:** Rebuild project to regenerate mapper implementation

### IDE Shows Errors but Maven Compiles

**Solution:** Rebuild project in IDE or reimport Maven project

## References

- [MapStruct Documentation](https://mapstruct.org/)
- [MapStruct + Lombok](https://mapstruct.org/documentation/stable/reference/html/#lombok)
- [MapStruct + Spring](https://mapstruct.org/documentation/stable/reference/html/#spring)

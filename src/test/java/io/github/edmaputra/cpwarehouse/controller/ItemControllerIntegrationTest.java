package io.github.edmaputra.cpwarehouse.controller;

import io.github.edmaputra.cpwarehouse.dto.request.ItemCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.request.ItemUpdateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ItemController.
 */
@SpringBootTest
@AutoConfigureMockMvc
//@Testcontainers
class ItemControllerIntegrationTest {

//    @Container
//    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
//            .withExposedPorts(27017);

//    @DynamicPropertySource
//    static void setProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
//    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        itemRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Clean database after each test
        itemRepository.deleteAll();
    }

    @Test
    void createItem_WithValidRequest_ShouldReturnCreatedItem() throws Exception {
        // Given
        ItemCreateRequest request = ItemCreateRequest.builder()
                .sku("TEST-001")
                .name("Test Item")
                .description("Test Description")
                .basePrice(new BigDecimal("29.99"))
                .build();

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item created successfully"))
                .andExpect(jsonPath("$.data.sku").value("TEST-001"))
                .andExpect(jsonPath("$.data.name").value("Test Item"))
                .andExpect(jsonPath("$.data.description").value("Test Description"))
                .andExpect(jsonPath("$.data.basePrice").value(29.99))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists())
                .andReturn();

        // Verify database
        assertThat(itemRepository.count()).isEqualTo(1);
        assertThat(itemRepository.findBySku("TEST-001")).isPresent();
    }

    @Test
    void createItem_WithDuplicateSku_ShouldReturnConflict() throws Exception {
        // Given - Create first item
        ItemCreateRequest firstRequest = ItemCreateRequest.builder()
                .sku("TEST-001")
                .name("First Item")
                .description("First Description")
                .basePrice(new BigDecimal("29.99"))
                .build();

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When & Then - Try to create duplicate
        ItemCreateRequest duplicateRequest = ItemCreateRequest.builder()
                .sku("TEST-001")
                .name("Duplicate Item")
                .description("Duplicate Description")
                .basePrice(new BigDecimal("39.99"))
                .build();

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.error.message").value(containsString("TEST-001")));

        // Verify only one item exists
        assertThat(itemRepository.count()).isEqualTo(1);
    }

    @Test
    void createItem_WithInvalidSku_ShouldReturnBadRequest() throws Exception {
        // Given - SKU with lowercase letters (invalid)
        ItemCreateRequest request = ItemCreateRequest.builder()
                .sku("test-001")
                .name("Test Item")
                .description("Test Description")
                .basePrice(new BigDecimal("29.99"))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.sku").exists());
    }

    @Test
    void createItem_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Given - Missing required fields
        ItemCreateRequest request = ItemCreateRequest.builder()
                .sku("TEST-001")
                // missing name and basePrice
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.name").exists())
                .andExpect(jsonPath("$.error.details.basePrice").exists());
    }

    @Test
    void getAllItems_WithNoItems_ShouldReturnEmptyPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/items"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void getAllItems_WithMultipleItems_ShouldReturnPagedItems() throws Exception {
        // Given - Create multiple items
        createTestItem("TEST-001", "Item One", new BigDecimal("10.00"));
        createTestItem("TEST-002", "Item Two", new BigDecimal("20.00"));
        createTestItem("TEST-003", "Item Three", new BigDecimal("30.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/items")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    void getAllItems_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Given - Create 5 items
        for (int i = 1; i <= 5; i++) {
            createTestItem("TEST-00" + i, "Item " + i, new BigDecimal("10.00"));
        }

        // When & Then - Get page 0 with size 2
        mockMvc.perform(get("/api/v1/items")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(3));
    }

    @Test
    void getAllItems_WithIsActiveFilter_ShouldReturnFilteredItems() throws Exception {
        // Given - Create active and inactive items
        String activeItemId = createTestItem("TEST-001", "Active Item", new BigDecimal("10.00"));
        String inactiveItemId = createTestItem("TEST-002", "Inactive Item", new BigDecimal("20.00"));
        
        // Soft delete one item
        mockMvc.perform(delete("/api/v1/items/" + inactiveItemId));

        // When & Then - Get only active items
        mockMvc.perform(get("/api/v1/items")
                        .param("isActive", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].sku").value("TEST-001"));
    }

    @Test
    void getAllItems_WithSearchTerm_ShouldReturnMatchingItems() throws Exception {
        // Given
        createTestItem("TEST-001", "Blue Shirt", new BigDecimal("10.00"));
        createTestItem("TEST-002", "Red Shirt", new BigDecimal("20.00"));
        createTestItem("TEST-003", "Blue Pants", new BigDecimal("30.00"));

        // When & Then - Search for "Blue"
        mockMvc.perform(get("/api/v1/items")
                        .param("search", "Blue"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].name", everyItem(containsString("Blue"))));
    }

    @Test
    void getItemById_WithExistingId_ShouldReturnItem() throws Exception {
        // Given
        String itemId = createTestItem("TEST-001", "Test Item", new BigDecimal("29.99"));

        // When & Then
        mockMvc.perform(get("/api/v1/items/" + itemId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(itemId))
                .andExpect(jsonPath("$.data.sku").value("TEST-001"))
                .andExpect(jsonPath("$.data.name").value("Test Item"));
    }

    @Test
    void getItemById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        String nonExistingId = "507f1f77bcf86cd799439011";

        // When & Then
        mockMvc.perform(get("/api/v1/items/" + nonExistingId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value(containsString(nonExistingId)));
    }

    @Test
    void updateItem_WithValidRequest_ShouldReturnUpdatedItem() throws Exception {
        // Given
        String itemId = createTestItem("TEST-001", "Original Name", new BigDecimal("29.99"));

        ItemUpdateRequest updateRequest = ItemUpdateRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .basePrice(new BigDecimal("39.99"))
                .isActive(true)
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item updated successfully"))
                .andExpect(jsonPath("$.data.id").value(itemId))
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.description").value("Updated Description"))
                .andExpect(jsonPath("$.data.basePrice").value(39.99))
                .andExpect(jsonPath("$.data.sku").value("TEST-001")); // SKU should not change
    }

    @Test
    void updateItem_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        String nonExistingId = "507f1f77bcf86cd799439011";
        
        ItemUpdateRequest updateRequest = ItemUpdateRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .basePrice(new BigDecimal("39.99"))
                .isActive(true)
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/items/" + nonExistingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void deleteItem_WithExistingId_ShouldSoftDeleteItem() throws Exception {
        // Given
        String itemId = createTestItem("TEST-001", "Test Item", new BigDecimal("29.99"));

        // When & Then
        mockMvc.perform(delete("/api/v1/items/" + itemId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify item is soft deleted
        mockMvc.perform(get("/api/v1/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    void deleteItem_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        String nonExistingId = "507f1f77bcf86cd799439011";

        // When & Then
        mockMvc.perform(delete("/api/v1/items/" + nonExistingId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void hardDeleteItem_WithExistingId_ShouldPermanentlyDeleteItem() throws Exception {
        // Given
        String itemId = createTestItem("TEST-001", "Test Item", new BigDecimal("29.99"));

        // When & Then
        mockMvc.perform(delete("/api/v1/items/" + itemId + "/permanent"))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify item is permanently deleted
        assertThat(itemRepository.findById(itemId)).isEmpty();
        assertThat(itemRepository.count()).isEqualTo(0);
    }

    /**
     * Helper method to create a test item.
     */
    private String createTestItem(String sku, String name, BigDecimal basePrice) throws Exception {
        ItemCreateRequest request = ItemCreateRequest.builder()
                .sku(sku)
                .name(name)
                .description("Test description for " + name)
                .basePrice(basePrice)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        
        // Parse the response to extract the item ID
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseContent);
        String itemId = jsonNode.get("data").get("id").asText();

        return itemId;
    }
}

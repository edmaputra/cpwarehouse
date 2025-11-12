package io.github.edmaputra.cpwarehouse.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.edmaputra.cpwarehouse.dto.request.VariantCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.request.VariantUpdateRequest;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VariantControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    VariantRepository variantRepository;

    @Autowired
    TestHelper testHelper;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        variantRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Clean database after each test
        variantRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void createVariant_WithValidRequest_ShouldReturnCreatedVariant() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));

        VariantCreateRequest request = VariantCreateRequest.builder()
                .itemId(itemId)
                .variantSku("VAR-001")
                .variantName("Red Variant")
                .attributes(Map.of("color", "red", "size", "L"))
                .priceAdjustment(new BigDecimal("10.00"))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/items/" + itemId + "/variants").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Variant created successfully"))
                .andExpect(jsonPath("$.data.variantSku").value("VAR-001"))
                .andExpect(jsonPath("$.data.variantName").value("Red Variant"))
                .andExpect(jsonPath("$.data.attributes.color").value("red"))
                .andExpect(jsonPath("$.data.attributes.size").value("L"))
                .andExpect(jsonPath("$.data.priceAdjustment").value(10.00))
                .andExpect(jsonPath("$.data.finalPrice").value(110.00))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.id").exists());

        // Verify database
        assertThat(variantRepository.count()).isEqualTo(1);
        assertThat(variantRepository.findByVariantSku("VAR-001")).isPresent();
    }

    @Test
    void createVariant_WithDuplicateSku_ShouldReturnConflict() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));

        VariantCreateRequest firstRequest = VariantCreateRequest.builder()
                .itemId(itemId)
                .variantSku("VAR-001")
                .variantName("First Variant")
                .attributes(Map.of("color", "red"))
                .priceAdjustment(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post("/api/v1/items/" + itemId + "/variants").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest))).andExpect(status().isCreated());

        // When & Then - Try to create duplicate
        VariantCreateRequest duplicateRequest = VariantCreateRequest.builder()
                .itemId(itemId)
                .variantSku("VAR-001")
                .variantName("Duplicate Variant")
                .attributes(Map.of("color", "blue"))
                .priceAdjustment(new BigDecimal("20.00"))
                .build();

        mockMvc.perform(post("/api/v1/items/" + itemId + "/variants").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.error.message").value(containsString("VAR-001")));

        // Verify only one variant exists
        assertThat(variantRepository.count()).isEqualTo(1);
    }

    @Test
    void createVariant_WithNonExistentItem_ShouldReturnNotFound() throws Exception {
        // Given
        String nonExistentItemId = "507f1f77bcf86cd799439011";

        VariantCreateRequest request = VariantCreateRequest.builder()
                .itemId(nonExistentItemId)
                .variantSku("VAR-001")
                .variantName("Test Variant")
                .attributes(Map.of("color", "red"))
                .priceAdjustment(new BigDecimal("10.00"))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/items/" + nonExistentItemId + "/variants").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void createVariant_WithInactiveItem_ShouldReturnBadRequest() throws Exception {
        // Given - Create item and soft delete it
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));

        mockMvc.perform(delete("/api/v1/items/" + itemId)).andExpect(status().isNoContent());

        VariantCreateRequest request = VariantCreateRequest.builder()
                .itemId(itemId)
                .variantSku("VAR-001")
                .variantName("Test Variant")
                .attributes(Map.of("color", "red"))
                .priceAdjustment(new BigDecimal("10.00"))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/items/" + itemId + "/variants").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_OPERATION"))
                .andExpect(jsonPath("$.error.message").value(containsString("inactive")));
    }

    @Test
    void createVariant_WithNegativeFinalPrice_ShouldReturnBadRequest() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("10.00"));

        VariantCreateRequest request = VariantCreateRequest.builder()
                .itemId(itemId)
                .variantSku("VAR-001")
                .variantName("Discount Variant")
                .attributes(Map.of("discount", "yes"))
                .priceAdjustment(new BigDecimal("-15.00")) // Results in negative final price
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/items/" + itemId + "/variants").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_OPERATION"))
                .andExpect(jsonPath("$.error.message").value(containsString("negative")));
    }

    @Test
    void createVariant_WithInvalidSku_ShouldReturnBadRequest() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));

        VariantCreateRequest request =
                VariantCreateRequest.builder()
                        .itemId(itemId)
                        .variantSku("var-001") // lowercase is invalid
                        .variantName("Test Variant")
                        .attributes(Map.of("color", "red"))
                        .priceAdjustment(new BigDecimal("10.00"))
                        .build();

        // When & Then
        mockMvc.perform(post("/api/v1/items/" + itemId + "/variants").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.variantSku").exists());
    }

    @Test
    void createVariant_WithNullPriceAdjustment_ShouldUseZeroAdjustment() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));

        VariantCreateRequest request = VariantCreateRequest.builder()
                .itemId(itemId)
                .variantSku("VAR-001")
                .variantName("Standard Variant")
                .attributes(Map.of("type", "standard"))
                .priceAdjustment(null) // No price adjustment
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/items/" + itemId + "/variants").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Variant created successfully"))
                .andExpect(jsonPath("$.data.variantSku").value("VAR-001"))
                .andExpect(jsonPath("$.data.priceAdjustment").value(0.00))
                .andExpect(jsonPath("$.data.finalPrice").value(100.00)); // Same as base price
    }

    @Test
    void getVariantsByItemId_WithMultipleVariants_ShouldReturnAllVariants() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        createTestVariant(itemId, "VAR-001", "Red Variant", new BigDecimal("10.00"));
        createTestVariant(itemId, "VAR-002", "Blue Variant", new BigDecimal("15.00"));
        createTestVariant(itemId, "VAR-003", "Green Variant", new BigDecimal("20.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/items/" + itemId + "/variants"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    void getVariantsByItemId_WithNoVariants_ShouldReturnEmptyList() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/items/" + itemId + "/variants"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getAllVariants_WithPagination_ShouldReturnPagedVariants() throws Exception {
        // Given
        String item1Id = testHelper.createTestItem("ITEM-001", "Item 1", new BigDecimal("100.00"));
        String item2Id = testHelper.createTestItem("ITEM-002", "Item 2", new BigDecimal("200.00"));

        createTestVariant(item1Id, "VAR-001", "Variant 1", new BigDecimal("10.00"));
        createTestVariant(item1Id, "VAR-002", "Variant 2", new BigDecimal("20.00"));
        createTestVariant(item2Id, "VAR-003", "Variant 3", new BigDecimal("30.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/variants").param("page", "0").param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }

    @Test
    void getAllVariants_WithItemIdFilter_ShouldReturnFilteredVariants() throws Exception {
        // Given
        String item1Id = testHelper.createTestItem("ITEM-001", "Item 1", new BigDecimal("100.00"));
        String item2Id = testHelper.createTestItem("ITEM-002", "Item 2", new BigDecimal("200.00"));

        createTestVariant(item1Id, "VAR-001", "Variant 1", new BigDecimal("10.00"));
        createTestVariant(item1Id, "VAR-002", "Variant 2", new BigDecimal("20.00"));
        createTestVariant(item2Id, "VAR-003", "Variant 3", new BigDecimal("30.00"));

        // When & Then - Filter by item1Id
        mockMvc.perform(get("/api/v1/variants").param("itemId", item1Id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    void getAllVariants_WithSearchTerm_ShouldReturnMatchingVariants() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        createTestVariant(itemId, "VAR-001", "Red Variant", new BigDecimal("10.00"));
        createTestVariant(itemId, "VAR-002", "Blue Variant", new BigDecimal("20.00"));
        createTestVariant(itemId, "VAR-003", "Red Special", new BigDecimal("30.00"));

        // When & Then - Search for "Red"
        mockMvc.perform(get("/api/v1/variants").param("search", "Red"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].variantName", everyItem(containsString("Red"))));
    }

    @Test
    void getAllVariants_WithIsActiveTrue_ShouldReturnOnlyActiveVariants() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        String variant1Id = createTestVariant(itemId, "VAR-001", "Active Variant 1", new BigDecimal("10.00"));
        String variant2Id = createTestVariant(itemId, "VAR-002", "Active Variant 2", new BigDecimal("20.00"));
        String variant3Id = createTestVariant(itemId, "VAR-003", "Inactive Variant", new BigDecimal("30.00"));

        // Soft delete variant3 to make it inactive
        mockMvc.perform(delete("/api/v1/variants/" + variant3Id)).andExpect(status().isNoContent());

        // When & Then - Filter by isActive=true
        mockMvc.perform(get("/api/v1/variants").param("isActive", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].isActive").value(true))
                .andExpect(jsonPath("$.data.content[1].isActive").value(true));
    }

    @Test
    void getAllVariants_WithIsActiveFalse_ShouldReturnOnlyInactiveVariants() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        String variant1Id = createTestVariant(itemId, "VAR-001", "Active Variant", new BigDecimal("10.00"));
        String variant2Id = createTestVariant(itemId, "VAR-002", "Inactive Variant 1", new BigDecimal("20.00"));
        String variant3Id = createTestVariant(itemId, "VAR-003", "Inactive Variant 2", new BigDecimal("30.00"));

        // Soft delete variant2 and variant3 to make them inactive
        mockMvc.perform(delete("/api/v1/variants/" + variant2Id)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/variants/" + variant3Id)).andExpect(status().isNoContent());

        // When & Then - Filter by isActive=false
        mockMvc.perform(get("/api/v1/variants").param("isActive", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].isActive").value(false))
                .andExpect(jsonPath("$.data.content[1].isActive").value(false));
    }

    @Test
    void getVariantById_WithExistingId_ShouldReturnVariant() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        String variantId = createTestVariant(itemId, "VAR-001", "Test Variant", new BigDecimal("10.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/variants/" + variantId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(variantId))
                .andExpect(jsonPath("$.data.variantSku").value("VAR-001"))
                .andExpect(jsonPath("$.data.variantName").value("Test Variant"));
    }

    @Test
    void getVariantBySku_WithExistingSku_ShouldReturnVariant() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        createTestVariant(itemId, "VAR-001", "Test Variant", new BigDecimal("10.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/variants/sku/VAR-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.variantSku").value("VAR-001"))
                .andExpect(jsonPath("$.data.variantName").value("Test Variant"));
    }

    @Test
    void updateVariant_WithValidRequest_ShouldReturnUpdatedVariant() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        String variantId = createTestVariant(itemId, "VAR-001", "Original Name", new BigDecimal("10.00"));

        VariantUpdateRequest updateRequest = VariantUpdateRequest.builder()
                .variantName("Updated Name")
                .priceAdjustment(new BigDecimal("20.00"))
                .attributes(Map.of("color", "blue", "size", "XL"))
                .isActive(true)
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/variants/" + variantId).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Variant updated successfully"))
                .andExpect(jsonPath("$.data.id").value(variantId))
                .andExpect(jsonPath("$.data.variantName").value("Updated Name"))
                .andExpect(jsonPath("$.data.priceAdjustment").value(20.00))
                .andExpect(jsonPath("$.data.finalPrice").value(120.00))
                .andExpect(jsonPath("$.data.variantSku").value("VAR-001")); // SKU should not change
    }

    @Test
    void updateVariant_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        String nonExistingId = "507f1f77bcf86cd799439011";

        VariantUpdateRequest updateRequest =
                VariantUpdateRequest.builder().variantName("Updated Name").priceAdjustment(new BigDecimal("20.00")).build();

        // When & Then
        mockMvc.perform(put("/api/v1/variants/" + nonExistingId).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void updateVariant_WithNegativeFinalPrice_ShouldReturnBadRequest() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("10.00"));
        String variantId = createTestVariant(itemId, "VAR-001", "Test Variant", new BigDecimal("5.00"));

        VariantUpdateRequest updateRequest =
                VariantUpdateRequest.builder().priceAdjustment(new BigDecimal("-15.00")) // Results in negative final price
                        .build();

        // When & Then
        mockMvc.perform(put("/api/v1/variants/" + variantId).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_OPERATION"))
                .andExpect(jsonPath("$.error.message").value(containsString("negative")));
    }

    @Test
    void updateVariant_WithNullPriceAdjustment_ShouldKeepCurrentAdjustment() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        String variantId = createTestVariant(itemId, "VAR-001", "Test Variant", new BigDecimal("20.00"));

        VariantUpdateRequest updateRequest = VariantUpdateRequest.builder()
                .variantName("Updated Variant")
                .priceAdjustment(null) // Keep existing price adjustment
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/variants/" + variantId).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.variantName").value("Updated Variant"))
                .andExpect(jsonPath("$.data.priceAdjustment").value(20.00)) // Should keep original
                .andExpect(jsonPath("$.data.finalPrice").value(120.00));
    }

    @Test
    void getAllVariants_WithSortingAsc_ShouldReturnSortedVariants() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        createTestVariant(itemId, "VAR-003", "Zebra Variant", new BigDecimal("30.00"));
        createTestVariant(itemId, "VAR-001", "Alpha Variant", new BigDecimal("10.00"));
        createTestVariant(itemId, "VAR-002", "Beta Variant", new BigDecimal("20.00"));

        // When & Then - Sort by variantName ascending
        mockMvc.perform(get("/api/v1/variants")
                        .param("sortBy", "variantName")
                        .param("sortDir", "ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.content[0].variantName").value("Alpha Variant"))
                .andExpect(jsonPath("$.data.content[1].variantName").value("Beta Variant"))
                .andExpect(jsonPath("$.data.content[2].variantName").value("Zebra Variant"));
    }

    @Test
    void getAllVariants_WithSortingDesc_ShouldReturnSortedVariants() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        createTestVariant(itemId, "VAR-001", "Variant A", new BigDecimal("10.00"));
        createTestVariant(itemId, "VAR-002", "Variant B", new BigDecimal("30.00"));
        createTestVariant(itemId, "VAR-003", "Variant C", new BigDecimal("20.00"));

        // When & Then - Sort by priceAdjustment descending
        mockMvc.perform(get("/api/v1/variants")
                        .param("sortBy", "priceAdjustment")
                        .param("sortDir", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.content[0].priceAdjustment").value(30.00))
                .andExpect(jsonPath("$.data.content[1].priceAdjustment").value(20.00))
                .andExpect(jsonPath("$.data.content[2].priceAdjustment").value(10.00));
    }

    @Test
    void deleteVariant_WithExistingId_ShouldSoftDeleteVariant() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        String variantId = createTestVariant(itemId, "VAR-001", "Test Variant", new BigDecimal("10.00"));

        // When & Then
        mockMvc.perform(delete("/api/v1/variants/" + variantId)).andDo(print()).andExpect(status().isNoContent());

        // Verify variant is soft deleted
        mockMvc.perform(get("/api/v1/variants/" + variantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    void hardDeleteVariant_WithExistingId_ShouldPermanentlyDeleteVariant() throws Exception {
        // Given
        String itemId = testHelper.createTestItem("ITEM-001", "Test Item", new BigDecimal("100.00"));
        String variantId = createTestVariant(itemId, "VAR-001", "Test Variant", new BigDecimal("10.00"));

        // When & Then
        mockMvc.perform(delete("/api/v1/variants/" + variantId + "/permanent"))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify variant is permanently deleted
        assertThat(variantRepository.findById(variantId)).isEmpty();
        assertThat(variantRepository.count()).isEqualTo(0);
    }

    @Test
    void deleteVariant_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        String nonExistingId = "507f1f77bcf86cd799439011";

        // When & Then
        mockMvc.perform(delete("/api/v1/variants/" + nonExistingId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    /**
     * Helper method to create a test variant.
     */
    private String createTestVariant(String itemId, String variantSku, String variantName, BigDecimal priceAdjustment)
            throws Exception {
        VariantCreateRequest request = VariantCreateRequest.builder()
                .itemId(itemId)
                .variantSku(variantSku)
                .variantName(variantName)
                .attributes(Map.of("test", "value"))
                .priceAdjustment(priceAdjustment)
                .build();

        MvcResult result =
                mockMvc.perform(post("/api/v1/items/" + itemId + "/variants").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andReturn();

        String responseContent = result.getResponse().getContentAsString();

        // Parse the response to extract the variant ID
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseContent);
        return jsonNode.get("data").get("id").asText();
    }
}

package io.github.edmaputra.cpwarehouse.integration;

import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.request.CheckoutRequest;
import io.github.edmaputra.cpwarehouse.dto.request.PaymentRequest;
import io.github.edmaputra.cpwarehouse.dto.response.CheckoutResponse;
import io.github.edmaputra.cpwarehouse.dto.response.PaymentResponse;
import io.github.edmaputra.cpwarehouse.repository.CheckoutItemRepository;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Checkout and Payment APIs.
 * Tests the complete e-commerce flow: checkout → payment → stock commitment/release.
 */
class CheckoutPaymentIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private VariantRepository variantRepository;

  @Autowired
  private StockRepository stockRepository;

  @Autowired
  private CheckoutItemRepository checkoutItemRepository;

  private Item testItem;
  private Variant testVariant;
  private Stock testStock;

  @BeforeEach
  void setUp() {
    // Clean up
    checkoutItemRepository.deleteAll();
    stockRepository.deleteAll();
    variantRepository.deleteAll();
    itemRepository.deleteAll();

    // Create test item
    testItem = Item.builder()
        .sku("TEST-ITEM-001")
        .name("Test Product")
        .description("Test product for checkout")
        .basePrice(new BigDecimal("100.00"))
        .isActive(true)
        .build();
    testItem.prePersist();
    testItem = itemRepository.save(testItem);

    // Create test variant
    testVariant = Variant.builder()
        .itemId(testItem.getId())
        .variantSku("TEST-VAR-001")
        .variantName("Size L - Red")
        .priceAdjustment(new BigDecimal("10.00"))
        .isActive(true)
        .build();
    testVariant.prePersist();
    testVariant = variantRepository.save(testVariant);

    // Create test stock
    testStock = Stock.builder()
        .itemId(testItem.getId())
        .variantId(testVariant.getId())
        .quantity(100)
        .reservedQuantity(0)
        .warehouseLocation("A-01-01")
        .build();
    testStock.prePersist();
    testStock = stockRepository.save(testStock);
  }

  @Test
  void checkout_WithValidRequest_ShouldReserveStock() throws Exception {
    // Given
    CheckoutRequest request = CheckoutRequest.builder()
        .itemId(testItem.getId())
        .variantId(testVariant.getId())
        .quantity(5)
        .customerId("CUST-001")
        .checkoutReference("CHECKOUT-001")
        .build();

    // When
    MvcResult result = mockMvc.perform(post("/api/v1/checkout")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.itemId").value(testItem.getId()))
        .andExpect(jsonPath("$.data.variantId").value(testVariant.getId()))
        .andExpect(jsonPath("$.data.quantity").value(5))
        .andExpect(jsonPath("$.data.pricePerUnit").value(110.00))
        .andExpect(jsonPath("$.data.totalPrice").value(550.00))
        .andExpect(jsonPath("$.data.status").value("PENDING"))
        .andReturn();

    // Then - Verify stock is reserved
    Stock updatedStock = stockRepository.findById(testStock.getId()).orElseThrow();
    assertThat(updatedStock.getQuantity()).isEqualTo(100);
    assertThat(updatedStock.getReservedQuantity()).isEqualTo(5);
    assertThat(updatedStock.getAvailableQuantity()).isEqualTo(95);
  }

  @Test
  void checkout_WithInsufficientStock_ShouldReturnError() throws Exception {
    // Given
    CheckoutRequest request = CheckoutRequest.builder()
        .itemId(testItem.getId())
        .variantId(testVariant.getId())
        .quantity(150)
        .customerId("CUST-001")
        .checkoutReference("CHECKOUT-002")
        .build();

    // When & Then
    mockMvc.perform(post("/api/v1/checkout")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_STOCK"));
  }

  @Test
  void payment_WithSuccessfulPayment_ShouldCommitStock() throws Exception {
    // Given - Create checkout first
    CheckoutRequest checkoutRequest = CheckoutRequest.builder()
        .itemId(testItem.getId())
        .variantId(testVariant.getId())
        .quantity(10)
        .customerId("CUST-002")
        .checkoutReference("CHECKOUT-003")
        .build();

    MvcResult checkoutResult = mockMvc.perform(post("/api/v1/checkout")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(checkoutRequest)))
        .andExpect(status().isOk())
        .andReturn();

    String checkoutJson = checkoutResult.getResponse().getContentAsString();
    CheckoutResponse checkoutResponse = objectMapper.readValue(
        objectMapper.readTree(checkoutJson).get("data").toString(),
        CheckoutResponse.class);

    // When - Process payment
    PaymentRequest paymentRequest = PaymentRequest.builder()
        .paymentAmount(new BigDecimal("1100.00"))
        .paymentSuccess(true)
        .paymentReference("PAY-001")
        .processedBy("SYSTEM")
        .build();

    mockMvc.perform(post("/api/v1/checkout/" + checkoutResponse.getId() + "/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.paymentSuccess").value(true))
        .andExpect(jsonPath("$.data.status").value("COMPLETED"));

    // Then - Verify stock is committed (reduced from both total and reserved)
    Stock updatedStock = stockRepository.findById(testStock.getId()).orElseThrow();
    assertThat(updatedStock.getQuantity()).isEqualTo(90); // 100 - 10
    assertThat(updatedStock.getReservedQuantity()).isEqualTo(0);
    assertThat(updatedStock.getAvailableQuantity()).isEqualTo(90);
  }

  @Test
  void payment_WithFailedPayment_ShouldReleaseStock() throws Exception {
    // Given - Create checkout first
    CheckoutRequest checkoutRequest = CheckoutRequest.builder()
        .itemId(testItem.getId())
        .variantId(testVariant.getId())
        .quantity(8)
        .customerId("CUST-003")
        .checkoutReference("CHECKOUT-004")
        .build();

    MvcResult checkoutResult = mockMvc.perform(post("/api/v1/checkout")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(checkoutRequest)))
        .andExpect(status().isOk())
        .andReturn();

    String checkoutJson = checkoutResult.getResponse().getContentAsString();
    CheckoutResponse checkoutResponse = objectMapper.readValue(
        objectMapper.readTree(checkoutJson).get("data").toString(),
        CheckoutResponse.class);

    // When - Process payment with failure
    PaymentRequest paymentRequest = PaymentRequest.builder()
        .paymentAmount(new BigDecimal("880.00"))
        .paymentSuccess(false)
        .paymentReference("PAY-002")
        .processedBy("SYSTEM")
        .build();

    mockMvc.perform(post("/api/v1/checkout/" + checkoutResponse.getId() + "/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.paymentSuccess").value(false))
        .andExpect(jsonPath("$.data.status").value("PAYMENT_FAILED"));

    // Then - Verify stock is released (only reserved is reduced)
    Stock updatedStock = stockRepository.findById(testStock.getId()).orElseThrow();
    assertThat(updatedStock.getQuantity()).isEqualTo(100); // Unchanged
    assertThat(updatedStock.getReservedQuantity()).isEqualTo(0);
    assertThat(updatedStock.getAvailableQuantity()).isEqualTo(100);
  }

  @Test
  void payment_WithInsufficientAmount_ShouldReturnError() throws Exception {
    // Given - Create checkout first
    CheckoutRequest checkoutRequest = CheckoutRequest.builder()
        .itemId(testItem.getId())
        .variantId(testVariant.getId())
        .quantity(5)
        .customerId("CUST-004")
        .checkoutReference("CHECKOUT-005")
        .build();

    MvcResult checkoutResult = mockMvc.perform(post("/api/v1/checkout")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(checkoutRequest)))
        .andExpect(status().isOk())
        .andReturn();

    String checkoutJson = checkoutResult.getResponse().getContentAsString();
    CheckoutResponse checkoutResponse = objectMapper.readValue(
        objectMapper.readTree(checkoutJson).get("data").toString(),
        CheckoutResponse.class);

    // When - Process payment with insufficient amount
    PaymentRequest paymentRequest = PaymentRequest.builder()
        .paymentAmount(new BigDecimal("500.00")) // Less than required 550.00
        .paymentSuccess(true)
        .paymentReference("PAY-003")
        .processedBy("SYSTEM")
        .build();

    // Then
    mockMvc.perform(post("/api/v1/checkout/" + checkoutResponse.getId() + "/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("INVALID_PAYMENT"));
  }
}

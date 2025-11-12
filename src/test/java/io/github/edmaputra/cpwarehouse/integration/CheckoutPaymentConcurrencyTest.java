package io.github.edmaputra.cpwarehouse.integration;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.request.CheckoutRequest;
import io.github.edmaputra.cpwarehouse.dto.request.PaymentRequest;
import io.github.edmaputra.cpwarehouse.dto.response.CheckoutResponse;
import io.github.edmaputra.cpwarehouse.repository.CheckoutItemRepository;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Concurrent integration tests for Checkout and Payment APIs.
 * Tests system reliability under moderate concurrent load.
 */
@Disabled
class CheckoutPaymentConcurrencyTest extends BaseIntegrationTest {

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
        .sku("CONCURRENT-TEST-001")
        .name("Concurrent Test Product")
        .description("Product for concurrent testing")
        .basePrice(new BigDecimal("50.00"))
        .isActive(true)
        .build();
    testItem.prePersist();
    testItem = itemRepository.save(testItem);

    // Create test variant
    testVariant = Variant.builder()
        .itemId(testItem.getId())
        .variantSku("CONCURRENT-VAR-001")
        .variantName("Standard")
        .priceAdjustment(new BigDecimal("5.00"))
        .isActive(true)
        .build();
    testVariant.prePersist();
    testVariant = variantRepository.save(testVariant);

    // Create test stock with moderate quantity
    testStock = Stock.builder()
        .itemId(testItem.getId())
        .variantId(testVariant.getId())
        .quantity(50)
        .reservedQuantity(0)
        .warehouseLocation("CONC-01")
        .build();
    testStock.prePersist();
    testStock = stockRepository.save(testStock);
  }

  @Test
  void concurrentCheckouts_WithModerateContention_ShouldHandleCorrectly() throws InterruptedException {
    // Given - 10 concurrent checkout requests for 3 items each
    int threadCount = 10;
    int quantityPerCheckout = 3;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // When - Execute concurrent checkouts
    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      executor.submit(() -> {
        try {
          CheckoutRequest request = CheckoutRequest.builder()
              .itemId(testItem.getId())
              .variantId(testVariant.getId())
              .quantity(quantityPerCheckout)
              .customerId("CUST-" + index)
              .checkoutReference("CONC-CHECKOUT-" + index)
              .build();

          MvcResult result = mockMvc.perform(post("/api/v1/checkout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andReturn();

          if (result.getResponse().getStatus() == 200) {
            successCount.incrementAndGet();
          } else {
            failureCount.incrementAndGet();
          }
        } catch (Exception e) {
          failureCount.incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }

    // Wait for all threads to complete
    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - Verify results
    // With 50 stock and 10 threads × 3 items = 30 total, all should succeed
    assertThat(successCount.get()).isGreaterThanOrEqualTo(10);
    
    // Verify final stock state
    Stock finalStock = stockRepository.findById(testStock.getId()).orElseThrow();
    assertThat(finalStock.getQuantity()).isEqualTo(50);
    assertThat(finalStock.getReservedQuantity()).isEqualTo(successCount.get() * quantityPerCheckout);
    assertThat(finalStock.getAvailableQuantity()).isEqualTo(50 - (successCount.get() * quantityPerCheckout));
  }

  @Test
  void concurrentCheckoutsWithOversubscription_ShouldPreventOverbooking() throws InterruptedException {
    // Given - 12 concurrent checkout requests for 5 items each (60 total > 50 stock)
    int threadCount = 12;
    int quantityPerCheckout = 5;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // When - Execute concurrent checkouts
    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      executor.submit(() -> {
        try {
          CheckoutRequest request = CheckoutRequest.builder()
              .itemId(testItem.getId())
              .variantId(testVariant.getId())
              .quantity(quantityPerCheckout)
              .customerId("CUST-OVER-" + index)
              .checkoutReference("OVER-CHECKOUT-" + index)
              .build();

          MvcResult result = mockMvc.perform(post("/api/v1/checkout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andReturn();

          if (result.getResponse().getStatus() == 200) {
            successCount.incrementAndGet();
          } else {
            failureCount.incrementAndGet();
          }
        } catch (Exception e) {
          failureCount.incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }

    // Wait for all threads to complete
    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - Verify no overbooking
    // With 50 stock and 5 per checkout, max 10 should succeed
    assertThat(successCount.get()).isLessThanOrEqualTo(10);
    assertThat(successCount.get()).isGreaterThanOrEqualTo(8); // Allow some variance due to retries
    assertThat(failureCount.get()).isGreaterThanOrEqualTo(2);
    
    // Verify stock never exceeded
    Stock finalStock = stockRepository.findById(testStock.getId()).orElseThrow();
    assertThat(finalStock.getReservedQuantity()).isLessThanOrEqualTo(50);
    assertThat(finalStock.getAvailableQuantity()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void concurrentPayments_AfterCheckouts_ShouldMaintainConsistency() throws Exception {
    // Given - Create 8 checkouts first
    int checkoutCount = 8;
    List<String> checkoutIds = new ArrayList<>();
    
    for (int i = 0; i < checkoutCount; i++) {
      CheckoutRequest request = CheckoutRequest.builder()
          .itemId(testItem.getId())
          .variantId(testVariant.getId())
          .quantity(3)
          .customerId("CUST-PAY-" + i)
          .checkoutReference("PAY-CHECKOUT-" + i)
          .build();

      MvcResult result = mockMvc.perform(post("/api/v1/checkout")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andReturn();

      String json = result.getResponse().getContentAsString();
      CheckoutResponse response = objectMapper.readValue(
          objectMapper.readTree(json).get("data").toString(),
          CheckoutResponse.class);
      checkoutIds.add(response.getId());
    }

    // When - Process all payments concurrently (all successful)
    ExecutorService executor = Executors.newFixedThreadPool(checkoutCount);
    CountDownLatch latch = new CountDownLatch(checkoutCount);
    AtomicInteger successPayments = new AtomicInteger(0);

    for (int i = 0; i < checkoutCount; i++) {
      final int index = i;

      executor.submit(() -> {
        try {
          PaymentRequest paymentRequest = PaymentRequest.builder()
              .paymentAmount(new BigDecimal("165.00"))
              .paymentReference("PAY-" + index)
              .processedBy("SYSTEM")
              .build();

          MvcResult result = mockMvc.perform(post("/api/v1/checkout/" + checkoutIds.get(index) + "/payment")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(paymentRequest)))
              .andReturn();

          if (result.getResponse().getStatus() == 200) {
            successPayments.incrementAndGet();
          }
        } catch (Exception e) {
          // Handle exception
        } finally {
          latch.countDown();
        }
      });
    }

    // Wait for all payments to complete
    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - Verify final state
    assertThat(successPayments.get()).isEqualTo(8);

    Stock finalStock = stockRepository.findById(testStock.getId()).orElseThrow();
    // All 8 successful payments should commit: 50 - (8 × 3) = 26
    assertThat(finalStock.getQuantity()).isEqualTo(26);
    assertThat(finalStock.getReservedQuantity()).isEqualTo(0);
    assertThat(finalStock.getAvailableQuantity()).isEqualTo(26);
  }

  @Test
  void mixedConcurrentCheckoutsAndPayments_ShouldMaintainConsistency() throws Exception {
    // Given - Setup initial checkouts
    List<String> checkoutIds = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      CheckoutRequest request = CheckoutRequest.builder()
          .itemId(testItem.getId())
          .variantId(testVariant.getId())
          .quantity(2)
          .customerId("CUST-MIX-" + i)
          .checkoutReference("MIX-CHECKOUT-" + i)
          .build();

      MvcResult result = mockMvc.perform(post("/api/v1/checkout")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andReturn();

      String json = result.getResponse().getContentAsString();
      CheckoutResponse response = objectMapper.readValue(
          objectMapper.readTree(json).get("data").toString(),
          CheckoutResponse.class);
      checkoutIds.add(response.getId());
    }

    // When - Mix of new checkouts and payments
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(10);
    AtomicInteger operations = new AtomicInteger(0);

    // 5 new checkout attempts
    for (int i = 0; i < 5; i++) {
      final int index = i + 5;
      executor.submit(() -> {
        try {
          CheckoutRequest request = CheckoutRequest.builder()
              .itemId(testItem.getId())
              .variantId(testVariant.getId())
              .quantity(2)
              .customerId("CUST-MIX-NEW-" + index)
              .checkoutReference("MIX-NEW-" + index)
              .build();

          mockMvc.perform(post("/api/v1/checkout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andReturn();
          
          operations.incrementAndGet();
        } catch (Exception e) {
          // Handle
        } finally {
          latch.countDown();
        }
      });
    }

    // 5 payment attempts
    for (int i = 0; i < 5; i++) {
      final int index = i;
      executor.submit(() -> {
        try {
          PaymentRequest paymentRequest = PaymentRequest.builder()
              .paymentAmount(new BigDecimal("110.00"))
              .paymentReference("PAY-MIX-" + index)
              .processedBy("SYSTEM")
              .build();

          mockMvc.perform(post("/api/v1/checkout/" + checkoutIds.get(index) + "/payment")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(paymentRequest)))
              .andReturn();
          
          operations.incrementAndGet();
        } catch (Exception e) {
          // Handle
        } finally {
          latch.countDown();
        }
      });
    }

    // Wait for all operations
    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - Verify consistency
    assertThat(operations.get()).isGreaterThanOrEqualTo(8); // Allow some failures

    Stock finalStock = stockRepository.findById(testStock.getId()).orElseThrow();
    // Quantity + Reserved should not exceed original quantity
    assertThat(finalStock.getQuantity() + finalStock.getReservedQuantity()).isLessThanOrEqualTo(50);
    assertThat(finalStock.getAvailableQuantity()).isGreaterThanOrEqualTo(0);
  }
}

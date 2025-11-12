package io.github.edmaputra.cpwarehouse.integration;

import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement.MovementType;
import io.github.edmaputra.cpwarehouse.dto.request.StockAdjustRequest;
import io.github.edmaputra.cpwarehouse.dto.request.StockReleaseRequest;
import io.github.edmaputra.cpwarehouse.dto.request.StockReserveRequest;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.repository.StockMovementRepository;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for concurrent stock operations.
 * Tests verify that optimistic locking and retry mechanisms work correctly
 * under concurrent load scenarios.
 */
@ExtendWith(OutputCaptureExtension.class)
class StockConcurrencyIntegrationTest extends BaseIntegrationTest {

  @Autowired
  StockRepository stockRepository;

  @Autowired
  StockMovementRepository stockMovementRepository;

  @Autowired
  ItemRepository itemRepository;

  @Autowired
  VariantRepository variantRepository;

  @Autowired
  TestHelper testHelper;

  private String testItemId;

  @BeforeEach
  void setUp() throws Exception {
    // Clean databases before each test
    stockMovementRepository.deleteAll();
    stockRepository.deleteAll();
    variantRepository.deleteAll();
    itemRepository.deleteAll();

    // Create test item
    testItemId = testHelper.createTestItem("TEST-ITEM-CONCURRENT", "Test Item for Concurrency", new BigDecimal("100.00"));
  }

  @AfterEach
  void tearDown() {
    // Clean databases after each test
    stockMovementRepository.deleteAll();
    stockRepository.deleteAll();
    variantRepository.deleteAll();
    itemRepository.deleteAll();
  }

  // ==================== CONCURRENT RESERVE TESTS ====================

  @Test
  void concurrentReserveOperations_ShouldAllSucceedWithRetry(CapturedOutput output) throws Exception {
    // Given - Create stock with 1000 units
    String stockId = createTestStock(testItemId, 1000, "WAREHOUSE-A");
    int concurrentRequests = 10;
    int quantityPerRequest = 20;

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);
    ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);
    List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

    // When - Execute concurrent reserve operations
    for (int i = 0; i < concurrentRequests; i++) {
      final int requestNum = i;
      executor.submit(() -> {
        try {
          startLatch.await(); // Wait for all threads to be ready

          StockReserveRequest request = StockReserveRequest.builder()
              .quantity(quantityPerRequest)
              .referenceNumber("ORDER-" + requestNum)
              .createdBy("customer-" + requestNum)
              .build();

          mockMvc.perform(post("/api/v1/stock/" + stockId + "/reserve")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andDo(print())
              .andExpect(status().isOk());

          successCount.incrementAndGet();

        } catch (Exception e) {
          failureCount.incrementAndGet();
          exceptions.add(e);
        } finally {
          completionLatch.countDown();
        }
      });
    }

    // Release all threads at once to create maximum contention
    startLatch.countDown();

    // Wait for all operations to complete (max 30 seconds)
    boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - Verify results
    assertThat(completed).as("All operations should complete within timeout").isTrue();
    assertThat(successCount.get()).as("Most operations should succeed").isGreaterThanOrEqualTo(concurrentRequests - 2);
    assertThat(failureCount.get()).as("Failure count should be minimal").isLessThanOrEqualTo(2);

    // Verify final stock state
    Stock finalStock = stockRepository.findById(stockId).orElseThrow();
    int expectedReserved = concurrentRequests * quantityPerRequest;
    assertThat(finalStock.getReservedQuantity())
        .as("Reserved quantity should be sum of all reservations")
        .isEqualTo(expectedReserved);
    assertThat(finalStock.getQuantity())
        .as("Total quantity should remain unchanged")
        .isEqualTo(1000);
    assertThat(finalStock.getAvailableQuantity())
        .as("Available quantity should be total minus reserved")
        .isEqualTo(1000 - expectedReserved);

    // Verify all movements were recorded
    List<StockMovement> movements = stockMovementRepository.findByStockId(stockId, null).getContent();
    assertThat(movements).hasSize(concurrentRequests);

    // Verify retry mechanism was invoked by checking logs
    String logs = output.toString();
    assertThat(logs)
        .as("Logs should contain retry attempt messages")
        .containsPattern("\\[Retry attempt: [2-5]/5\\]"); // At least one retry occurred

    System.out.println("\n=== Concurrency Test Results ===");
    System.out.println("Concurrent requests: " + concurrentRequests);
    System.out.println("Success count: " + successCount.get());
    System.out.println("Failure count: " + failureCount.get());
    System.out.println("Final reserved quantity: " + finalStock.getReservedQuantity());
    System.out.println("Retry invocations detected: " + (logs.contains("[Retry attempt: 2/5]") ? "YES" : "NO"));
  }

  @Test
  void concurrentAdjustOperations_ShouldAllSucceedWithRetry(CapturedOutput output) throws Exception {
    // Given - Create stock with 500 units
    String stockId = createTestStock(testItemId, 500, "WAREHOUSE-A");
    int concurrentRequests = 8;
    int quantityPerRequest = 10;

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);
    ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);

    AtomicInteger successCount = new AtomicInteger(0);
    List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

    // When - Execute concurrent IN adjustments
    for (int i = 0; i < concurrentRequests; i++) {
      final int requestNum = i;
      executor.submit(() -> {
        try {
          startLatch.await();

          StockAdjustRequest request = StockAdjustRequest.builder()
              .movementType(MovementType.IN)
              .quantity(quantityPerRequest)
              .referenceNumber("ADJ-IN-" + requestNum)
              .notes("Concurrent stock IN")
              .createdBy("admin-" + requestNum)
              .build();

          mockMvc.perform(put("/api/v1/stock/" + stockId + "/adjust")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andDo(print())
              .andExpect(status().isOk());

          successCount.incrementAndGet();

        } catch (Exception e) {
          exceptions.add(e);
        } finally {
          completionLatch.countDown();
        }
      });
    }

    startLatch.countDown();
    boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - Verify results
    assertThat(completed).isTrue();
    assertThat(successCount.get()).isEqualTo(concurrentRequests);
    assertThat(exceptions).isEmpty();

    // Verify final stock state
    Stock finalStock = stockRepository.findById(stockId).orElseThrow();
    int expectedQuantity = 500 + (concurrentRequests * quantityPerRequest);
    assertThat(finalStock.getQuantity()).isEqualTo(expectedQuantity);

    // Verify retry mechanism was invoked
    String logs = output.toString();
    assertThat(logs)
        .as("Logs should show retry attempts during concurrent adjustments")
        .containsPattern("\\[Retry attempt: [2-5]/5\\]");

    System.out.println("\n=== Concurrent Adjust Test Results ===");
    System.out.println("Initial quantity: 500");
    System.out.println("Concurrent IN adjustments: " + concurrentRequests);
    System.out.println("Final quantity: " + finalStock.getQuantity());
    System.out.println("Expected quantity: " + expectedQuantity);
  }

  @Test
  void concurrentReleaseOperations_ShouldAllSucceedWithRetry(CapturedOutput output) throws Exception {
    // Given - Create stock with 1000 units and reserve 500
    String stockId = createTestStock(testItemId, 1000, "WAREHOUSE-A");
    int reservationCount = 10;
    int quantityPerReservation = 50;

    // Create multiple reservations
    List<String> reservationIds = new ArrayList<>();
    for (int i = 0; i < reservationCount; i++) {
      String reservationId = reserveTestStockAndGetMovementId(stockId, quantityPerReservation, "ORDER-" + i);
      reservationIds.add(reservationId);
    }

    // Verify initial state
    Stock stockBeforeRelease = stockRepository.findById(stockId).orElseThrow();
    assertThat(stockBeforeRelease.getReservedQuantity()).isEqualTo(500);

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completionLatch = new CountDownLatch(reservationCount);
    ExecutorService executor = Executors.newFixedThreadPool(reservationCount);

    AtomicInteger successCount = new AtomicInteger(0);
    List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

    // When - Execute concurrent release operations
    for (int i = 0; i < reservationCount; i++) {
      final int requestNum = i;
      final String reservationId = reservationIds.get(i);
      executor.submit(() -> {
        try {
          startLatch.await();

          StockReleaseRequest request = StockReleaseRequest.builder()
              .reservationId(reservationId)
              .movementType(MovementType.RELEASE)
              .referenceNumber("RELEASE-" + requestNum)
              .createdBy("admin-" + requestNum)
              .build();

          mockMvc.perform(post("/api/v1/stock/" + stockId + "/release")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andDo(print())
              .andExpect(status().isOk());

          successCount.incrementAndGet();

        } catch (Exception e) {
          exceptions.add(e);
        } finally {
          completionLatch.countDown();
        }
      });
    }

    startLatch.countDown();
    boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - Verify results
    assertThat(completed).isTrue();
    assertThat(successCount.get()).isGreaterThanOrEqualTo(reservationCount - 1);
    assertThat(exceptions.size()).isLessThanOrEqualTo(1);

    // Verify final stock state
    Stock finalStock = stockRepository.findById(stockId).orElseThrow();
    assertThat(finalStock.getReservedQuantity())
        .as("All reservations should be released")
        .isEqualTo(0);
    assertThat(finalStock.getQuantity())
        .as("Total quantity unchanged for RELEASE type")
        .isEqualTo(1000);

    // Verify retry mechanism was invoked
    String logs = output.toString();
    assertThat(logs)
        .as("Logs should show retry attempts during concurrent releases")
        .containsPattern("\\[Retry attempt: [2-5]/5\\]");

    System.out.println("\n=== Concurrent Release Test Results ===");
    System.out.println("Initial reservations: " + reservationCount);
    System.out.println("Successful releases: " + successCount.get());
    System.out.println("Final reserved quantity: " + finalStock.getReservedQuantity());
  }

  @Test
  void mixedConcurrentOperations_ShouldMaintainDataConsistency(CapturedOutput output) throws Exception {
    // Given - Create stock with 1000 units
    String stockId = createTestStock(testItemId, 1000, "WAREHOUSE-A");
    int totalOperations = 15; // 5 reserve + 5 adjust IN + 5 adjust OUT

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completionLatch = new CountDownLatch(totalOperations);
    ExecutorService executor = Executors.newFixedThreadPool(totalOperations);

    AtomicInteger successCount = new AtomicInteger(0);
    List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

    // When - Execute mixed concurrent operations
    // 5 reserve operations (20 units each = 100 total)
    for (int i = 0; i < 5; i++) {
      final int requestNum = i;
      executor.submit(() -> {
        try {
          startLatch.await();

          StockReserveRequest request = StockReserveRequest.builder()
              .quantity(20)
              .referenceNumber("RESERVE-" + requestNum)
              .createdBy("customer-" + requestNum)
              .build();

          mockMvc.perform(post("/api/v1/stock/" + stockId + "/reserve")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk());

          successCount.incrementAndGet();
        } catch (Exception e) {
          exceptions.add(e);
        } finally {
          completionLatch.countDown();
        }
      });
    }

    // 5 IN adjustments (30 units each = 150 total)
    for (int i = 0; i < 5; i++) {
      final int requestNum = i;
      executor.submit(() -> {
        try {
          startLatch.await();

          StockAdjustRequest request = StockAdjustRequest.builder()
              .movementType(MovementType.IN)
              .quantity(30)
              .referenceNumber("ADJ-IN-" + requestNum)
              .createdBy("admin-" + requestNum)
              .build();

          mockMvc.perform(put("/api/v1/stock/" + stockId + "/adjust")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk());

          successCount.incrementAndGet();
        } catch (Exception e) {
          exceptions.add(e);
        } finally {
          completionLatch.countDown();
        }
      });
    }

    // 5 OUT adjustments (10 units each = 50 total)
    for (int i = 0; i < 5; i++) {
      final int requestNum = i;
      executor.submit(() -> {
        try {
          startLatch.await();

          StockAdjustRequest request = StockAdjustRequest.builder()
              .movementType(MovementType.OUT)
              .quantity(10)
              .referenceNumber("ADJ-OUT-" + requestNum)
              .createdBy("admin-" + requestNum)
              .build();

          mockMvc.perform(put("/api/v1/stock/" + stockId + "/adjust")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk());

          successCount.incrementAndGet();
        } catch (Exception e) {
          exceptions.add(e);
        } finally {
          completionLatch.countDown();
        }
      });
    }

    startLatch.countDown();
    boolean completed = completionLatch.await(45, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - Verify results
    assertThat(completed).isTrue();
    // Allow for some potential failures due to high contention/timing in mixed operations
    assertThat(successCount.get()).as("Most operations should succeed").isGreaterThanOrEqualTo(totalOperations - 3);
    if (!exceptions.isEmpty()) {
      System.out.println("Exceptions encountered: " + exceptions.size());
      exceptions.forEach(e -> System.out.println("  - " + e.getMessage()));
    }

    // Verify final stock state
    // Expected: 1000 + 150 (IN) - 50 (OUT) = 1100 total, 100 reserved
    Stock finalStock = stockRepository.findById(stockId).orElseThrow();
    assertThat(finalStock.getQuantity()).isEqualTo(1100);
    assertThat(finalStock.getReservedQuantity()).isEqualTo(100);
    assertThat(finalStock.getAvailableQuantity()).isEqualTo(1000);

    // Verify all movements were recorded
    List<StockMovement> movements = stockMovementRepository.findByStockId(stockId, null).getContent();
    assertThat(movements).hasSize(totalOperations);

    // Verify retry mechanism was invoked
    String logs = output.toString();
    assertThat(logs)
        .as("Logs should show retry attempts during mixed concurrent operations")
        .containsPattern("\\[Retry attempt: [2-5]/5\\]");

    // Count retry attempts in logs
    long retryCount = logs.lines()
        .filter(line -> line.contains("[Retry attempt:") && !line.contains("[Retry attempt: 1/5]"))
        .count();

    System.out.println("\n=== Mixed Concurrent Operations Test Results ===");
    System.out.println("Total operations: " + totalOperations);
    System.out.println("Successful operations: " + successCount.get());
    System.out.println("Final quantity: " + finalStock.getQuantity() + " (expected: 1100)");
    System.out.println("Final reserved: " + finalStock.getReservedQuantity() + " (expected: 100)");
    System.out.println("Final available: " + finalStock.getAvailableQuantity() + " (expected: 1000)");
    System.out.println("Retry attempts detected: " + retryCount);
  }

  @Test
  void highContentionScenario_ShouldHandleMultipleRetriesSuccessfully(CapturedOutput output) throws Exception {
    // Given - Create stock with limited quantity to increase contention
    String stockId = createTestStock(testItemId, 200, "WAREHOUSE-A");
    int concurrentRequests = 20; // High number of concurrent requests
    int quantityPerRequest = 5;

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);
    ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);
    List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

    // When - Execute high-contention concurrent operations
    for (int i = 0; i < concurrentRequests; i++) {
      final int requestNum = i;
      executor.submit(() -> {
        try {
          startLatch.await();

          StockReserveRequest request = StockReserveRequest.builder()
              .quantity(quantityPerRequest)
              .referenceNumber("HIGH-CONTENTION-" + requestNum)
              .createdBy("customer-" + requestNum)
              .build();

          mockMvc.perform(post("/api/v1/stock/" + stockId + "/reserve")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk());

          successCount.incrementAndGet();

        } catch (Exception e) {
          failureCount.incrementAndGet();
          exceptions.add(e);
        } finally {
          completionLatch.countDown();
        }
      });
    }

    startLatch.countDown();
    boolean completed = completionLatch.await(60, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - Verify results
    assertThat(completed).isTrue();
    // Allow for some potential failures due to very high contention
    assertThat(successCount.get()).as("Most operations should succeed").isGreaterThanOrEqualTo(concurrentRequests - 1);
    assertThat(failureCount.get()).as("Minimal failures expected").isLessThanOrEqualTo(1);

    // Verify final stock state
    Stock finalStock = stockRepository.findById(stockId).orElseThrow();
    assertThat(finalStock.getReservedQuantity()).isEqualTo(concurrentRequests * quantityPerRequest);

    // Verify retry mechanism was invoked multiple times
    String logs = output.toString();
    assertThat(logs).containsPattern("\\[Retry attempt: 2/5\\]");
    assertThat(logs).containsPattern("\\[Retry attempt: 3/5\\]");

    // Count different retry levels
    long retry2Count = logs.lines().filter(line -> line.contains("[Retry attempt: 2/5]")).count();
    long retry3Count = logs.lines().filter(line -> line.contains("[Retry attempt: 3/5]")).count();
    long retry4Count = logs.lines().filter(line -> line.contains("[Retry attempt: 4/5]")).count();
    long retry5Count = logs.lines().filter(line -> line.contains("[Retry attempt: 5/5]")).count();

    System.out.println("\n=== High Contention Scenario Results ===");
    System.out.println("Concurrent requests: " + concurrentRequests);
    System.out.println("Success count: " + successCount.get());
    System.out.println("Failure count: " + failureCount.get());
    System.out.println("Retry attempts detected:");
    System.out.println("  - Attempt 2/5: " + retry2Count + " times");
    System.out.println("  - Attempt 3/5: " + retry3Count + " times");
    System.out.println("  - Attempt 4/5: " + retry4Count + " times");
    System.out.println("  - Attempt 5/5: " + retry5Count + " times");
    
    assertThat(retry2Count)
        .as("High contention should trigger multiple retry attempts")
        .isGreaterThan(0);
  }

  // ==================== HELPER METHODS ====================

  private String createTestStock(String itemId, int quantity, String location) {
    Stock stock = new Stock();
    stock.setItemId(itemId);
    stock.setQuantity(quantity);
    stock.setReservedQuantity(0);
    stock.setWarehouseLocation(location);
    return stockRepository.save(stock).getId();
  }

  private String reserveTestStockAndGetMovementId(String stockId, int quantity, String referenceNumber) throws Exception {
    StockReserveRequest request = StockReserveRequest.builder()
        .quantity(quantity)
        .referenceNumber(referenceNumber)
        .createdBy("test")
        .build();

    mockMvc.perform(post("/api/v1/stock/" + stockId + "/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Find the reservation movement by reference number
    var movements = stockMovementRepository.findByReferenceNumber(referenceNumber);
    assertThat(movements).isNotEmpty();
    return movements.get(0).getId();
  }
}

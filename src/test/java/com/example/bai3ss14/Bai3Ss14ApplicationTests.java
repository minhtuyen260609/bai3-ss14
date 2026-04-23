package com.example.bai3ss14;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.bai3ss14.dto.FlashSaleOrderRequest;
import com.example.bai3ss14.dto.FlashSaleOrderResponse;
import com.example.bai3ss14.dto.FlashSaleOrderResult;
import com.example.bai3ss14.entity.Product;
import com.example.bai3ss14.repository.FlashSaleOrderRepository;
import com.example.bai3ss14.repository.ProductRepository;
import com.example.bai3ss14.service.FlashSaleService;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Bai3Ss14ApplicationTests {

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FlashSaleOrderRepository flashSaleOrderRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        flashSaleOrderRepository.deleteAll();
        productRepository.deleteAll();

        Product product = productRepository.save(Product.builder()
                .sku("IPHONE15-FLASH")
                .name("iPhone 15 Flash Sale")
                .price(new BigDecimal("19990000"))
                .stock(5)
                .build());

        productId = product.getId();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldReturnInvalidRequestWhenQuantityIsMissing() {
        FlashSaleOrderResponse response = flashSaleService.placeOrder(
                new FlashSaleOrderRequest(productId, "customer-invalid", null));

        assertEquals(FlashSaleOrderResult.INVALID_REQUEST, response.getResult());
        assertEquals("quantity khong duoc de trong", response.getMessage());
    }

    @Test
    void shouldPreventOversellingDuringFlashSale() throws InterruptedException {
        int totalBuyers = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(totalBuyers);
        CountDownLatch readyLatch = new CountDownLatch(totalBuyers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalBuyers);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger soldOutCount = new AtomicInteger();

        for (int i = 0; i < totalBuyers; i++) {
            final int customerIndex = i;
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    FlashSaleOrderResponse response =
                            flashSaleService.placeOrder(new FlashSaleOrderRequest(productId, "customer-" + customerIndex, 1));

                    if (response.getResult() == FlashSaleOrderResult.SUCCESS) {
                        successCount.incrementAndGet();
                    } else if (response.getResult() == FlashSaleOrderResult.SOLD_OUT) {
                        soldOutCount.incrementAndGet();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executorService.shutdown();
        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));

        Product product = productRepository.findById(productId).orElseThrow();

        assertEquals(5, successCount.get());
        assertEquals(0, product.getStock());
        assertEquals(5, flashSaleOrderRepository.countByProduct_Id(productId));
        assertEquals(totalBuyers - successCount.get(), soldOutCount.get());
    }
}

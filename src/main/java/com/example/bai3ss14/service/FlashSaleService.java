package com.example.bai3ss14.service;

import com.example.bai3ss14.dto.FlashSaleOrderRequest;
import com.example.bai3ss14.dto.FlashSaleOrderResponse;
import com.example.bai3ss14.dto.ProductStockResponse;
import com.example.bai3ss14.entity.FlashSaleOrder;
import com.example.bai3ss14.entity.OrderStatus;
import com.example.bai3ss14.entity.Product;
import com.example.bai3ss14.repository.FlashSaleOrderRepository;
import com.example.bai3ss14.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final ProductRepository productRepository;
    private final FlashSaleOrderRepository flashSaleOrderRepository;
    private final EntityManager entityManager;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FlashSaleOrderResponse placeOrder(FlashSaleOrderRequest request) {
        FlashSaleOrderResponse invalidResponse = validateRequest(request);
        if (invalidResponse != null) {
            return invalidResponse;
        }

        Product product = productRepository.findByIdForUpdate(request.getProductId()).orElse(null);

        if (product == null) {
            return FlashSaleOrderResponse.notFound("Khong tim thay san pham flash sale");
        }

        if (product.getStock() < request.getQuantity()) {
            return FlashSaleOrderResponse.soldOut("Het hang", product.getStock());
        }

        product.setStock(product.getStock() - request.getQuantity());

        FlashSaleOrder order = flashSaleOrderRepository.save(FlashSaleOrder.builder()
                .product(product)
                .customerName(request.getCustomerName().trim())
                .quantity(request.getQuantity())
                .status(OrderStatus.CONFIRMED)
                .build());

        entityManager.flush();

        return FlashSaleOrderResponse.success(order.getId(), "Dat hang thanh cong", product.getStock());
    }

    @Transactional(readOnly = true)
    public Optional<ProductStockResponse> getProductStock(Long productId) {
        return productRepository.findById(productId)
                .map(this::toProductStockResponse);
    }

    private FlashSaleOrderResponse validateRequest(FlashSaleOrderRequest request) {
        if (request == null) {
            return FlashSaleOrderResponse.invalid("Du lieu dat hang khong duoc de trong");
        }

        if (request.getProductId() == null) {
            return FlashSaleOrderResponse.invalid("productId khong duoc de trong");
        }

        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            return FlashSaleOrderResponse.invalid("customerName khong duoc de trong");
        }

        if (request.getQuantity() == null) {
            return FlashSaleOrderResponse.invalid("quantity khong duoc de trong");
        }

        if (request.getQuantity() <= 0) {
            return FlashSaleOrderResponse.invalid("quantity phai lon hon 0");
        }

        return null;
    }

    private ProductStockResponse toProductStockResponse(Product product) {
        return new ProductStockResponse(product.getId(), product.getName(), product.getStock(), product.getVersion());
    }
}

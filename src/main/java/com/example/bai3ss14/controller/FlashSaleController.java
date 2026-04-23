package com.example.bai3ss14.controller;

import com.example.bai3ss14.dto.ApiErrorResponse;
import com.example.bai3ss14.dto.FlashSaleOrderRequest;
import com.example.bai3ss14.dto.FlashSaleOrderResult;
import com.example.bai3ss14.dto.FlashSaleOrderResponse;
import com.example.bai3ss14.service.FlashSaleService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flash-sale")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @PostMapping("/orders")
    public ResponseEntity<FlashSaleOrderResponse> placeOrder(@RequestBody(required = false) FlashSaleOrderRequest request) {
        FlashSaleOrderResponse response = flashSaleService.placeOrder(request);
        HttpStatus status = mapHttpStatus(response.getResult());
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductStock(@PathVariable Long productId) {
        return flashSaleService.getProductStock(productId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiErrorResponse("Khong tim thay san pham flash sale", LocalDateTime.now())));
    }

    private HttpStatus mapHttpStatus(FlashSaleOrderResult result) {
        return switch (result) {
            case SUCCESS -> HttpStatus.CREATED;
            case SOLD_OUT -> HttpStatus.CONFLICT;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
        };
    }
}

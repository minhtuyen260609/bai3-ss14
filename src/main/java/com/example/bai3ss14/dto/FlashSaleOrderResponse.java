package com.example.bai3ss14.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FlashSaleOrderResponse {

    private FlashSaleOrderResult result;
    private Long orderId;
    private String message;
    private Integer remainingStock;

    public static FlashSaleOrderResponse success(Long orderId, String message, Integer remainingStock) {
        return FlashSaleOrderResponse.builder()
                .result(FlashSaleOrderResult.SUCCESS)
                .orderId(orderId)
                .message(message)
                .remainingStock(remainingStock)
                .build();
    }

    public static FlashSaleOrderResponse soldOut(String message, Integer remainingStock) {
        return FlashSaleOrderResponse.builder()
                .result(FlashSaleOrderResult.SOLD_OUT)
                .message(message)
                .remainingStock(remainingStock)
                .build();
    }

    public static FlashSaleOrderResponse notFound(String message) {
        return FlashSaleOrderResponse.builder()
                .result(FlashSaleOrderResult.NOT_FOUND)
                .message(message)
                .build();
    }

    public static FlashSaleOrderResponse invalid(String message) {
        return FlashSaleOrderResponse.builder()
                .result(FlashSaleOrderResult.INVALID_REQUEST)
                .message(message)
                .build();
    }
}

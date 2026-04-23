package com.example.bai3ss14.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductStockResponse {

    private Long productId;
    private String productName;
    private Integer stock;
    private Long version;
}

package com.example.bai3ss14.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleOrderRequest {

    private Long productId;

    private String customerName;

    private Integer quantity;
}

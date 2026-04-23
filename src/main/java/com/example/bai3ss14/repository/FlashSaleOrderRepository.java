package com.example.bai3ss14.repository;

import com.example.bai3ss14.entity.FlashSaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashSaleOrderRepository extends JpaRepository<FlashSaleOrder, Long> {

    long countByProduct_Id(Long productId);
}

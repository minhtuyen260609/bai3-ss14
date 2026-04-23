package com.example.bai3ss14.config;

import com.example.bai3ss14.entity.Product;
import com.example.bai3ss14.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlashSaleDataInitializer {

    @Bean
    CommandLineRunner seedFlashSaleProduct(ProductRepository productRepository) {
        return args -> productRepository.findBySku("IPHONE15-FLASH").orElseGet(() -> productRepository.save(Product.builder()
                .sku("IPHONE15-FLASH")
                .name("iPhone 15 Flash Sale")
                .price(new BigDecimal("19990000"))
                .stock(5)
                .build()));
    }
}

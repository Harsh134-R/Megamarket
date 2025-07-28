package com.ecommerce.Megamarket.repository;

import com.ecommerce.Megamarket.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
package com.ecommerce.Megamarket.repository;

import com.ecommerce.Megamarket.model.Order;
import com.ecommerce.Megamarket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}

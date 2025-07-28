package com.ecommerce.Megamarket.repository;

import com.ecommerce.Megamarket.model.Cart;
import com.ecommerce.Megamarket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}

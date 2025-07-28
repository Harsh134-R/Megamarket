package com.ecommerce.Megamarket.controller;


import com.ecommerce.Megamarket.model.Cart;
import com.ecommerce.Megamarket.model.CartItem;
import com.ecommerce.Megamarket.model.Product;
import com.ecommerce.Megamarket.model.User;
import com.ecommerce.Megamarket.repository.CartRepository;
import com.ecommerce.Megamarket.repository.ProductRepository;
import com.ecommerce.Megamarket.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartController(CartRepository cartRepository, UserRepository userRepository,
                          ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return cartRepository.findByUser(user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> updateCart(@RequestBody List<CartRequest> cartRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return newCart;
                });

        // Load existing items
        List<CartItem> existingItems = cart.getItems() != null ? cart.getItems() : new ArrayList<>();

        // Map to track productId -> CartItem
        java.util.Map<Long, CartItem> productToItem = new java.util.HashMap<>();
        for (CartItem item : existingItems) {
            productToItem.put(item.getProduct().getId(), item);
        }

        // Build a set of product IDs from the request
        java.util.Set<Long> requestedProductIds = cartRequest.stream()
                .map(CartRequest::getProductId)
                .collect(java.util.stream.Collectors.toSet());

        // Remove any CartItems not present in the request
        existingItems.removeIf(item -> !requestedProductIds.contains(item.getProduct().getId()));

        for (CartRequest request : cartRequest) {
            Product product = productRepository.findById(request.getProductId()).orElseThrow();

            CartItem existingItem = productToItem.get(product.getId());

            if (request.getQuantity() <= 0) {
                // Remove item if quantity is zero or less
                if (existingItem != null) {
                    existingItems.remove(existingItem);
                    productToItem.remove(product.getId());
                }
            } else if (existingItem != null) {
                // Set quantity to the value in the request (not add)
                existingItem.setQuantity(request.getQuantity());
            } else {
                // Add new item
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(request.getQuantity());
                existingItems.add(newItem);
                productToItem.put(product.getId(), newItem);
            }
        }

        cart.setItems(existingItems);
        cartRepository.save(cart);
        return ResponseEntity.ok("Cart updated successfully");
    }

    public static class CartRequest {
        private Long productId;
        private Integer quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}

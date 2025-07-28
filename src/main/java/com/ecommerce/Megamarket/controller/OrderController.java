package com.ecommerce.Megamarket.controller;


import com.ecommerce.Megamarket.model.Order;
import com.ecommerce.Megamarket.model.OrderItem;
import com.ecommerce.Megamarket.model.Product;
import com.ecommerce.Megamarket.model.User;
import com.ecommerce.Megamarket.repository.OrderRepository;
import com.ecommerce.Megamarket.repository.ProductRepository;
import com.ecommerce.Megamarket.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository,
                           ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest orderRequest) {


        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            logger.info("Placing order for user: {}", email);
            User user = userRepository.findByEmail(email).orElseThrow();

            logger.info("OrderRequest: items={}, shippingAddress={}, total={}", orderRequest.getItems(), orderRequest.getShippingAddress(), orderRequest.getTotal());




            Order order = new Order();
            order.setUser(user);
            order.setDate(LocalDateTime.now());
            order.setTotal(orderRequest.getTotal());
            order.setShippingAddress(orderRequest.getShippingAddress());

            List<OrderItem> items = new ArrayList<>();
            for (OrderRequest.Item item : orderRequest.getItems()) {
                Product product = productRepository.findById(item.getProductId()).orElseThrow();
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(product.getPrice());
                items.add(orderItem);
            }
            order.setItems(items);
            orderRepository.save(order);
            logger.info("Order saved successfully for user: {} with order ID: {}", email, order.getId());
            return ResponseEntity.ok("Order placed successfully");
        } catch (Exception e) {
            logger.error("Error placing order: ", e);
            return ResponseEntity.status(500).body("Error placing order: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Order> getUserOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return orderRepository.findByUser(user);
    }

    @PutMapping("/{orderId}/address")
    public ResponseEntity<Order> updateOrderAddress(@PathVariable Long orderId, @RequestBody String newAddress) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        order.setShippingAddress(newAddress);
        return ResponseEntity.ok(orderRepository.save(order));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        orderRepository.delete(order);
        return ResponseEntity.ok().build();
    }

    public static class OrderRequest {
        private List<Item> items;
        private String shippingAddress;
        private Double total;

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public Double getTotal() {
            return total;
        }

        public void setTotal(Double total) {
            this.total = total;
        }

        public static class Item {
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
}






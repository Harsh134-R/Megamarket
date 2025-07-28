package com.ecommerce.Megamarket.controller;

import com.ecommerce.Megamarket.service.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            Long amount = Long.parseLong(request.get("amount").toString());
            String currency = (String) request.get("currency");

            if (currency == null) {
                currency = "usd";
            }

            Map<String, Object> response = stripeService.createPaymentIntentResponse(amount, currency);

            if (response.containsKey("error")) {
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody Map<String, Object> request) {
        try {
            String paymentIntentId = (String) request.get("paymentIntentId");
            String status = (String) request.get("status");

            Map<String, Object> response = Map.of(
                    "success", "success".equals(status),
                    "paymentIntentId", paymentIntentId,
                    "message", "Payment processed successfully"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

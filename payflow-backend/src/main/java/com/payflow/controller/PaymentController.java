package com.payflow.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payflow.service.RazorpayService;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final RazorpayService razorpayService;

    public PaymentController(RazorpayService razorpayService) {
        this.razorpayService = razorpayService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder() {
        try {
            String orderId = razorpayService.createTestOrder();

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "orderId", orderId,
                "keyId", razorpayService.getKeyId(),
                "amount", 49900,
                "currency", "INR"
            ));

        } catch (Exception e) {
            System.err.println("Razorpay order creation failed: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                Map.of(
                    "status", "error",
                    "message", "Unable to create Razorpay order"
                )
            );
        }
    }
}
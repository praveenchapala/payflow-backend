
package com.payflow.controller;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payflow.service.RazorpayService;
import com.razorpay.Payment;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(
    origins = "${PAYFLOW_FRONTEND_URL:http://localhost:5173}"
)
public class PaymentController {

    private static final int EXPECTED_AMOUNT = 49900;
    private static final String EXPECTED_CURRENCY = "INR";

    private final RazorpayService razorpayService;

    // Temporary demo storage. Replace with a database later.
    private final Set<String> createdOrderIds =
        ConcurrentHashMap.newKeySet();

    private final Set<String> verifiedOrderIds =
        ConcurrentHashMap.newKeySet();

    public PaymentController(RazorpayService razorpayService) {
        this.razorpayService = razorpayService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder() {
        try {
            String orderId = razorpayService.createTestOrder();

            createdOrderIds.add(orderId);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "orderId", orderId,
                "keyId", razorpayService.getKeyId(),
                "amount", EXPECTED_AMOUNT,
                "currency", EXPECTED_CURRENCY
            ));

        } catch (Exception e) {
            System.err.println(
                "Razorpay order creation failed: " + e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                Map.of(
                    "status", "error",
                    "message", "Unable to create Razorpay order"
                )
            );
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestBody VerifyPaymentRequest request) {

        if (request == null
                || request.razorpay_order_id() == null
                || request.razorpay_payment_id() == null
                || request.razorpay_signature() == null
                || request.razorpay_order_id().isBlank()
                || request.razorpay_payment_id().isBlank()
                || request.razorpay_signature().isBlank()) {

            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Required payment details are missing"
            ));
        }

        String orderId = request.razorpay_order_id();
        String paymentId = request.razorpay_payment_id();

        // Accept only orders created by this running backend.
        if (!createdOrderIds.contains(orderId)) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Order was not created by this PayFlow session"
            ));
        }

        if (verifiedOrderIds.contains(orderId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "error",
                "message", "This order has already been verified"
            ));
        }

        try {
            // Check 1: Verify the HMAC signature.
            boolean validSignature =
                razorpayService.verifyPaymentSignature(
                    orderId,
                    paymentId,
                    request.razorpay_signature()
                );

            if (!validSignature) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid payment signature"
                ));
            }

            // Check 2: Fetch authoritative payment details from Razorpay.
            Payment payment = razorpayService.fetchPayment(paymentId);

            Object orderIdValue = payment.get("order_id");
            Object statusValue = payment.get("status");
            Object currencyValue = payment.get("currency");
            Object amountValue = payment.get("amount");

            String razorpayOrderId =
                orderIdValue == null ? null : orderIdValue.toString();

            String paymentStatus =
                statusValue == null ? null : statusValue.toString();

            String currency =
                currencyValue == null ? null : currencyValue.toString();

            if (!(amountValue instanceof Number number)) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    Map.of(
                        "status", "error",
                        "message", "Razorpay returned an invalid payment amount"
                    )
                );
            }

            int amount = number.intValue();

            // Check 3: Verify order, amount and currency.
            if (!orderId.equals(razorpayOrderId)
                    || amount != EXPECTED_AMOUNT
                    || !EXPECTED_CURRENCY.equals(currency)) {

                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message",
                    "Payment order, amount, or currency does not match"
                ));
            }

            // A valid signature does not necessarily mean payment is captured.
            if (!"captured".equals(paymentStatus)) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "status", "pending",
                    "message",
                    "Payment is not captured yet. Current status: "
                        + paymentStatus,
                    "orderId", orderId,
                    "paymentId", paymentId
                ));
            }

            // Mark the order verified only after every check succeeds.
            if (!verifiedOrderIds.add(orderId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "status", "error",
                    "message", "This order has already been verified"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "status", "verified",
                "message", "Payment captured and verified successfully",
                "orderId", orderId,
                "paymentId", paymentId,
                "amount", amount,
                "currency", currency
            ));

        } catch (Exception e) {
            // Log the exception for diagnosis; don't expose internal details.
            System.err.println(
                "Payment verification failed: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "status", "error",
                "message",
                "Unable to verify payment with Razorpay. Please retry."
            ));
        }
    }
}


package com.payflow.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public String createTestOrder() throws Exception {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject options = new JSONObject();
        options.put("amount", 49900);
        options.put("currency", "INR");
        options.put(
            "receipt",
            "payflow_receipt_" + System.currentTimeMillis()
        );

        Order order = client.orders.create(options);
        return order.get("id");
    }

    public String getKeyId() {
        return keyId;
    }

    public boolean verifyPaymentSignature(
            String orderId,
            String paymentId,
            String receivedSignature
    ) throws Exception {

        if (orderId == null || orderId.isBlank()
                || paymentId == null || paymentId.isBlank()
                || receivedSignature == null
                || receivedSignature.isBlank()) {
            return false;
        }

        String payload = orderId + "|" + paymentId;

        Mac mac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(
            keySecret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );

        mac.init(secretKey);

        byte[] hash = mac.doFinal(
            payload.getBytes(StandardCharsets.UTF_8)
        );

        String expectedSignature = HexFormat.of().formatHex(hash);

        return MessageDigest.isEqual(
            expectedSignature.getBytes(StandardCharsets.US_ASCII),
            receivedSignature.getBytes(StandardCharsets.US_ASCII)
        );
    }

    // Fetch the payment directly from Razorpay's API.
    public Payment fetchPayment(String paymentId) throws Exception {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        return client.payments.fetch(paymentId);
    }
}

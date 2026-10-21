package com.payflow.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
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
        options.put("receipt", "payflow_receipt_" + System.currentTimeMillis());

        Order order = client.orders.create(options);
        return order.get("id");
    }

    public String getKeyId() {
        return keyId;
    }
}
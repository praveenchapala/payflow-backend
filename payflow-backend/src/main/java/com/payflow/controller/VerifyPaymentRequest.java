package com.payflow.controller;

public record VerifyPaymentRequest(
    String razorpay_order_id,
    String razorpay_payment_id,
    String razorpay_signature
) {
}
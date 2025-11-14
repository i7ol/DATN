package com.datn.shoppayment.service;

import com.datn.shoppayment.client.OrderClient;
import com.datn.shoppayment.dto.request.PaymentRequest;
import com.datn.shoppayment.dto.request.UpdateOrderPaymentRequest;
import com.datn.shoppayment.dto.response.OrderResponse;
import com.datn.shoppayment.dto.response.PaymentResponse;
import com.datn.shoppayment.entity.Payment;
import com.datn.shoppayment.enums.PaymentMethod;
import com.datn.shoppayment.enums.PaymentStatus;
import com.datn.shoppayment.mapper.PaymentStatusMapper;
import com.datn.shoppayment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    @Transactional
    public Payment createPayment(Long orderId, PaymentMethod method) {
        // Lấy thông tin order qua REST (nếu cần lấy amount)
        OrderResponse order = orderClient.getOrder(orderId);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(order.totalPrice())
                .method(method)
                .status(PaymentStatus.PENDING)
                .build();

        Payment saved = paymentRepository.save(payment);

        return saved;
    }

    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(status);
        if (status == PaymentStatus.SUCCESS) {
            payment.setPaidAt(LocalDateTime.now());
        }
        Payment updated = paymentRepository.save(payment);

        // Notify order service
        String orderPaymentStatus = PaymentStatusMapper.toOrderPaymentStatus(status);
        orderClient.updateOrderPayment(payment.getOrderId(), orderPaymentStatus);

        return updated;
    }

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    // Simulate processing external payment gateway
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // --- here you would integrate real gateway ---
        // For demo, mark as SUCCESS
        Payment payment = createPayment(request.orderId(), PaymentMethod.valueOf(request.method()));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Notify order
        String orderStatus = PaymentStatusMapper.toOrderPaymentStatus(PaymentStatus.SUCCESS);
        orderClient.updateOrderPayment(payment.getOrderId(), orderStatus);

        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getStatus());
    }
}

//package com.datn.shoppayment.service;
//
//import com.datn.shopdatabase.entity.PaymentEntity;
//import com.datn.shopdatabase.enums.PaymentMethod;
//import com.datn.shopdatabase.enums.PaymentStatus;
//
//import com.datn.shoppayment.client.OrderClient;
//import com.datn.shoppayment.dto.request.PaymentRequest;
//import com.datn.shoppayment.dto.response.OrderResponse;
//import com.datn.shoppayment.dto.response.PaymentResponse;
//
//import com.datn.shopdatabase.repository.PaymentRepository;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//public class PaymentService {
//
//    private final PaymentRepository paymentRepository;
//    private final OrderClient orderClient;
//
//    // -------------------------------------------------
//    // TẠO PAYMENT TỪ REQUEST JSON
//    // -------------------------------------------------
//    @Transactional
//    public PaymentResponse createNewPayment(PaymentRequest req) {
//
//        OrderResponse order = orderClient.getOrder(req.orderId());
//
//        PaymentEntity payment = PaymentEntity.builder()
//                .orderId(req.orderId())
//                .amount(order.totalPrice())
//                .method(PaymentMethod.valueOf(req.method()))
//                .status(PaymentStatus.PENDING)
//                .build();
//
//        paymentRepository.save(payment);
//
//        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getStatus());
//    }
//
//    // -------------------------------------------------
//    // CẬP NHẬT STATUS
//    // -------------------------------------------------
//    @Transactional
//    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus status) {
//
//        PaymentEntity payment = paymentRepository.findById(paymentId)
//                .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//        payment.setStatus(status);
//
//        if (status == PaymentStatus.PAID) {
//            payment.setPaidAt(LocalDateTime.now());
//        }
//
//        paymentRepository.save(payment);
//
//        // Sync sang Order Service
//        orderClient.updateOrderPayment(payment.getOrderId(), status.name());
//
//        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getStatus());
//    }
//
//    // -------------------------------------------------
//    // LẤY PAYMENT
//    // -------------------------------------------------
//    public PaymentResponse getPaymentResponse(Long paymentId) {
//        PaymentEntity p = paymentRepository.findById(paymentId)
//                .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//        return new PaymentResponse(p.getId(), p.getOrderId(), p.getStatus());
//    }
//
//    // -------------------------------------------------
//    // PROCESS PAYMENT
//    // -------------------------------------------------
//    @Transactional
//    public PaymentResponse processPayment(PaymentRequest request) {
//
//        // tạo payment PENDING
//        OrderResponse order = orderClient.getOrder(request.orderId());
//
//        PaymentEntity payment = PaymentEntity.builder()
//                .orderId(order.id())
//                .amount(order.totalPrice())
//                .method(PaymentMethod.valueOf(request.method()))
//                .status(PaymentStatus.PENDING)
//                .build();
//
//        paymentRepository.save(payment);
//
//        // chuyển sang PAID
//        payment.setStatus(PaymentStatus.PAID);
//        payment.setPaidAt(LocalDateTime.now());
//        paymentRepository.save(payment);
//
//        // update Order
//        orderClient.updateOrderPayment(payment.getOrderId(), "PAID");
//
//        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getStatus());
//    }
//}

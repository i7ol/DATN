package com.datn.shoppayment.service;

import com.datn.shopclient.client.OrderInternalClient;
import com.datn.shopdatabase.entity.PaymentEntity;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.request.GuestPaymentRequest;
import com.datn.shopobject.dto.request.UserPaymentRequest;
import com.datn.shoppayment.repository.PaymentRepository;
import com.datn.shopobject.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderInternalClient orderClient;
    private final VNPayService vnPayService;

    @Override
    public PaymentResponse createUserPayment(
            UserPaymentRequest request,
            Long userId,
            String ipAddress
    ) {
        OrderResponse order = orderClient.getOrder(request.getOrderId());

        if (!userId.equals(order.getUserId())) {
            throw new RuntimeException("Order does not belong to user");
        }

        return createPayment(order, userId, null, request.getMethod(), ipAddress);
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(String txnRef) {

        PaymentEntity payment = paymentRepository
                .findByTransactionId(txnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);

        orderClient.updatePaymentStatus(
                payment.getOrderId(),
                PaymentStatus.PAID
        );
    }

    @Override
    @Transactional
    public void handlePaymentFail(String txnRef) {

        PaymentEntity payment = paymentRepository
                .findByTransactionId(txnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);

        orderClient.updatePaymentStatus(
                payment.getOrderId(),
                PaymentStatus.FAILED
        );
    }
    @Override
    public PaymentResponse createGuestPayment(
            GuestPaymentRequest request,
            String ipAddress
    ) {
        OrderResponse order = orderClient.getOrder(request.getOrderId());

        if (order.getUserId() != null ||
                !request.getGuestId().equals(order.getGuestId())) {
            throw new RuntimeException("Order does not belong to guest");
        }

        return createPayment(order, null, request.getGuestId(), request.getMethod(), ipAddress);
    }

    private PaymentResponse createPayment(
            OrderResponse order,
            Long userId,
            String guestId,
            String method,
            String ip
    ) {
        PaymentMethod pm;

        try {
            pm = PaymentMethod.valueOf(method.trim().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Unsupported payment method");
        }

        PaymentEntity payment = paymentRepository.save(
                PaymentEntity.builder()
                        .orderId(order.getId())
                        .userId(userId)
                        .guestId(guestId)
                        .amount(order.getFinalAmount())
                        .method(pm)
                        .status(PaymentStatus.PENDING)
                        .build()
        );

        if (pm == PaymentMethod.VNPAY) {

            VNPayPaymentResponse vnp =
                    vnPayService.createPayment(
                            order.getId(),
                            order.getFinalAmount().multiply(BigDecimal.valueOf(1)).longValue(),
                            "Payment order #" + order.getId(),
                            ip
                    );

            payment.setTransactionId(vnp.getTransactionId());
            paymentRepository.save(payment);

            orderClient.updatePaymentStatus(
                    order.getId(),
                    PaymentStatus.PENDING
            );


            PaymentResponse res = PaymentResponse.from(payment);
            res.setPaymentUrl(vnp.getPaymentUrl());
            return res;
        }

        return PaymentResponse.from(payment);
    }

    @Override
    public Page<PaymentResponse> getAllPayments(
            PaymentStatus status,
            PaymentMethod method,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    ) {
        return paymentRepository
                .findAll(
                        PaymentSpecification.filter(
                                status, method, fromDate, toDate,
                                minAmount, maxAmount
                        ),
                        pageable
                )
                .map(PaymentResponse::from);
    }

    @Override
    public PaymentResponse getById(Long id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse markPaid(Long id, String transactionId) {

        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(transactionId);

        PaymentEntity saved = paymentRepository.save(payment);


        orderClient.updatePaymentStatus(
                payment.getOrderId(),
                PaymentStatus.PAID
        );

        return PaymentResponse.from(saved);
    }

    @Override
    public PaymentResponse refund(Long id, String reason) {

        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);

        PaymentEntity saved = paymentRepository.save(payment);

        orderClient.updatePaymentStatus(
                payment.getOrderId(),
                PaymentStatus.REFUNDED
        );

        return PaymentResponse.from(saved);
    }
    @Override
    public PaymentResponse cancel(Long id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Cannot cancel paid payment");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Override
    public Object getSummary(LocalDate fromDate, LocalDate toDate) {

        BigDecimal totalPaid = paymentRepository
                .getTotalAmountByPeriodAndStatus(fromDate, toDate, PaymentStatus.PAID);

        long totalPayments = paymentRepository.count();

        return Map.of(
                "totalPayments", totalPayments,
                "totalPaidAmount", totalPaid == null ? BigDecimal.ZERO : totalPaid
        );
    }

}

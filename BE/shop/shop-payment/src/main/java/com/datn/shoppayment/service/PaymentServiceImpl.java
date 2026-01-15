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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
                            order.getFinalAmount().longValue(),
                            "Payment order #" + order.getId(),
                            ip
                    );

            payment.setTransactionId(vnp.getTransactionId());
            paymentRepository.save(payment);

            orderClient.updatePaymentStatus(
                    order.getId(),
                    PaymentStatus.PENDING.name()
            );


            PaymentResponse res = PaymentResponse.from(payment);
            res.setPaymentUrl(vnp.getPaymentUrl());
            return res;
        }

        return PaymentResponse.from(payment);
    }
}

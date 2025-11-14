package com.datn.shopshipping.service;

import com.datn.shopshipping.dto.request.ShippingRequest;
import com.datn.shopshipping.entity.ShippingOrder;
import com.datn.shopshipping.repository.ShippingRepository;
import com.datn.shoporder.entity.Order;
import com.datn.shoporder.repository.OrderRepository;
import com.datn.shopcore.entity.User;
import com.datn.shopcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ShippingOrder create(ShippingRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id " + request.getOrderId()));

        ShippingOrder shippingOrder = ShippingOrder.builder()
                .orderId(order.getId())
                .shippingCompany(request.getShippingCompany())
                .shippingMethod(request.getShippingMethod())
                .trackingNumber(request.getTrackingNumber())
                .shippingFee(request.getShippingFee())
                .status(ShippingOrder.Status.PENDING)
                .build();

        // Lấy thông tin recipient từ order
        if (order.getUserId() != null) {
            User user = userRepository.findById(order.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id " + order.getUserId()));
            shippingOrder.setRecipientName(user.getUsername());
            shippingOrder.setRecipientPhone(user.getPhone());
            shippingOrder.setRecipientAddress(order.getShippingAddress());
        } else {
            shippingOrder.setRecipientName(order.getGuestName());
            shippingOrder.setRecipientPhone(order.getGuestPhone());
            shippingOrder.setRecipientAddress(order.getShippingAddress());
        }

        return shippingRepository.save(shippingOrder);
    }

    public ShippingOrder updateStatus(Long id, ShippingOrder.Status status) {
        ShippingOrder shippingOrder = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping order not found with id " + id));
        shippingOrder.setStatus(status);
        return shippingRepository.save(shippingOrder);
    }

    public ShippingOrder get(Long id) {
        return shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping order not found with id " + id));
    }

    public List<ShippingOrder> getByOrderId(Long orderId) {
        return shippingRepository.findByOrderId(orderId);
    }
}

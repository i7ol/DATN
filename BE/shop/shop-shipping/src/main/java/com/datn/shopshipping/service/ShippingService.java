package com.datn.shopshipping.service;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.entity.ShippingOrderEntity;
import com.datn.shopdatabase.entity.UserEntity;
import com.datn.shopobject.dto.request.ShippingRequest;
import com.datn.shopdatabase.repository.ShippingRepository;
import com.datn.shopdatabase.repository.OrderRepository;
import com.datn.shopdatabase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private  ShippingRepository shippingRepository;
    private  OrderRepository orderRepository;
    private  UserRepository userRepository;

    public ShippingOrderEntity create(ShippingRequest request) {
        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id " + request.getOrderId()));

        ShippingOrderEntity shippingOrder = ShippingOrderEntity.builder()
                .orderId(order.getId())
                .shippingCompany(request.getShippingCompany())
                .shippingMethod(request.getShippingMethod())
                .trackingNumber(request.getTrackingNumber())
                .shippingFee(request.getShippingFee())
                .status(ShippingOrderEntity.Status.PENDING)
                .build();

        // Lấy thông tin recipient từ order
        if (order.getUserId() != null) {
            UserEntity user = userRepository.findById(order.getUserId())
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

    public ShippingOrderEntity updateStatus(Long id, ShippingOrderEntity.Status status) {
        ShippingOrderEntity shippingOrder = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping order not found with id " + id));
        shippingOrder.setStatus(status);
        return shippingRepository.save(shippingOrder);
    }

    public ShippingOrderEntity get(Long id) {
        return shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping order not found with id " + id));
    }

    public List<ShippingOrderEntity> getByOrderId(Long orderId) {
        return shippingRepository.findByOrderId(orderId);
    }
}

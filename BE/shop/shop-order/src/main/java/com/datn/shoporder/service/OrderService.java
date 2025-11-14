package com.datn.shoporder.service;

import com.datn.shopcore.exception.AppException;
import com.datn.shopcore.exception.ErrorCode;
import com.datn.shoporder.dto.request.CreateOrderRequest;
import com.datn.shoporder.dto.request.OrderItemRequest;
import com.datn.shoporder.entity.Order;
import com.datn.shoporder.entity.OrderItem;
import com.datn.shoporder.enums.OrderStatus;
import com.datn.shoporder.enums.PaymentStatus;
import com.datn.shoporder.repository.OrderRepository;
import com.datn.shopproduct.entity.Product;
import com.datn.shopproduct.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class OrderService {

      OrderRepository orderRepository;
      ProductRepository productRepository;


    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();

        // Nếu userId != null → khách đăng nhập
        if (request.getUserId() != null) {
            order.setUserId(request.getUserId());
        } else {
            // Guest checkout
            order.setGuestName(request.getGuestName());
            order.setGuestEmail(request.getGuestEmail());
            order.setGuestPhone(request.getGuestPhone());
            order.setShippingAddress(request.getShippingAddress());
            order.setBillingAddress(request.getBillingAddress());
        }

        // Tạo OrderItem
        List<OrderItem> items = request.getItems().stream().map(dto -> {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setPrice(product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
            item.setQuantity(dto.getQuantity());
            return item;
        }).collect(Collectors.toList());

        BigDecimal totalPrice = items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(items);
        items.forEach(i -> i.setOrder(order));
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.NEW);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserById(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public Order updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        Order order = getOrder(orderId);
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public void updatePaymentStatus(Long orderId, String paymentStatusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        PaymentStatus status = PaymentStatus.valueOf(paymentStatusStr);

        order.setPaymentStatus(status);
        orderRepository.save(order);
    }
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    private Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> getGuestOrders() {
        return orderRepository.findByUserIdIsNull();
    }

}

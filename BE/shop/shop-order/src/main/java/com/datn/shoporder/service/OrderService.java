package com.datn.shoporder.service;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.entity.OrderItemEntity;
import com.datn.shopdatabase.entity.ProductEntity;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopdatabase.repository.OrderRepository;
import com.datn.shopdatabase.repository.ProductRepository;
import com.datn.shopobject.dto.request.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private  OrderRepository orderRepository;
    private  ProductRepository productRepository;

    // ---------------------------------------------------
    // TẠO ĐƠN HÀNG
    // ---------------------------------------------------
    @Transactional
    public OrderEntity createOrder(CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();

        if (request.getUserId() != null) {
            order.setUserId(request.getUserId());
        } else {
            order.setGuestName(request.getGuestName());
            order.setGuestEmail(request.getGuestEmail());
            order.setGuestPhone(request.getGuestPhone());
            order.setShippingAddress(request.getShippingAddress());
            order.setBillingAddress(request.getBillingAddress());
        }

        // Tạo danh sách item
        List<OrderItemEntity> items = request.getItems().stream().map(dto -> {
            ProductEntity product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            OrderItemEntity item = new OrderItemEntity();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(dto.getQuantity());
            item.setPrice(product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
            item.setOrder(order);
            return item;
        }).collect(Collectors.toList());

        // Tổng tiền
        BigDecimal total = items.stream().map(OrderItemEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(items);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.NEW);
        order.setPaymentStatus(PaymentStatus.PENDING);

        return orderRepository.save(order);
    }

    // ---------------------------------------------------
    // UPDATE STATUS
    // ---------------------------------------------------
    @Transactional
    public OrderEntity updateStatus(Long orderId, OrderStatus status) {
        OrderEntity order = getOrder(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity updatePaymentStatus(Long orderId, PaymentStatus status) {
        OrderEntity order = getOrder(orderId);
        order.setPaymentStatus(status);
        return orderRepository.save(order);
    }

    // Khi PaymentService gọi (bằng String)
    @Transactional
    public void updatePaymentStatus(Long orderId, String paymentStatus) {
        OrderEntity order = getOrder(orderId);
        order.setPaymentStatus(PaymentStatus.valueOf(paymentStatus));
        orderRepository.save(order);
    }

    // ---------------------------------------------------
    // GET
    // ---------------------------------------------------
    public Optional<OrderEntity> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    private OrderEntity getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    public List<OrderEntity> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<OrderEntity> getGuestOrders() {
        return orderRepository.findByUserIdIsNull();
    }
}

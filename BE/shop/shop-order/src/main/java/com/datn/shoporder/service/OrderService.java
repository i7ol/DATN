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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // =============================================
    // CREATE ORDER (FROM CHECKOUT REQUEST)
    // =============================================
    @Transactional
    public OrderEntity createOrderFromCheckout(
            Long userId,
            String guestName,
            String guestEmail,
            String guestPhone,
            String shippingAddress,
            String shippingProvince,
            String shippingDistrict,
            String shippingWard,
            String shippingNote,
            String billingAddress,
            String billingProvince,
            String billingDistrict,
            String billingWard,
            List<OrderItemEntity> items
    ) {
        OrderEntity order = new OrderEntity();

        if (userId != null) {
            order.setUserId(userId);
        } else {
            order.setGuestName(guestName);
            order.setGuestEmail(guestEmail);
            order.setGuestPhone(guestPhone);
        }

        // Set shipping address details
        order.setShippingAddress(shippingAddress);
        order.setShippingProvince(shippingProvince);
        order.setShippingDistrict(shippingDistrict);
        order.setShippingWard(shippingWard);
        order.setShippingNote(shippingNote);

        // Set billing address (use shipping if not provided)
        if (billingAddress != null) {
            order.setBillingAddress(billingAddress);
            order.setBillingProvince(billingProvince);
            order.setBillingDistrict(billingDistrict);
            order.setBillingWard(billingWard);
        } else {
            order.setBillingAddress(shippingAddress);
            order.setBillingProvince(shippingProvince);
            order.setBillingDistrict(shippingDistrict);
            order.setBillingWard(shippingWard);
        }

        // Set items
        items.forEach(item -> {
            item.setOrder(order);
            order.addItem(item);
        });

        // Calculate prices
        BigDecimal total = items.stream()
                .map(OrderItemEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.calculateFinalAmount();

        // Set status
        order.setStatus(OrderStatus.NEW);
        order.setPaymentStatus(PaymentStatus.PENDING);

        return orderRepository.save(order); // createdAt, updatedAt tự động set
    }

    // =============================================
    // CREATE ORDER (FROM CREATE ORDER REQUEST)
    // =============================================
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

        // Create items
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

        // Calculate prices
        BigDecimal total = items.stream().map(OrderItemEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(items);
        order.setTotalPrice(total);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.calculateFinalAmount();

        order.setStatus(OrderStatus.NEW);
        order.setPaymentStatus(PaymentStatus.PENDING);

        return orderRepository.save(order);
    }

    // =============================================
    // UPDATE METHODS
    // =============================================
    @Transactional
    public OrderEntity updateStatus(Long orderId, OrderStatus status) {
        OrderEntity order = getOrder(orderId);

        validateStatusTransition(order.getStatus(), status);
        order.setStatus(status);

        return orderRepository.save(order); // updatedAt tự động
    }

    @Transactional
    public OrderEntity updatePaymentStatus(Long orderId, PaymentStatus status) {
        OrderEntity order = getOrder(orderId);
        order.setPaymentStatus(status);

        if (status == PaymentStatus.PAID) {
            order.setStatus(OrderStatus.PROCESSING);
            order.setPaymentDate(Instant.now());
        } else if (status == PaymentStatus.REFUNDED) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity updateOrderDetails(Long orderId, BigDecimal shippingFee, BigDecimal discountAmount) {
        OrderEntity order = getOrder(orderId);

        if (shippingFee != null) {
            order.setShippingFee(shippingFee);
        }

        if (discountAmount != null) {
            order.setDiscountAmount(discountAmount);
        }

        order.calculateFinalAmount();
        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity updateShippingInfo(Long orderId, String shippingMethod, Instant estimatedDeliveryDate) {
        OrderEntity order = getOrder(orderId);

        order.setShippingMethod(shippingMethod);
        order.setEstimatedDeliveryDate(estimatedDeliveryDate);

        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity markAsDelivered(Long orderId) {
        OrderEntity order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Chỉ có thể đánh dấu đã giao hàng khi đơn hàng đang vận chuyển");
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setActualDeliveryDate(Instant.now());

        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity completeOrder(Long orderId) {
        OrderEntity order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Chỉ có thể hoàn thành đơn hàng khi đã giao hàng");
        }

        order.setStatus(OrderStatus.COMPLETED);
        return orderRepository.save(order);
    }

    // =============================================
    // GET METHODS
    // =============================================
    public Optional<OrderEntity> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public OrderEntity getOrder(Long id) {
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

    public List<OrderEntity> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<OrderEntity> getOrdersByPaymentStatus(PaymentStatus paymentStatus) {
        return orderRepository.findByPaymentStatus(paymentStatus);
    }

    // =============================================
    // VALIDATION & PERMISSION
    // =============================================
    public boolean canUserAccessOrder(Long orderId, Long userId) {
        Optional<OrderEntity> orderOpt = orderRepository.findById(orderId);
        return orderOpt.isPresent()
                && orderOpt.get().getUserId() != null
                && orderOpt.get().getUserId().equals(userId);
    }

    public boolean canCancelOrder(OrderEntity order) {
        return order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.CONFIRMED;
    }

    public OrderEntity getOrderWithPermission(Long orderId, Long userId) {
        OrderEntity order = getOrder(orderId);

        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        return order;
    }

    @Transactional
    public OrderEntity cancelOrderWithPermission(Long orderId, Long userId) {
        OrderEntity order = getOrderWithPermission(orderId, userId);

        if (!canCancelOrder(order)) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_ORDER);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    // =============================================
    // BUSINESS LOGIC
    // =============================================
    public BigDecimal calculateTotalRevenue() {
        List<OrderEntity> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED);
        return completedOrders.stream()
                .map(OrderEntity::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Không thể thay đổi trạng thái đơn hàng đã hủy");
        }

        if (currentStatus == OrderStatus.COMPLETED && newStatus != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Không thể thay đổi trạng thái đơn hàng đã hoàn thành");
        }
    }

    @Transactional
    public OrderEntity save(OrderEntity order) {
        if (order.getFinalAmount() == null) {
            order.calculateFinalAmount();
        }

        return orderRepository.save(order);
    }
}
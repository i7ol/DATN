package com.datn.shoporder.service;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.entity.OrderItemEntity;
import com.datn.shopdatabase.entity.ProductEntity;
import com.datn.shopdatabase.entity.UserEntity;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopdatabase.repository.OrderRepository;
import com.datn.shopdatabase.repository.ProductRepository;
import com.datn.shopdatabase.repository.UserRepository;
import com.datn.shopobject.dto.request.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    @Transactional
    public OrderEntity createOrderFromCheckout(
            Long userId,
            String guestId,
            String guestName,
            String guestEmail,
            String guestPhone,
            String shippingAddress,
            Integer shippingProvince,
            Integer shippingDistrict,
            Integer shippingWard,
            String shippingNote,
            String billingAddress,
            Integer  billingProvince,
            Integer  billingDistrict,
            Integer  billingWard,
            PaymentMethod paymentMethod,
            List<OrderItemEntity> items
    ) {

        if (userId == null) {
            if (shippingAddress == null
                    || shippingProvince == null
                    || shippingDistrict == null
                    || shippingWard == null) {

                throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu địa chỉ giao hàng");
            }
        }
        OrderEntity order = new OrderEntity();

        if (userId != null) {
            order.setUserId(userId);
            order.setGuestId(null);

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            if (shippingAddress == null && user.getAddress() == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "User chưa có địa chỉ");
            }

            if (shippingProvince == null && user.getProvinceCode() == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu tỉnh/thành");
            }

            if (shippingDistrict == null && user.getDistrictCode() == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu quận/huyện");
            }

            if (shippingWard == null && user.getWardCode() == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu phường/xã");
            }
            order.setShippingAddress(
                    shippingAddress != null ? shippingAddress : user.getAddress()
            );

            order.setShippingProvince(
                    shippingProvince != null ? shippingProvince : user.getProvinceCode()
            );

            order.setShippingDistrict(
                    shippingDistrict != null ? shippingDistrict : user.getDistrictCode()
            );

            order.setShippingWard(
                    shippingWard != null ? shippingWard : user.getWardCode()
            );
        } else {
            order.setUserId(null);
            order.setGuestId(guestId);
            if (guestName == null || guestPhone == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu thông tin khách hàng");
            }
            order.setGuestName(guestName);
            order.setGuestEmail(guestEmail);
            order.setGuestPhone(guestPhone);

            order.setShippingAddress(shippingAddress);
            order.setShippingProvince(shippingProvince);
            order.setShippingDistrict(shippingDistrict);
            order.setShippingWard(shippingWard);
        }

        // Set shipping address details
        order.setShippingNote(shippingNote);
        order.setShippingMethod("STANDARD");
        // Set billing address (use shipping if not provided)
        boolean hasFullBilling =
                billingAddress != null &&
                        billingProvince != null &&
                        billingDistrict != null &&
                        billingWard != null;

        if (hasFullBilling) {
            order.setBillingAddress(billingAddress);
            order.setBillingProvince(billingProvince);
            order.setBillingDistrict(billingDistrict);
            order.setBillingWard(billingWard);
        } else {
            order.setBillingAddress(order.getShippingAddress());
            order.setBillingProvince(order.getShippingProvince());
            order.setBillingDistrict(order.getShippingDistrict());
            order.setBillingWard(order.getShippingWard());
        }

        if (items == null || items.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Order không có sản phẩm");
        }
        items.forEach(item -> {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Số lượng không hợp lệ");
            }
            item.setOrder(order);
        });
        order.setItems(items);

        Map<Long, ProductEntity> productMap = productRepository
                .findAllById(items.stream().map(OrderItemEntity::getProductId).toList())
                .stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));
        BigDecimal total = items.stream()
                .map(item -> {
                    ProductEntity product = productMap.get(item.getProductId());
                    if (product == null) {
                        throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                    }

                    BigDecimal unitPrice = product.getPrice();
                    BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

                    // overwrite lại cho chắc
                    item.setUnitPrice(unitPrice);
                    item.setTotalPrice(itemTotal);

                    return itemTotal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        order.setTotalPrice(total);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.calculateFinalAmount();
        // Set status
        if (paymentMethod == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu phương thức thanh toán");
        }
        order.setPaymentMethod(paymentMethod);

        if (paymentMethod == PaymentMethod.COD) {
            order.setStatus(OrderStatus.PROCESSING);
            order.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setPaymentStatus(PaymentStatus.PENDING);
        }
        OrderEntity savedOrder = orderRepository.save(order);
        orderRepository.flush();

        savedOrder.setTrackingCode(generateTrackingCode(savedOrder.getId()));

        return savedOrder;
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

            item.setUnitPrice(product.getPrice());
            item.setQuantity(dto.getQuantity());
            item.setTotalPrice(
                    product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()))
            );

            item.setOrder(order);
            return item;
        }).collect(Collectors.toList());


        BigDecimal total = items.stream()
                .map(OrderItemEntity::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(items);
        order.setTotalPrice(total);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.calculateFinalAmount();


        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        OrderEntity savedOrder = orderRepository.save(order);

        savedOrder.setTrackingCode(generateTrackingCode(savedOrder.getId()));

        return orderRepository.save(savedOrder);
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
        }
        else if (status == PaymentStatus.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        else if (status == PaymentStatus.REFUNDED) {
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
    public OrderEntity updateShippingInfo(Long orderId, String shippingMethod, LocalDateTime estimatedDeliveryDate) {
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
        order.setActualDeliveryDate(LocalDateTime.now());

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

    public Page<OrderEntity> getOrdersByUserId(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "User ID không được null");
        }

        return orderRepository.findByUserIdWithItems(userId, pageable);
    }

    public Page<OrderEntity> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
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
        return order.getStatus() == OrderStatus.PENDING_PAYMENT
                || order.getStatus() == OrderStatus.PROCESSING;
    }

    public OrderEntity getOrderWithPermission(Long orderId, Long userId) {
        OrderEntity order = getOrder(orderId);

        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        return order;
    }

    private String generateTrackingCode(Long orderId) {
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return "VN-" + date + "-" + String.format("%06d", orderId);
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
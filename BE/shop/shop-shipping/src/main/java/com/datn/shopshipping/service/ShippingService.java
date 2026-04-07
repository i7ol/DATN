package com.datn.shopshipping.service;

import com.datn.shopdatabase.entity.ShippingOrderEntity;
import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.entity.UserEntity;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopdatabase.enums.StatusEnum;
import com.datn.shopobject.dto.request.ShippingRequest;
import com.datn.shopobject.dto.request.ShippingSearchRequest;
import com.datn.shopobject.dto.request.ShippingUpdateRequest;
import com.datn.shopobject.dto.response.ShippingCalculateResponse;
import com.datn.shopobject.dto.response.ShippingSummaryResponse;
import com.datn.shopdatabase.repository.ShippingRepository;
import com.datn.shopdatabase.repository.OrderRepository;
import com.datn.shopdatabase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ShippingProviderService shippingProviderService;

    // ADMIN tạo đơn vận chuyển
    @Transactional
    public ShippingOrderEntity create(ShippingRequest request) {

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentMethod() == PaymentMethod.VNPAY
                && order.getPaymentStatus() != PaymentStatus.PAID) {

            throw new RuntimeException("Order must be paid before shipping");
        }

        List<ShippingOrderEntity> existing = shippingRepository.findByOrderId(order.getId());
        if (!existing.isEmpty()) {
            throw new RuntimeException("Order already has shipping");
        }

        ShippingOrderEntity shippingOrder = ShippingOrderEntity.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .shippingCompany(request.getShippingCompany())
                .shippingMethod(request.getShippingMethod())
                .trackingCode(order.getTrackingCode())
                .shippingFee(request.getShippingFee())
                .estimatedDeliveryDays(request.getEstimatedDeliveryDays())
                .status(StatusEnum.PREPARING)
                .notes(request.getNotes())
                .build();

        setRecipientInfo(shippingOrder, order);

        ShippingOrderEntity saved = shippingRepository.save(shippingOrder);

        if (saved.getTrackingCode() != null) {
            saved = syncWithShippingProvider(saved.getId());
        }

        order.setStatus(OrderStatus.SHIPPING);
        orderRepository.save(order);

        return saved;
    }

    // USER xem shipping của mình
    public ShippingOrderEntity getByIdAndUserId(Long id, Long userId) {
        return shippingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Shipping not found"));
    }

    // ADMIN cập nhật trạng thái
    @Transactional
    public ShippingOrderEntity updateStatus(Long id, StatusEnum status, String notes) {

        ShippingOrderEntity shippingOrder = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping not found"));

        shippingOrder.setStatus(status);

        OrderEntity order = orderRepository.findById(shippingOrder.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (status == StatusEnum.SHIPPED) {
            shippingOrder.setShippedAt(LocalDateTime.now());
            order.setStatus(OrderStatus.SHIPPING);
        }

        if (status == StatusEnum.DELIVERED) {

            shippingOrder.setDeliveredAt(LocalDateTime.now());

            order.setStatus(OrderStatus.COMPLETED);

            if(order.getPaymentMethod() == PaymentMethod.COD){
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        }

        if (status == StatusEnum.CANCELLED) {
            shippingOrder.setCancelledAt(LocalDateTime.now());
            order.setStatus(OrderStatus.CANCELLED);
        }

        if (notes != null) {
            shippingOrder.setNotes(notes);
        }

        orderRepository.save(order);

        return shippingRepository.save(shippingOrder);
    }

    // Cập nhật thông tin shipping
    @Transactional
    public ShippingOrderEntity updateShippingInfo(Long id, ShippingUpdateRequest request) {
        ShippingOrderEntity shippingOrder = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping not found"));

        if (request.getTrackingCode() != null) {
            shippingOrder.setTrackingCode(request.getTrackingCode());
        }
        if (request.getShippingCompany() != null) {
            shippingOrder.setShippingCompany(request.getShippingCompany());
        }
        if (request.getShippingMethod() != null) {
            shippingOrder.setShippingMethod(request.getShippingMethod());
        }
        if (request.getShippingFee() != null) {
            shippingOrder.setShippingFee(request.getShippingFee());
        }
        if (request.getEstimatedDeliveryDays() != null) {
            shippingOrder.setEstimatedDeliveryDays(request.getEstimatedDeliveryDays());
        }
        if (request.getNotes() != null) {
            shippingOrder.setNotes(request.getNotes());
        }

        return shippingRepository.save(shippingOrder);
    }

    public ShippingOrderEntity getById(Long id) {
        return shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping not found"));
    }

    // Lấy shipping theo order
    public List<ShippingOrderEntity> getByOrderId(Long orderId) {
        return shippingRepository.findByOrderId(orderId);
    }

    // Lấy shipping của user
    public List<ShippingOrderEntity> getByUserId(Long userId) {
        return shippingRepository.findByUserId(userId);
    }

    // Lấy tất cả shipping với filter
    public Page<ShippingOrderEntity> getAllShippings(ShippingSearchRequest filter, Pageable pageable) {
        return shippingRepository.findAllWithFilters(
                filter.getStatus(),
                filter.getShippingCompany(),
                filter.getFromDate(),
                filter.getToDate(),
                pageable
        );
    }

    // Tính phí vận chuyển
    public ShippingCalculateResponse calculateShippingFee(String address, Double weight,
                                                          String shippingMethod, String company) {
        return shippingProviderService.calculateFee(address, weight, shippingMethod, company);
    }

    // Đồng bộ với hãng vận chuyển
    @Transactional
    public ShippingOrderEntity syncWithShippingProvider(Long shippingId) {
        ShippingOrderEntity shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("Shipping not found"));

        if (shipping.getTrackingCode() == null || shipping.getTrackingCode().isEmpty()) {
            throw new RuntimeException("Tracking number is required");
        }

        // Gọi API hãng vận chuyển
        ShippingProviderService.TrackingInfo trackingInfo =
                shippingProviderService.getTrackingInfo(shipping.getTrackingCode(),
                        shipping.getShippingCompany());

        // Cập nhật trạng thái
        shipping.setStatus(mapProviderStatus(trackingInfo.getStatus()));
        if (shipping.getStatus() == StatusEnum.SHIPPED) {
            shipping.setShippedAt(LocalDateTime.now());
        }

        if (shipping.getStatus() == StatusEnum.DELIVERED) {
            shipping.setDeliveredAt(LocalDateTime.now());
        }

        if (shipping.getStatus() == StatusEnum.CANCELLED) {
            shipping.setCancelledAt(LocalDateTime.now());
        }
        OrderEntity order = orderRepository.findById(shipping.getOrderId()).orElseThrow();

        if (shipping.getStatus() == StatusEnum.SHIPPED
                || shipping.getStatus() == StatusEnum.IN_TRANSIT
                || shipping.getStatus() == StatusEnum.OUT_FOR_DELIVERY) {

            order.setStatus(OrderStatus.SHIPPING);
        }

        if (shipping.getStatus() == StatusEnum.DELIVERED) {

            order.setStatus(OrderStatus.COMPLETED);

            if(order.getPaymentMethod() == PaymentMethod.COD){
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        }

        if (shipping.getStatus() == StatusEnum.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
        shipping.setCurrentLocation(trackingInfo.getCurrentLocation());
        shipping.setEstimatedDeliveryDate(trackingInfo.getEstimatedDeliveryDate());
        shipping.setLastSyncAt(LocalDateTime.now());

        return shippingRepository.save(shipping);
    }


    // Thống kê
    public ShippingSummaryResponse getShippingSummary() {
        long total = shippingRepository.count();
        long preparing = shippingRepository.countByStatus(com.datn.shopdatabase.enums.StatusEnum.PREPARING); // SỬA
        long shipped = shippingRepository.countByStatus(com.datn.shopdatabase.enums.StatusEnum.SHIPPED); // SỬA
        long delivered = shippingRepository.countByStatus(com.datn.shopdatabase.enums.StatusEnum.DELIVERED); // SỬA
        long cancelled = shippingRepository.countByStatus(com.datn.shopdatabase.enums.StatusEnum.CANCELLED); // SỬA

        return ShippingSummaryResponse.builder()
                .total(total)
                .preparing(preparing)
                .shipped(shipped)
                .delivered(delivered)
                .cancelled(cancelled)
                .build();
    }

    // Helper methods
    private void setRecipientInfo(ShippingOrderEntity shippingOrder, OrderEntity order) {
        if (order.getUserId() != null) {
            UserEntity user = userRepository.findById(order.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            // Sửa: Lấy fullName nếu có, nếu không dùng username
//            String recipientName = user.getFullName() != null ? user.getFullName() : user.getUsername();
            shippingOrder.setRecipientName(user.getUsername() );
            shippingOrder.setRecipientPhone(user.getPhone());
            shippingOrder.setRecipientEmail(user.getEmail());
        } else {
            shippingOrder.setRecipientName(order.getGuestName());
            shippingOrder.setRecipientPhone(order.getGuestPhone());
            shippingOrder.setRecipientEmail(order.getGuestEmail());
        }
        shippingOrder.setRecipientAddress(order.getShippingAddress());

        shippingOrder.setRecipientProvince(order.getShippingProvince());
        shippingOrder.setRecipientDistrict(order.getShippingDistrict());
        shippingOrder.setRecipientWard(order.getShippingWard());
    }

    private StatusEnum mapProviderStatus(String providerStatus) { // SỬA: Trả về StatusEnum
        // Map trạng thái từ hãng vận chuyển sang hệ thống
        return switch (providerStatus.toUpperCase()) {
            case "PICKED_UP", "IN_TRANSIT" -> com.datn.shopdatabase.enums.StatusEnum.SHIPPED;
            case "DELIVERED" -> com.datn.shopdatabase.enums.StatusEnum.DELIVERED;
            case "CANCELLED", "RETURNED" -> com.datn.shopdatabase.enums.StatusEnum.CANCELLED;
            case "READY_TO_SHIP" -> com.datn.shopdatabase.enums.StatusEnum.READY_TO_SHIP;
            case "OUT_FOR_DELIVERY" -> com.datn.shopdatabase.enums.StatusEnum.OUT_FOR_DELIVERY;
            case "FAILED" -> com.datn.shopdatabase.enums.StatusEnum.FAILED;
            default -> com.datn.shopdatabase.enums.StatusEnum.PREPARING;
        };
    }


}
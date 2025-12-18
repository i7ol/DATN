package com.datn.shopshipping.service;

import com.datn.shopdatabase.entity.ShippingOrderEntity;
import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.entity.UserEntity;
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

        // Kiểm tra order đã có shipping chưa
        List<ShippingOrderEntity> existingShippings = shippingRepository.findByOrderId(order.getId());
        if (!existingShippings.isEmpty()) {
            throw new RuntimeException("Order already has shipping");
        }

        // Kiểm tra order đã thanh toán chưa
        if (!"PAID".equals(order.getPaymentStatus())) {
            throw new RuntimeException("Order must be paid before shipping");
        }

        ShippingOrderEntity shippingOrder = ShippingOrderEntity.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .shippingCompany(request.getShippingCompany())
                .shippingMethod(request.getShippingMethod())
                .trackingNumber(request.getTrackingNumber())
                .shippingFee(request.getShippingFee())
                .estimatedDeliveryDays(request.getEstimatedDeliveryDays())
                .status(com.datn.shopdatabase.enums.StatusEnum.PREPARING) // SỬA: Sử dụng StatusEnum
                .notes(request.getNotes())
                .build();

        // Lấy thông tin người nhận
        setRecipientInfo(shippingOrder, order);

        // Lưu trước khi đồng bộ
        ShippingOrderEntity savedShipping = shippingRepository.save(shippingOrder);

        // Gọi API hãng vận chuyển (nếu có tracking number)
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isEmpty()) {
            savedShipping = syncWithShippingProvider(savedShipping.getId());
        }

        return savedShipping;
    }

    // USER xem shipping của mình
    public ShippingOrderEntity getByIdAndUserId(Long id, Long userId) {
        return shippingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Shipping not found"));
    }

    // ADMIN cập nhật trạng thái
    @Transactional
    public ShippingOrderEntity updateStatus(Long id, com.datn.shopdatabase.enums.StatusEnum status, String notes) { // SỬA: Sử dụng StatusEnum
        ShippingOrderEntity shippingOrder = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping not found"));

        shippingOrder.setStatus(status);

        if (status == com.datn.shopdatabase.enums.StatusEnum.SHIPPED) { // SỬA
            shippingOrder.setShippedAt(LocalDateTime.now());
        } else if (status == com.datn.shopdatabase.enums.StatusEnum.DELIVERED) { // SỬA
            shippingOrder.setDeliveredAt(LocalDateTime.now());
        } else if (status == com.datn.shopdatabase.enums.StatusEnum.CANCELLED) { // SỬA
            shippingOrder.setCancelledAt(LocalDateTime.now());
        }

        if (notes != null) {
            shippingOrder.setNotes(notes);
        }

        return shippingRepository.save(shippingOrder);
    }

    // Cập nhật thông tin shipping
    @Transactional
    public ShippingOrderEntity updateShippingInfo(Long id, ShippingUpdateRequest request) {
        ShippingOrderEntity shippingOrder = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping not found"));

        if (request.getTrackingNumber() != null) {
            shippingOrder.setTrackingNumber(request.getTrackingNumber());
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

        if (shipping.getTrackingNumber() == null || shipping.getTrackingNumber().isEmpty()) {
            throw new RuntimeException("Tracking number is required");
        }

        // Gọi API hãng vận chuyển
        ShippingProviderService.TrackingInfo trackingInfo =
                shippingProviderService.getTrackingInfo(shipping.getTrackingNumber(),
                        shipping.getShippingCompany());

        // Cập nhật trạng thái
        shipping.setStatus(mapProviderStatus(trackingInfo.getStatus()));
        shipping.setCurrentLocation(trackingInfo.getCurrentLocation());
        shipping.setEstimatedDeliveryDate(trackingInfo.getEstimatedDeliveryDate());
        shipping.setLastSyncAt(LocalDateTime.now());

        return shippingRepository.save(shipping);
    }

    // Thêm phương thức đồng bộ với đối tượng ShippingOrderEntity (cho nội bộ)
    private void syncWithShippingProvider(ShippingOrderEntity shipping) {
        if (shipping.getTrackingNumber() == null || shipping.getTrackingNumber().isEmpty()) {
            return;
        }

        // Gọi API hãng vận chuyển
        ShippingProviderService.TrackingInfo trackingInfo =
                shippingProviderService.getTrackingInfo(shipping.getTrackingNumber(),
                        shipping.getShippingCompany());

        // Cập nhật trạng thái
        shipping.setStatus(mapProviderStatus(trackingInfo.getStatus()));
        shipping.setCurrentLocation(trackingInfo.getCurrentLocation());
        shipping.setEstimatedDeliveryDate(trackingInfo.getEstimatedDeliveryDate());
        shipping.setLastSyncAt(LocalDateTime.now());
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
            String recipientName = user.getFullName() != null ? user.getFullName() : user.getUsername();
            shippingOrder.setRecipientName(recipientName);
            shippingOrder.setRecipientPhone(user.getPhone());
            shippingOrder.setRecipientEmail(user.getEmail());
        } else {
            shippingOrder.setRecipientName(order.getGuestName());
            shippingOrder.setRecipientPhone(order.getGuestPhone());
            shippingOrder.setRecipientEmail(order.getGuestEmail());
        }
        shippingOrder.setRecipientAddress(order.getShippingAddress());

        // Cập nhật thông tin địa chỉ chi tiết
        try {
            shippingOrder.setRecipientProvince(order.getShippingProvince());
        } catch (Exception e) {
            shippingOrder.setRecipientProvince(parseProvinceFromAddress(order.getShippingAddress()));
        }

        try {
            shippingOrder.setRecipientDistrict(order.getShippingDistrict());
        } catch (Exception e) {
            shippingOrder.setRecipientDistrict(parseDistrictFromAddress(order.getShippingAddress()));
        }

        try {
            shippingOrder.setRecipientWard(order.getShippingWard());
        } catch (Exception e) {
            shippingOrder.setRecipientWard(parseWardFromAddress(order.getShippingAddress()));
        }
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

    // Helper methods để parse địa chỉ
    private String parseProvinceFromAddress(String address) {
        if (address == null) return null;
        String[] provinces = {"Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ"};
        for (String province : provinces) {
            if (address.contains(province)) {
                return province;
            }
        }
        return null;
    }

    private String parseDistrictFromAddress(String address) {
        // Logic parse quận/huyện từ địa chỉ
        if (address == null) return null;
        String[] districts = {"Quận 1", "Quận 2", "Quận 3", "Huyện Củ Chi", "Huyện Bình Chánh"};
        for (String district : districts) {
            if (address.contains(district)) {
                return district;
            }
        }
        return null;
    }

    private String parseWardFromAddress(String address) {
        // Logic parse phường/xã từ địa chỉ
        return null;
    }
}
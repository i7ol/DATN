package com.datn.shoporder.service;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.entity.OrderReturnEntity;
import com.datn.shopdatabase.entity.OrderReturnItemEntity;
import com.datn.shopdatabase.entity.ReturnImageEntity;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.ReturnStatus;
import com.datn.shopdatabase.enums.ReturnType;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopdatabase.repository.OrderRepository;
import com.datn.shopdatabase.repository.OrderReturnRepository;
import com.datn.shopobject.dto.request.CreateReturnRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final OrderReturnRepository returnRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderReturnEntity createReturnRequest(
            Long userId,                    // null nếu là guest
            String guestId,                 // null nếu là user
            CreateReturnRequest request) {

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // ==================== KIỂM TRA QUYỀN SỞ HỮU ====================
        boolean isValidOwner = false;

        if (userId != null) {
            // Trường hợp User đã login
            isValidOwner = Objects.equals(order.getUserId(), userId);
        } else if (guestId != null || request.getGuestPhone() != null) {
            // Trường hợp Guest
            isValidOwner = (Objects.equals(order.getGuestId(), guestId) ||
                    Objects.equals(order.getGuestPhone(), request.getGuestPhone()));
        }

        if (!isValidOwner) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền thực hiện đổi trả trên đơn hàng này");
        }

        // Kiểm tra điều kiện đổi trả
        validateReturnEligibility(order);

        // Kiểm tra đã có yêu cầu đang xử lý chưa
        if (returnRepository.findActiveReturnByOrderId(order.getId()).isPresent()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đơn hàng này đã có yêu cầu đổi trả đang xử lý");
        }

        OrderReturnEntity returnRequest = OrderReturnEntity.builder()
                .orderId(order.getId())
                .userId(userId)
                .guestId(guestId != null ? guestId : order.getGuestId())
                .returnType(request.getReturnType())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReturnStatus.PENDING)
                .items(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        // Thêm items
        if (request.getItems() != null) {
            request.getItems().forEach(itemReq -> {
                OrderReturnItemEntity item = new OrderReturnItemEntity();
                item.setOrderItemId(itemReq.getOrderItemId());
                item.setProductId(itemReq.getProductId());
                item.setQuantity(itemReq.getQuantity());
                item.setReason(itemReq.getReason());
                returnRequest.addItem(item);
            });
        }

        // Thêm ảnh
        if (request.getImageUrls() != null) {
            request.getImageUrls().forEach(url -> {
                ReturnImageEntity image = new ReturnImageEntity();
                image.setImageUrl(url);
                returnRequest.addImage(image);
            });
        }

        return returnRepository.save(returnRequest);
    }

    private void validateReturnEligibility(OrderEntity order) {
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Chỉ đơn hàng đã giao hàng mới được yêu cầu đổi trả/bảo hành");
        }

        if (order.getActualDeliveryDate() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy ngày giao hàng");
        }

        LocalDateTime deadline = order.getActualDeliveryDate().plusDays(7);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Đã hết hạn yêu cầu đổi trả (tối đa 7 ngày kể từ ngày nhận hàng)");
        }
    }

    // ==================== ADMIN FUNCTIONS ====================

    @Transactional
    public OrderReturnEntity approveReturn(Long returnId, String adminNote, BigDecimal refundAmount) {
        OrderReturnEntity ret = getReturnById(returnId);
        ret.setStatus(ReturnStatus.APPROVED);
        ret.setAdminNote(adminNote);
        ret.setRefundAmount(refundAmount);
        ret.setProcessedDate(Instant.now());
        return returnRepository.save(ret);
    }

    @Transactional
    public OrderReturnEntity rejectReturn(Long returnId, String adminNote) {
        OrderReturnEntity ret = getReturnById(returnId);
        ret.setStatus(ReturnStatus.REJECTED);
        ret.setAdminNote(adminNote);
        ret.setProcessedDate(Instant.now());
        return returnRepository.save(ret);
    }

    @Transactional
    public OrderReturnEntity completeReturn(Long returnId, String refundTransactionId) {
        OrderReturnEntity ret = getReturnById(returnId);
        ret.setStatus(ReturnStatus.COMPLETED);
        ret.setRefundTransactionId(refundTransactionId);
        ret.setCompletedDate(Instant.now());
        return returnRepository.save(ret);
    }

    public OrderReturnEntity getReturnById(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RETURN_NOT_FOUND));
    }

    public Page<OrderReturnEntity> getMyReturns(Long userId, Pageable pageable) {
        return returnRepository.findByUserId(userId, pageable);
    }

    public Page<OrderReturnEntity> getAllReturns(Pageable pageable) {
        return returnRepository.findAll(pageable);
    }

    public Page<OrderReturnEntity> getPendingReturns(Pageable pageable) {
        return returnRepository.findPendingReturns(pageable);
    }
}
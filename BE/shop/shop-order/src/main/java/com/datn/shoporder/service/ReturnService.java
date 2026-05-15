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
import com.datn.shopobject.dto.request.ReturnItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnService {

    private final OrderReturnRepository returnRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderReturnEntity createReturnRequest(
            Long userId, String guestId, CreateReturnRequest request) {

        log.info("=== CREATE RETURN REQUEST START ===");
        log.info("UserId={}, GuestId={}, OrderId={}, ReturnType={}, Items={}",
                userId, guestId, request.getOrderId(), request.getReturnType(), request.getItems());

        // 1. Tìm order
        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND, "Không tìm thấy đơn hàng"));

        log.info("Order found - ID={}, Status={}, UserId={}, ActualDeliveryDate={}",
                order.getId(), order.getStatus(), order.getUserId(), order.getActualDeliveryDate());

        // 2. Kiểm tra quyền sở hữu
        boolean isValidOwner = (userId != null && Objects.equals(order.getUserId(), userId)) ||
                (guestId != null && Objects.equals(order.getGuestId(), guestId));
        if (!isValidOwner) {
            log.error("Permission denied! User {} / Guest {} is not owner of order {}",
                    userId, guestId, order.getId());
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền đổi trả đơn hàng này");
        }

        // 3. Validate thời gian
        validateReturnEligibility(order);

        // 4. Kiểm tra đã có return active chưa
        Optional<OrderReturnEntity> activeReturn = returnRepository.findActiveReturnByOrderId(order.getId());
        log.info("Has active return: {}", activeReturn.isPresent());
        if (activeReturn.isPresent()) {
            log.warn("Order {} already has an active return with ID: {}",
                    order.getId(), activeReturn.get().getId());
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đơn hàng này đã có yêu cầu đổi trả đang xử lý");
        }

        // 5. Tạo entity
        OrderReturnEntity returnRequest = OrderReturnEntity.builder()
                .orderId(order.getId())
                .userId(userId)
                .guestId(guestId != null ? guestId : order.getGuestId())
                .returnType(request.getReturnType())
                .reason(request.getReason())
                .description(request.getDescription() != null ? request.getDescription() : "")
                .status(ReturnStatus.PENDING)
                .build();

        // === KHỞI TẠO LIST AN TOÀN ===
        if (returnRequest.getItems() == null) {
            returnRequest.setItems(new ArrayList<>());
        }
        if (returnRequest.getImages() == null) {
            returnRequest.setImages(new ArrayList<>());
        }

        log.info("Entity built successfully. Items list size before adding: {}", returnRequest.getItems().size());

        // 6. Thêm items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Phải chọn ít nhất một sản phẩm để đổi trả");
        }

        for (ReturnItemRequest itemReq : request.getItems()) {
            log.info("Adding item - orderItemId={}, productId={}, quantity={}",
                    itemReq.getOrderItemId(), itemReq.getProductId(), itemReq.getQuantity());

            OrderReturnItemEntity item = new OrderReturnItemEntity();
            item.setOrderItemId(itemReq.getOrderItemId());
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1);
            item.setReason(itemReq.getReason() != null ? itemReq.getReason() : request.getReason());

            returnRequest.addItem(item);
        }

        // 7. Thêm ảnh (nếu có)
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String url : request.getImageUrls()) {
                ReturnImageEntity image = new ReturnImageEntity();
                image.setImageUrl(url);
                returnRequest.addImage(image);
            }
        }

        log.info("Saving return to database...");
        OrderReturnEntity saved = returnRepository.save(returnRequest);

        log.info("✅ RETURN CREATED SUCCESSFULLY - Return ID: {}", saved.getId());
        return saved;
    }

    private void validateReturnEligibility(OrderEntity order) {
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Chỉ đơn hàng đã giao (DELIVERED) mới được đổi trả. Trạng thái hiện tại: " + order.getStatus());
        }

        if (order.getActualDeliveryDate() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy ngày giao hàng thực tế");
        }

        LocalDateTime deadline = order.getActualDeliveryDate().plusDays(7).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime now = LocalDateTime.now();

        log.info("Deadline (end of day): {}, Now: {}", deadline, now);

        if (now.isAfter(deadline)) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Đã hết hạn yêu cầu đổi trả (tối đa 7 ngày kể từ ngày nhận hàng)");
        }

        log.info("✅ Validation passed - Days remaining: {}",
                java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), deadline.toLocalDate()));
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
package com.datn.shoporder.service;

import com.datn.shopclient.client.InventoryClient;
import com.datn.shopdatabase.entity.*;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.ReturnStatus;
import com.datn.shopdatabase.repository.OrderItemRepository;
import com.datn.shopdatabase.repository.OrderReturnRepository;
import com.datn.shopdatabase.repository.OrderRepository;
import com.datn.shopdatabase.repository.ProductRepository;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
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
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnService {

    private final OrderReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryClient inventoryClient;

    @Transactional
    public OrderReturnEntity createReturnRequest(
            Long userId, String guestId, CreateReturnRequest request) {

        log.info("=== CREATE RETURN REQUEST START ===");
        log.info("UserId={}, GuestId={}, OrderId={}, Items={}",
                userId, guestId, request.getOrderId(), request.getItems());

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra quyền sở hữu
        boolean isValidOwner = (userId != null && Objects.equals(order.getUserId(), userId)) ||
                (guestId != null && Objects.equals(order.getGuestId(), guestId));
        if (!isValidOwner) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền đổi trả đơn hàng này");
        }

        validateReturnEligibility(order);

        // Kiểm tra đã có return active chưa
        if (returnRepository.findActiveReturnByOrderId(order.getId()).isPresent()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đơn hàng này đã có yêu cầu đổi trả đang xử lý");
        }

        OrderReturnEntity returnRequest = OrderReturnEntity.builder()
                .orderId(order.getId())
                .userId(userId)
                .guestId(guestId != null ? guestId : order.getGuestId())
                .returnType(request.getReturnType())
                .reason(request.getReason())
                .description(request.getDescription() != null ? request.getDescription() : "")
                .status(ReturnStatus.PENDING)
                .items(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        BigDecimal totalReturnValue = BigDecimal.ZERO;

        for (ReturnItemRequest itemReq : request.getItems()) {
            OrderReturnItemEntity item = new OrderReturnItemEntity();
            item.setOrderItemId(itemReq.getOrderItemId());
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1);
            item.setReason(itemReq.getReason() != null ? itemReq.getReason() : request.getReason());

            ProductEntity product = productRepository.findById(itemReq.getProductId()).orElse(null);
            String productName = product != null ? product.getName() : "Sản phẩm #" + itemReq.getProductId();
            BigDecimal unitPrice = product != null ? product.getPrice() : BigDecimal.ZERO;

            item.setProductName(productName);
            item.setUnitPrice(unitPrice);

            totalReturnValue = totalReturnValue.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

            returnRequest.addItem(item);
        }

        returnRequest.setTotalReturnValue(totalReturnValue);

        // Thêm ảnh
        if (request.getImageUrls() != null) {
            for (String url : request.getImageUrls()) {
                ReturnImageEntity image = new ReturnImageEntity();
                image.setImageUrl(url);
                returnRequest.addImage(image);
            }
        }

        OrderReturnEntity saved = returnRepository.save(returnRequest);
        log.info(" RETURN CREATED - ID: {}, Total Items: {}, Total Value: {}",
                saved.getId(), saved.getItems().size(), totalReturnValue);

        return saved;
    }

    // ==================== ADMIN ACTIONS ====================

    @Transactional
    public OrderReturnEntity approveReturn(Long returnId, String adminNote, BigDecimal refundAmount) {
        OrderReturnEntity ret = getReturnById(returnId);

        if (ret.getStatus() != ReturnStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ có thể duyệt yêu cầu đang chờ xử lý");
        }

        ret.setStatus(ReturnStatus.APPROVED);
        ret.setAdminNote(adminNote);
        ret.setRefundAmount(refundAmount);
        ret.setProcessedDate(Instant.now());

        addStockForReturn(ret);   // Chỉ cộng kho ở bước APPROVE

        return returnRepository.save(ret);
    }

    @Transactional
    public OrderReturnEntity rejectReturn(Long returnId, String adminNote) {
        OrderReturnEntity ret = getReturnById(returnId);

        if (ret.getStatus() != ReturnStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ có thể từ chối yêu cầu đang chờ xử lý");
        }

        ret.setStatus(ReturnStatus.REJECTED);
        ret.setAdminNote(adminNote);
        ret.setProcessedDate(Instant.now());

        return returnRepository.save(ret);
    }

    @Transactional
    public OrderReturnEntity completeReturn(Long returnId, String refundTransactionId) {
        OrderReturnEntity ret = getReturnById(returnId);

        if (ret.getStatus() != ReturnStatus.APPROVED) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Phải duyệt trước khi hoàn thành");
        }

        ret.setStatus(ReturnStatus.COMPLETED);
        ret.setRefundTransactionId(refundTransactionId);
        ret.setCompletedDate(Instant.now());

        // KHÔNG cộng kho lại ở đây
        return returnRepository.save(ret);
    }

    // ==================== INVENTORY HELPER ====================

    private void addStockForReturn(OrderReturnEntity returnEntity) {
        log.info("=== ADD STOCK FOR RETURN ID: {} ===", returnEntity.getId());

        for (OrderReturnItemEntity item : returnEntity.getItems()) {
            if (item.getOrderItemId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                log.warn(" Skip item invalid: orderItemId={}, quantity={}",
                        item.getOrderItemId(), item.getQuantity());
                continue;
            }

            OrderItemEntity orderItem = findOrderItemById(item.getOrderItemId());
            if (orderItem == null || orderItem.getVariantId() == null) {
                log.warn(" Không tìm thấy OrderItem hoặc VariantId cho returnItemId={}", item.getId());
                continue;
            }

            try {
                inventoryClient.importStock(
                        orderItem.getVariantId(),
                        item.getQuantity(),
                        null,
                        "Hoàn trả đơn #" + returnEntity.getOrderId()
                );
                log.info("Cộng kho thành công - VariantId={} | Quantity={}",
                        orderItem.getVariantId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Lỗi cộng kho variantId={}", orderItem.getVariantId(), e);
            }
        }
    }

    private OrderItemEntity findOrderItemById(Long orderItemId) {
        if (orderItemId == null) return null;
        return orderItemRepository.findById(orderItemId).orElse(null);
    }

    // ==================== VALIDATION ====================

    private void validateReturnEligibility(OrderEntity order) {
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Chỉ đơn hàng đã giao mới được đổi trả. Trạng thái hiện tại: " + order.getStatus());
        }

        if (order.getActualDeliveryDate() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy ngày giao hàng thực tế");
        }

        LocalDateTime deadline = order.getActualDeliveryDate().plusDays(7)
                .withHour(23).withMinute(59).withSecond(59);

        if (LocalDateTime.now().isAfter(deadline)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đã hết hạn yêu cầu đổi trả (7 ngày)");
        }
    }

    public OrderReturnEntity getReturnById(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RETURN_NOT_FOUND));
    }

    // Các method get khác giữ nguyên...
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
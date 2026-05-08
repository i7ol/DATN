package com.datn.shoporder.controller;

import com.datn.shopdatabase.entity.OrderReturnEntity;
import com.datn.shopdatabase.entity.ProductEntity;
import com.datn.shopdatabase.entity.ReturnImageEntity;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopdatabase.repository.ProductRepository;
import com.datn.shopobject.dto.request.CreateReturnRequest;
import com.datn.shopobject.dto.response.ReturnResponse;
import com.datn.shopobject.dto.response.ReturnItemResponse;
import com.datn.shoporder.mapper.ReturnMapper;
import com.datn.shoporder.service.ReturnService;
import com.datn.shopobject.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnUserController {

    private final ReturnService returnService;
    private final ProductRepository productRepository;   // ← Thêm repository này

    // Tạo yêu cầu đổi trả / bảo hành
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse createReturn(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateReturnRequest request) {

        Long userId = principal != null ? principal.getId() : null;
        String guestId = request.getGuestId();

        // Nếu không phải user login thì phải có guestId hoặc guestPhone
        if (userId == null && (guestId == null && request.getGuestPhone() == null)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu thông tin xác thực (user hoặc guest)");
        }

        OrderReturnEntity returnEntity = returnService.createReturnRequest(userId, guestId, request);

        ReturnResponse response = ReturnMapper.toResponse(returnEntity);
        enrichReturnWithImages(response);
        return response;
    }

    // Danh sách yêu cầu của tôi
    @GetMapping(value = "/my-returns", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ReturnResponse> getMyReturns(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {

        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Page<OrderReturnEntity> returns = returnService.getMyReturns(principal.getId(), pageable);

        return returns.map(entity -> {
            ReturnResponse response = ReturnMapper.toResponse(entity);
            enrichReturnWithImages(response);
            return response;
        });
    }

    // Chi tiết một yêu cầu
    @GetMapping(value = "/{returnId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse getReturnDetail(
            @PathVariable Long returnId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        OrderReturnEntity returnEntity = returnService.getReturnById(returnId);

        if (returnEntity.getUserId() == null ||
                !returnEntity.getUserId().equals(principal.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        ReturnResponse response = ReturnMapper.toResponse(returnEntity);
        enrichReturnWithImages(response);   // ← Gọi enrich ảnh

        return response;
    }

    // ==================== HELPER: ENRICH ẢNH ====================
    private void enrichReturnWithImages(ReturnResponse response) {
        if (response.getItems() == null || response.getItems().isEmpty()) {
            return;
        }

        // Lấy danh sách ProductId từ items
        List<Long> productIds = response.getItems().stream()
                .map(ReturnItemResponse::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (productIds.isEmpty()) return;

        // Lấy thông tin sản phẩm + ảnh từ DB
        Map<Long, ProductEntity> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        // Gán ảnh sản phẩm vào từng item
        for (ReturnItemResponse item : response.getItems()) {
            ProductEntity product = productMap.get(item.getProductId());
            if (product != null && product.getImages() != null && !product.getImages().isEmpty()) {

                List<String> productImages = product.getImages().stream()
                        .map(com.datn.shopdatabase.entity.ProductImageEntity::getUrl)
                        .collect(Collectors.toList());

                item.setImages(productImages);   // Giả sử ReturnItemResponse có field List<String> images
            } else {
                item.setImages(new ArrayList<>()); // Tránh null
            }
        }
    }
}
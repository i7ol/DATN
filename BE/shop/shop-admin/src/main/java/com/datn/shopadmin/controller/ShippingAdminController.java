package com.datn.shopadmin.controller;

import com.datn.shopdatabase.entity.ShippingOrderEntity;
import com.datn.shopobject.dto.request.ShippingRequest;
import com.datn.shopobject.dto.request.ShippingSearchRequest;
import com.datn.shopobject.dto.request.ShippingUpdateRequest;
import com.datn.shopobject.dto.response.PageShippingResponse;
import com.datn.shopobject.dto.response.ShippingResponse;
import com.datn.shopobject.dto.response.ShippingSummaryResponse;
import com.datn.shopshipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/shipping")
@RequiredArgsConstructor
public class ShippingAdminController {

    private final ShippingService shippingService;

    @PostMapping
    public ResponseEntity<ShippingResponse> create(@RequestBody ShippingRequest request) {
        ShippingOrderEntity shipping = shippingService.create(request);
        return ResponseEntity.ok(toResponse(shipping));
    }

    @GetMapping
    public ResponseEntity<PageShippingResponse> getAllShippings(
            ShippingSearchRequest filter,
            Pageable pageable) {
        Page<ShippingOrderEntity> page = shippingService.getAllShippings(filter, pageable);

        // Convert Page<ShippingOrderEntity> to Page<ShippingResponse>
        Page<ShippingResponse> shippingPage = page.map(this::toResponse);

        // Create PageShippingResponse
        PageShippingResponse response = PageShippingResponse.builder()
                .content(shippingPage.getContent())
                .pageNumber(shippingPage.getNumber())
                .pageSize(shippingPage.getSize())
                .totalElements(shippingPage.getTotalElements())
                .totalPages(shippingPage.getTotalPages())
                .first(shippingPage.isFirst())
                .last(shippingPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<ShippingSummaryResponse> getSummary() {
        ShippingSummaryResponse summary = shippingService.getShippingSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShippingResponse> getById(@PathVariable Long id) {
        ShippingOrderEntity shipping = shippingService.getByIdAndUserId(id, null);
        return ResponseEntity.ok(toResponse(shipping));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ShippingResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        String notes = request.get("notes");

        // SỬA: Sử dụng StatusEnum từ package đúng
        com.datn.shopdatabase.enums.StatusEnum statusEnum =
                com.datn.shopdatabase.enums.StatusEnum.valueOf(status);

        ShippingOrderEntity shipping = shippingService.updateStatus(id, statusEnum, notes);
        return ResponseEntity.ok(toResponse(shipping));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShippingResponse> updateShippingInfo(
            @PathVariable Long id,
            @RequestBody ShippingUpdateRequest request) {
        ShippingOrderEntity shipping = shippingService.updateShippingInfo(id, request);
        return ResponseEntity.ok(toResponse(shipping));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ShippingResponse> syncWithProvider(@PathVariable Long id) {
        ShippingOrderEntity shipping = shippingService.syncWithShippingProvider(id);
        return ResponseEntity.ok(toResponse(shipping));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ShippingResponse>> getByOrderId(@PathVariable Long orderId) {
        List<ShippingOrderEntity> shippings = shippingService.getByOrderId(orderId);
        List<ShippingResponse> responses = shippings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private ShippingResponse toResponse(ShippingOrderEntity e) {
        return ShippingResponse.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .userId(e.getUserId())
                .recipientName(e.getRecipientName())
                .recipientPhone(e.getRecipientPhone())
                .recipientEmail(e.getRecipientEmail())
                .recipientAddress(e.getRecipientAddress())
                .shippingCompany(e.getShippingCompany())
                .shippingMethod(e.getShippingMethod())
                .trackingNumber(e.getTrackingNumber())
                .shippingFee(e.getShippingFee())
                .estimatedDeliveryDays(e.getEstimatedDeliveryDays())
                .estimatedDeliveryDate(e.getEstimatedDeliveryDate())
                .status(e.getStatus())
                .currentLocation(e.getCurrentLocation())
                .notes(e.getNotes())
                .shippedAt(e.getShippedAt())
                .deliveredAt(e.getDeliveredAt())
                .cancelledAt(e.getCancelledAt())
                .lastSyncAt(e.getLastSyncAt())
                .createdAt(e.getCreatedAt().toString())
                .updatedAt(e.getUpdatedAt().toString())
                .build();
    }
}
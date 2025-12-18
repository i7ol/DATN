
package com.datn.shopapp.controller;

import com.datn.shopdatabase.entity.ShippingOrderEntity;
import com.datn.shopobject.dto.response.ShippingResponse;
import com.datn.shopshipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/shipping")
@RequiredArgsConstructor
public class ShippingUserController {

    private final ShippingService shippingService;

    @GetMapping("/my-shippings")
    public ResponseEntity<List<ShippingResponse>> getMyShippings(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<ShippingOrderEntity> shippings = shippingService.getByUserId(userId);
        List<ShippingResponse> responses = shippings.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShippingResponse> getShippingByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<ShippingOrderEntity> shippings = shippingService.getByOrderId(orderId);
        if (shippings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Kiểm tra ownership
        ShippingOrderEntity shipping = shippings.get(0);
        if (!shipping.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(toResponse(shipping));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShippingResponse> getShippingById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ShippingOrderEntity shipping = shippingService.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(toResponse(shipping));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ShippingResponse> syncShipping(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ShippingOrderEntity shipping = shippingService.getByIdAndUserId(id, userId);
        ShippingOrderEntity updated = shippingService.syncWithShippingProvider(shipping.getId());
        return ResponseEntity.ok(toResponse(updated));
    }

    private ShippingResponse toResponse(ShippingOrderEntity e) {
        return ShippingResponse.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
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
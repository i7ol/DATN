package com.datn.shopshipping.controller;

import com.datn.shopobject.dto.request.ShippingRequest;
import com.datn.shopobject.dto.response.ShippingResponse;
import com.datn.shopdatabase.entity.ShippingOrderEntity;
import com.datn.shopshipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private  ShippingService shippingService;

    @PostMapping
    public ShippingResponse create(@RequestBody ShippingRequest request) {
        ShippingOrderEntity shippingOrder = shippingService.create(request);
        return toResponse(shippingOrder);
    }

    @PutMapping("/update-status/{id}")
    public ShippingResponse updateStatus(@PathVariable("id") Long id, @RequestParam(name = "status") ShippingOrderEntity.Status status) {
        ShippingOrderEntity shippingOrder = shippingService.updateStatus(id, status);
        return toResponse(shippingOrder);
    }

    @GetMapping("/{id}")
    public ShippingResponse get(@PathVariable("id") Long id) {
        return toResponse(shippingService.get(id));
    }

    @GetMapping("/by-order/{orderId}")
    public List<ShippingResponse> getByOrder(@PathVariable("orderId") Long orderId) {
        return shippingService.getByOrderId(orderId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ShippingResponse toResponse(ShippingOrderEntity order) {
        return ShippingResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .recipientAddress(order.getRecipientAddress())
                .shippingCompany(order.getShippingCompany())
                .shippingMethod(order.getShippingMethod())
                .trackingNumber(order.getTrackingNumber())
                .shippingFee(order.getShippingFee())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt().atOffset(ZoneOffset.UTC).toString())
                .updatedAt(order.getUpdatedAt().atOffset(ZoneOffset.UTC).toString())
                .build();
    }
}

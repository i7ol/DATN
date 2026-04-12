package com.datn.shoporder.controller;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopdatabase.repository.OrderRepository;
import com.datn.shopobject.dto.response.OrderResponse;
import com.datn.shoporder.mapper.OrderMapper;
import com.datn.shoporder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<OrderEntity> getAllOrders(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return orderRepository.findAll(sortedPageable);
    }

    @GetMapping(value = "/{orderId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return OrderMapper.toResponse(orderService.getOrder(orderId));
    }

    @PutMapping(value = "/{orderId}/status",produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    ) {
        return OrderMapper.toResponse(
                orderService.updateStatus(orderId, status)
        );
    }

    @PutMapping(value = "/{orderId}/payment-status",produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus
    ) {
        return OrderMapper.toResponse(
                orderService.updatePaymentStatus(orderId, paymentStatus)
        );
    }   
}



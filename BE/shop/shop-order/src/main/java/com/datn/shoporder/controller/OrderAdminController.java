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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    @GetMapping(value = "/statistics/revenue", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getRevenueStatistics() {
        return orderService.getRevenueStatistics();
    }
    @PutMapping(value = "/{orderId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse completeOrder(@PathVariable Long orderId) {
        return OrderMapper.toResponse(orderService.completeOrder(orderId));
    }

    @PutMapping(value = "/{orderId}/delivered", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse markAsDelivered(@PathVariable Long orderId) {
        return OrderMapper.toResponse(orderService.markAsDelivered(orderId));
    }
    @GetMapping("/statistics/top-products")
    public List<Map<String, Object>> getTopProducts(@RequestParam(defaultValue = "10") int limit) {
        return orderService.getTopSellingProducts(limit);
    }

    @GetMapping("/statistics/revenue-by-date")
    public List<Map<String, Object>> getRevenueByDate(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return orderService.getRevenueByDateRange(start, end);
    }

    @GetMapping("/statistics/summary")
    public Map<String, Object> getRevenueSummary(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return orderService.getRevenueSummary(start, end);
    }
    @GetMapping("/statistics/revenue-range")
    public List<Map<String, Object>> getRevenueByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return orderService.getRevenueByDateRangeFlexible(startDate, endDate);
    }
}



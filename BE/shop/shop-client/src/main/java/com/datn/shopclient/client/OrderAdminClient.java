package com.datn.shopclient.client;

import com.datn.shopclient.config.FeignClientUserConfig;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "order-admin",
        url = "${order.service.url}",
        configuration = FeignClientUserConfig.class
)
public interface OrderAdminClient {

    @GetMapping(value = "/api/admin/orders",produces = MediaType.APPLICATION_JSON_VALUE)
    Page<OrderResponse> getAll(Pageable pageable);

    @GetMapping(value = "/api/admin/orders/{orderId}",produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse getOrder(@PathVariable Long orderId);

    @PutMapping(value = "/api/admin/orders/{orderId}/status",produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    );

    @PutMapping(value = "/api/admin/orders/{orderId}/payment-status",produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus
    );
    @GetMapping(value = "/api/admin/orders/statistics/revenue", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getRevenueStatistics();
    @PutMapping(value = "/api/admin/orders/{orderId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse completeOrder(@PathVariable("orderId") Long orderId);

    @PutMapping(value = "/api/admin/orders/{orderId}/delivered", produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse markAsDelivered(@PathVariable("orderId") Long orderId);

    @GetMapping(value = "/api/admin/orders/statistics/top-products", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Map<String, Object>> getTopSellingProducts(@RequestParam(defaultValue = "10") int limit);

    @GetMapping(value = "/api/admin/orders/statistics/revenue-by-date", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Map<String, Object>> getRevenueByDate(
            @RequestParam String startDate,
            @RequestParam String endDate);

    @GetMapping(value = "/api/admin/orders/statistics/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getRevenueSummary(
            @RequestParam String startDate,
            @RequestParam String endDate);
}

